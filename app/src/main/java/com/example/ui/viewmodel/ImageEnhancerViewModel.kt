package com.example.ui.viewmodel

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.ImageEnhancementParams
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.AppDatabase
import com.example.data.database.EnhancementHistoryItem
import com.example.data.image.ImageProcessor
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

sealed interface UiState {
    object Idle : UiState
    object Analyzing : UiState
    object Enhancing : UiState
    data class Success(
        val originalUri: Uri,
        val originalWidth: Int,
        val originalHeight: Int,
        val enhancedBitmap: Bitmap,
        val params: ImageEnhancementParams,
        val scale: Int,
        val savedUri: Uri? = null
    ) : UiState
    data class Error(val message: String) : UiState
}

class ImageEnhancerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val historyDao = database.historyDao()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _history = MutableStateFlow<List<EnhancementHistoryItem>>(emptyList())
    val history: StateFlow<List<EnhancementHistoryItem>> = _history.asStateFlow()

    // Control parameters (user can adjust these after Gemini makes recommendations, or use presets)
    val selectedScale = MutableStateFlow(2) // 2x default, or 4, or 1
    val brightnessOffset = MutableStateFlow(0f)
    val contrastFactor = MutableStateFlow(1f)
    val saturationFactor = MutableStateFlow(1f)
    val sharpnessStrength = MutableStateFlow(0f)
    val noiseReductionStrength = MutableStateFlow(0f)

    // Live preview split slider position (0.0 to 1.0)
    val sliderPosition = MutableStateFlow(0.5f)

    // Chosen original image Uri
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    init {
        // Load history
        viewModelScope.launch {
            historyDao.getAllHistory().collectLatest { items ->
                _history.value = items
            }
        }
    }

    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
        _uiState.value = UiState.Idle
        // Reset control values to safe defaults
        brightnessOffset.value = 0f
        contrastFactor.value = 1f
        saturationFactor.value = 1f
        sharpnessStrength.value = 0f
        noiseReductionStrength.value = 0f
    }

    fun triggerEnhancement() {
        val uri = _selectedImageUri.value
        if (uri == null) {
            _uiState.value = UiState.Error("Please select or upload an image first.")
            return
        }

        val scale = selectedScale.value

        viewModelScope.launch {
            _uiState.value = UiState.Analyzing
            try {
                // 1. Load original bitmap and calculate its dimensions
                val originalBitmap = loadBitmapFromUri(uri) ?: throw Exception("Failed to load selected image.")
                val origWidth = originalBitmap.width
                val origHeight = originalBitmap.height

                // 2. Prepare analysis request for Gemini
                val analysisParams = performGeminiAnalysis(originalBitmap)

                // 3. Update sliders based on Gemini recommendations
                brightnessOffset.value = analysisParams.brightness
                contrastFactor.value = analysisParams.contrast
                saturationFactor.value = analysisParams.saturation
                sharpnessStrength.value = analysisParams.sharpness
                noiseReductionStrength.value = analysisParams.noiseReductionStrength

                _uiState.value = UiState.Enhancing

                // 4. Run local high-performance DSP enhancement
                val enhanced = withContext(Dispatchers.Default) {
                    ImageProcessor.enhance(
                        src = originalBitmap,
                        brightness = analysisParams.brightness,
                        contrast = analysisParams.contrast,
                        saturation = analysisParams.saturation,
                        sharpness = analysisParams.sharpness,
                        noiseReduction = analysisParams.noiseReductionStrength,
                        scale = scale
                    )
                }

                // 5. Cache the enhanced image locally and save to Room history
                val cacheFile = saveBitmapToCache(enhanced)
                val historyItem = EnhancementHistoryItem(
                    originalUri = uri.toString(),
                    enhancedPath = cacheFile.absolutePath,
                    brightness = analysisParams.brightness,
                    contrast = analysisParams.contrast,
                    saturation = analysisParams.saturation,
                    sharpness = analysisParams.sharpness,
                    noiseReduction = analysisParams.noiseReductionStrength,
                    scale = scale,
                    analysis = analysisParams.analysis,
                    enhancementSummary = analysisParams.enhancementSummary
                )
                withContext(Dispatchers.IO) {
                    historyDao.insertHistory(historyItem)
                }

                _uiState.value = UiState.Success(
                    originalUri = uri,
                    originalWidth = origWidth,
                    originalHeight = origHeight,
                    enhancedBitmap = enhanced,
                    params = analysisParams,
                    scale = scale
                )

            } catch (e: Exception) {
                Log.e("ImageEnhancerViewModel", "Enhancement failed", e)
                _uiState.value = UiState.Error(e.localizedMessage ?: "An unexpected error occurred.")
            }
        }
    }

    /**
     * Applies manual adjustment changes after Gemini's analysis, allowing instant preview recalculation.
     */
    fun applyManualAdjustments() {
        val uri = _selectedImageUri.value ?: return
        val currentState = _uiState.value
        if (currentState !is UiState.Success) return

        viewModelScope.launch {
            _uiState.value = UiState.Enhancing
            try {
                val originalBitmap = loadBitmapFromUri(uri) ?: throw Exception("Failed to load original image.")
                val enhanced = withContext(Dispatchers.Default) {
                    ImageProcessor.enhance(
                        src = originalBitmap,
                        brightness = brightnessOffset.value,
                        contrast = contrastFactor.value,
                        saturation = saturationFactor.value,
                        sharpness = sharpnessStrength.value,
                        noiseReduction = noiseReductionStrength.value,
                        scale = selectedScale.value
                    )
                }

                _uiState.value = currentState.copy(
                    enhancedBitmap = enhanced,
                    scale = selectedScale.value,
                    params = currentState.params.copy(
                        brightness = brightnessOffset.value,
                        contrast = contrastFactor.value,
                        saturation = saturationFactor.value,
                        sharpness = sharpnessStrength.value,
                        noiseReductionStrength = noiseReductionStrength.value,
                        enhancementSummary = "Manually adjusted sliders."
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to apply manual edits: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Downloads (saves) the enhanced image to the public device gallery.
     */
    fun downloadEnhancedImage() {
        val state = _uiState.value
        if (state !is UiState.Success) return

        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val bitmap = state.enhancedBitmap
                val filename = "Lumina_${System.currentTimeMillis()}.png"

                val savedUri: Uri? = withContext(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Lumina")
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }

                        val resolver = context.contentResolver
                        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            resolver.openOutputStream(uri).use { out ->
                                if (out != null) {
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                }
                            }
                            contentValues.clear()
                            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            resolver.update(uri, contentValues, null, null)
                        }
                        uri
                    } else {
                        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        val appDir = File(imagesDir, "Lumina")
                        if (!appDir.exists()) appDir.mkdirs()
                        val file = File(appDir, filename)
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        Uri.fromFile(file)
                    }
                }

                if (savedUri != null) {
                    _uiState.value = state.copy(savedUri = savedUri)
                } else {
                    throw Exception("Could not allocate media store slot.")
                }
            } catch (e: Exception) {
                Log.e("ImageEnhancerViewModel", "Failed to save to gallery", e)
                _uiState.value = UiState.Error("Failed to save image to gallery: ${e.localizedMessage}")
            }
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.deleteHistoryById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.clearAllHistory()
        }
    }

    private suspend fun performGeminiAnalysis(bitmap: Bitmap): ImageEnhancementParams = withContext(Dispatchers.Default) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback parameters if no API key is set yet
            return@withContext ImageEnhancementParams(
                brightness = 0.05f,
                contrast = 1.15f,
                saturation = 1.1f,
                sharpness = 1.2f,
                noiseReductionStrength = 0.3f,
                analysis = "No API key configured. Applied generic modern smart enhancement heuristics.",
                enhancementSummary = "Automatic local brightness boost, contrast stretching, and bilateral edge preservation."
            )
        }

        // 1. Resize bitmap to under 512x512 to preserve upload speed and control cost
        val analysisSize = 384
        val resized = if (bitmap.width > analysisSize || bitmap.height > analysisSize) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val tw = if (ratio > 1) analysisSize else (analysisSize * ratio).toInt()
            val th = if (ratio > 1) (analysisSize / ratio).toInt() else analysisSize
            Bitmap.createScaledBitmap(bitmap, tw, th, true)
        } else {
            bitmap
        }

        // 2. Base64 Encode
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        // 3. Compose Request
        val systemPromptText = "You are an expert digital image processing AI. Analyze the uploaded low-resolution or imperfect image. Identify visual artifacts, lighting inconsistencies, noise, blurriness, and color fading. Provide precise DSP parameters to enhance the image. You must respond with valid raw JSON only, matching the exact schema."

        val promptText = """
            Analyze this image and return optimal digital signal processing (DSP) parameters as a JSON object matching this schema:
            {
              "brightness": <float between -0.4 and 0.4 representing light offset correction>,
              "contrast": <float between 0.8 and 1.6 representing contrast stretch multiplier>,
              "saturation": <float between 0.8 and 1.6 representing color enhancement multiplier>,
              "sharpness": <float between 0.0 and 2.5 representing high-pass convolution strength>,
              "noiseReductionStrength": <float between 0.0 and 1.0 representing noise smoothing blend strength>,
              "analysis": "<1-sentence details of noise, contrast, and sharpness issues>",
              "enhancementSummary": "<1-sentence summary of parameters recommended>"
            }
            Do not include any markdown format blocks, notes, or explanations outside the JSON object.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = promptText),
                        Part(
                            inlineData = InlineData(
                                mimeType = "image/jpeg",
                                data = base64Data
                            )
                        )
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemPromptText))
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from Gemini API.")

            // Clean json text from markdown code blocks
            val cleanedJson = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adapter = RetrofitClient.moshiInstance.adapter(ImageEnhancementParams::class.java)
            adapter.fromJson(cleanedJson) ?: throw Exception("Failed to parse enhancement parameters from JSON.")
        } catch (e: Exception) {
            Log.e("ImageEnhancerViewModel", "Gemini call failed, using default parameters", e)
            ImageEnhancementParams(
                brightness = 0.08f,
                contrast = 1.12f,
                saturation = 1.15f,
                sharpness = 1.4f,
                noiseReductionStrength = 0.35f,
                analysis = "Gemini processing unavailable or rate limited. Used fallback heuristics.",
                enhancementSummary = "Adaptive local exposure scaling and medium-strength sharp-unsharp masking."
            )
        }
    }

    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val context = getApplication<Application>()
            val resolver = context.contentResolver
            resolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e("ImageEnhancerViewModel", "Failed to load bitmap from uri: $uri", e)
            null
        }
    }

    private suspend fun saveBitmapToCache(bitmap: Bitmap): File = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "enhanced_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file
    }
}

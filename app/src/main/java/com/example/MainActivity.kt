package com.example

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.data.database.EnhancementHistoryItem
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ImageEnhancerViewModel
import com.example.ui.viewmodel.UiState
import java.io.File
import kotlin.math.roundToInt

// Programmatic custom high-quality vector icons to avoid resource-loading failures
val SparklesIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Sparkles",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(9f, 21f)
            lineTo(7.5f, 16.5f)
            lineTo(3f, 15f)
            lineTo(7.5f, 13.5f)
            lineTo(9f, 9f)
            lineTo(10.5f, 13.5f)
            lineTo(15f, 15f)
            lineTo(10.5f, 16.5f)
            close()
            moveTo(19f, 9f)
            lineTo(18.25f, 6.75f)
            lineTo(16f, 6f)
            lineTo(18.25f, 5.25f)
            lineTo(19f, 3f)
            lineTo(19.75f, 5.25f)
            lineTo(22f, 6f)
            lineTo(19.75f, 6.75f)
            close()
            moveTo(19f, 21f)
            lineTo(18.25f, 18.75f)
            lineTo(16f, 18f)
            lineTo(18.25f, 17.25f)
            lineTo(19f, 15f)
            lineTo(19.75f, 17.25f)
            lineTo(22f, 18f)
            lineTo(19.75f, 18.75f)
            close()
        }
    }.build()

val DownloadIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Download",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2.5f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(4f, 17f)
            verticalLineToRelative(2f)
            quadToRelative(0f, 0.8f, 0.6f, 1.4f)
            quadToRelative(0.6f, 0.6f, 1.4f, 0.6f)
            horizontalLineToRelative(12f)
            quadToRelative(0.8f, 0f, 1.4f, -0.6f)
            quadToRelative(0.6f, -0.6f, 0.6f, -1.4f)
            verticalLineToRelative(-2f)
            moveTo(12f, 3f)
            verticalLineToRelative(11f)
            moveTo(8f, 10f)
            lineTo(12f, 14f)
            lineTo(16f, 10f)
        }
    }.build()

val UploadIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Upload",
        defaultWidth = 32.dp,
        defaultHeight = 32.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color(0xFFD0BCFF)),
            strokeLineWidth = 2f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
        ) {
            moveTo(12f, 21f)
            verticalLineTo(7f)
            moveTo(5f, 14f)
            lineTo(12f, 7f)
            lineTo(19f, 14f)
            moveTo(2f, 3f)
            horizontalLineToRelative(20f)
        }
    }.build()

val CompareIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Compare",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(10f, 21f)
            horizontalLineTo(4f)
            quadToRelative(-0.825f, 0f, -1.412f, -0.587f)
            quadTo(2f, 19.825f, 2f, 19f)
            verticalLineTo(5f)
            quadToRelative(0f, -0.825f, 0.588f, -1.412f)
            quadTo(3.175f, 3f, 4f, 3f)
            horizontalLineToRelative(6f)
            close()
            moveTo(20f, 21f)
            horizontalLineToRelative(-6f)
            verticalLineTo(3f)
            horizontalLineToRelative(6f)
            quadToRelative(0.825f, 0f, 1.413f, 0.588f)
            quadTo(22f, 4.175f, 22f, 5f)
            verticalLineToRelative(14f)
            quadToRelative(0f, 0.825f, -0.587f, 1.413f)
            quadTo(20.825f, 21f, 20f, 21f)
            close()
        }
    }.build()

// Simple Split-Screen shape clipper
class SplitShape(private val fraction: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = 0f,
                right = size.width * fraction,
                bottom = size.height
            )
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageEnhancerScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ImageEnhancerScreen(
    modifier: Modifier = Modifier,
    viewModel: ImageEnhancerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val history by viewModel.history.collectAsState()
    val sliderPosition by viewModel.sliderPosition.collectAsState()
    val scaleFactor by viewModel.selectedScale.collectAsState()

    // Parameters
    val brightness by viewModel.brightnessOffset.collectAsState()
    val contrast by viewModel.contrastFactor.collectAsState()
    val saturation by viewModel.saturationFactor.collectAsState()
    val sharpness by viewModel.sharpnessStrength.collectAsState()
    val noiseReduction by viewModel.noiseReductionStrength.collectAsState()

    // Activity launcher for picking images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.selectImage(uri)
        }
    }

    // Trigger visual cues on download
    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            val success = uiState as UiState.Success
            if (success.savedUri != null) {
                Toast.makeText(context, "Saved enhanced image to Gallery!", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Modern UI Shell matching 'Immersive UI' styling
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // 1. Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gradient AI/Lumina Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF9333EA))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Column {
                    Text(
                        text = "Lumina HD",
                        color = Color(0xFFE6E1E5),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Gemini Neural Enhancer",
                        color = Color(0xFF938F99),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Right side state indicator (Simple visual check indicating Gemini connectivity status)
            val hasApiKey = BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (hasApiKey) Color(0x1510B981) else Color(0x15F59E0B))
                    .border(
                        width = 1.dp,
                        color = if (hasApiKey) Color(0xFF10B981) else Color(0xFFF59E0B),
                        shape = CircleShape
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (hasApiKey) Color(0xFF10B981) else Color(0xFFF59E0B))
                    )
                    Text(
                        text = if (hasApiKey) "GEMINI ENGINE" else "HEURISTIC MODE",
                        color = if (hasApiKey) Color(0xFF34D399) else Color(0xFFFBBF24),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // LazyColumn to ensure everything scrolls smoothly and fits perfectly across form factors
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview & Comparison Canvas Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF1C1B1F))
                        .border(1.dp, Color(0xFF49454F), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri == null) {
                        // Empty State / Upload Zone
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { imagePickerLauncher.launch("image/*") }
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B2930)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = UploadIcon,
                                    contentDescription = "Upload Icon",
                                    tint = Color(0xFFD0BCFF)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Select Image to Enhance",
                                color = Color(0xFFE6E1E5),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Supports JPG, PNG, WEBP. Neural networks will analyze, denoise, and upscale pixels automatically.",
                                color = Color(0xFF938F99),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 280.dp)
                            )
                        }
                    } else {
                        // Loaded / Processing / Success State
                        when (uiState) {
                            is UiState.Idle -> {
                                // Chosen image but not processed yet
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Selected Original",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    )
                                    // Soft overlay to indicate ready for trigger
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f))
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Button(
                                            onClick = { viewModel.triggerEnhancement() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFD0BCFF),
                                                contentColor = Color(0xFF381E72)
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.testTag("enhance_button")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = SparklesIcon,
                                                    contentDescription = "Enhance"
                                                )
                                                Text(
                                                    "Analyze & Enhance",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            is UiState.Analyzing, is UiState.Enhancing -> {
                                val stateMessage = if (uiState is UiState.Analyzing) {
                                    "Gemini analyzing image imperfections..."
                                } else {
                                    "Re-synthesizing pixels & upscaling dimensions..."
                                }

                                // Interactive Glowing Spinner Overlay
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xE00D0D0D))
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFD0BCFF),
                                        strokeWidth = 4.dp,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = stateMessage,
                                        color = Color(0xFFE6E1E5),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Please keep the app open",
                                        color = Color(0xFF938F99),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            is UiState.Success -> {
                                val success = uiState as UiState.Success
                                // Split screen custom comparison slider!
                                BoxWithConstraints(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val boxWidth = constraints.maxWidth.toFloat()
                                    val boxHeight = constraints.maxHeight.toFloat()

                                    // Base layer (Full screen): Enhanced image
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = success.enhancedBitmap,
                                            contentDescription = "Enhanced Preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(12.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF381E72).copy(alpha = 0.8f))
                                                .border(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "ENHANCED (${(success.originalWidth * success.scale)}x${(success.originalHeight * success.scale)})",
                                                color = Color(0xFFD0BCFF),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    // Top layer (Clipped to fraction): Original image
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(SplitShape(sliderPosition))
                                    ) {
                                        AsyncImage(
                                            model = success.originalUri,
                                            contentDescription = "Original Preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(12.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.7f))
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "ORIGINAL (${success.originalWidth}x${success.originalHeight})",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    // Swipe detector layer overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .pointerInput(Unit) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val newFraction = (sliderPosition + dragAmount.x / boxWidth).coerceIn(0f, 1f)
                                                    viewModel.sliderPosition.value = newFraction
                                                }
                                            }
                                    )

                                    // Divider Line representation
                                    val densityLocal = androidx.compose.ui.platform.LocalDensity.current
                                    val dividerX = boxWidth * sliderPosition
                                    val dividerXDp = with(densityLocal) { dividerX.toDp() }
                                    val handleXDp = with(densityLocal) { (dividerX - 18f).toDp() }
                                    val handleYDp = with(densityLocal) { (boxHeight / 2f - 18f).toDp() }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(2.dp)
                                            .offset(x = dividerXDp)
                                            .background(Color.White.copy(alpha = 0.6f))
                                    )

                                    // Grab Handle circle in the absolute middle of divider line
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .offset(
                                                x = handleXDp,
                                                y = handleYDp
                                            )
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFF1C1B1F), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Box(modifier = Modifier.size(2.dp, 12.dp).background(Color(0xFF1C1B1F)))
                                            Box(modifier = Modifier.size(2.dp, 12.dp).background(Color(0xFF1C1B1F)))
                                        }
                                    }
                                }
                            }

                            is UiState.Error -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = (uiState as UiState.Error).message,
                                        color = Color(0xFFE6E1E5),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = { viewModel.triggerEnhancement() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2930))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Retry")
                                            Text("Retry Enhancement", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Stats Label
            if (uiState is UiState.Success) {
                val s = uiState as UiState.Success
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${s.originalWidth}x${s.originalHeight} → ${(s.originalWidth * s.scale)}x${(s.originalHeight * s.scale)} (${s.scale}x Scale)",
                            color = Color(0xFF938F99),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "DENOISE: ${(s.params.noiseReductionStrength * 100).roundToInt()}% | SHARP: ${(s.params.sharpness * 100).roundToInt()}%",
                            color = Color(0xFF938F99),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 2. Control & Adjustments Panel
            if (selectedImageUri != null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF1C1B1F))
                            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        // Quick Presets Toggles
                        Text(
                            text = "PRESETS",
                            color = Color(0xFF938F99),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Preset 1: Auto Light / Smart Balance
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF2B2930))
                                    .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        // Set smart high-fidelity values
                                        viewModel.brightnessOffset.value = 0.12f
                                        viewModel.contrastFactor.value = 1.25f
                                        viewModel.saturationFactor.value = 1.15f
                                        viewModel.sharpnessStrength.value = 1.1f
                                        viewModel.noiseReductionStrength.value = 0.25f
                                        viewModel.applyManualAdjustments()
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF818CF8))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "AUTO LIGHT",
                                        color = Color(0xFF818CF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Preset 2: Maximum Detail (De-Noise + Super Sharp)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF2B2930))
                                    .border(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        viewModel.brightnessOffset.value = 0.0f
                                        viewModel.contrastFactor.value = 1.1f
                                        viewModel.saturationFactor.value = 1.0f
                                        viewModel.sharpnessStrength.value = 2.4f
                                        viewModel.noiseReductionStrength.value = 0.6f
                                        viewModel.applyManualAdjustments()
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFD0BCFF))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "MAX DETAIL",
                                        color = Color(0xFFD0BCFF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Preset 3: Smooth Soft/Face Portra
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF2B2930))
                                    .border(1.dp, Color(0xFFE6E1E5).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        viewModel.brightnessOffset.value = 0.05f
                                        viewModel.contrastFactor.value = 0.95f
                                        viewModel.saturationFactor.value = 1.2f
                                        viewModel.sharpnessStrength.value = 0.4f
                                        viewModel.noiseReductionStrength.value = 0.85f
                                        viewModel.applyManualAdjustments()
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF938F99))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "PORTRAIT",
                                        color = Color(0xFF938F99),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Scaler Multipliers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Upscale Multiplier",
                                color = Color(0xFFE6E1E5),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF0D0D0D))
                                    .border(1.dp, Color(0xFF49454F), RoundedCornerShape(20.dp))
                                    .padding(2.dp)
                            ) {
                                val scales = listOf(1, 2, 4)
                                scales.forEach { factor ->
                                    val isSelected = scaleFactor == factor
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(if (isSelected) Color(0xFF6366F1) else Color.Transparent)
                                            .clickable {
                                                viewModel.selectedScale.value = factor
                                                viewModel.applyManualAdjustments()
                                            }
                                            .padding(horizontal = 14.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (factor == 1) "Off" else "${factor}x",
                                            color = if (isSelected) Color.White else Color(0xFF938F99),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Display analysis summary if present
                        if (uiState is UiState.Success) {
                            val success = uiState as UiState.Success
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "AI ANALYSIS",
                                        color = Color(0xFFD0BCFF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = success.params.analysis,
                                        color = Color(0xFFE6E1E5),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = success.params.enhancementSummary,
                                        color = Color(0xFF938F99),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Fine Tuning Sliders
                        Text(
                            text = "FINE TUNING CORRECTIONS",
                            color = Color(0xFF938F99),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Brightness Slider (-0.4f to 0.4f)
                        AdjustmentSlider(
                            label = "Brightness Offset",
                            value = brightness,
                            valueRange = -0.4f..0.4f,
                            onValueChange = {
                                viewModel.brightnessOffset.value = it
                                viewModel.applyManualAdjustments()
                            }
                        )

                        // Contrast Slider (0.7f to 1.7f)
                        AdjustmentSlider(
                            label = "Contrast Multiplier",
                            value = contrast,
                            valueRange = 0.7f..1.7f,
                            onValueChange = {
                                viewModel.contrastFactor.value = it
                                viewModel.applyManualAdjustments()
                            }
                        )

                        // Saturation Slider (0.7f to 1.7f)
                        AdjustmentSlider(
                            label = "Saturation Intensity",
                            value = saturation,
                            valueRange = 0.7f..1.7f,
                            onValueChange = {
                                viewModel.saturationFactor.value = it
                                viewModel.applyManualAdjustments()
                            }
                        )

                        // Sharpness Slider (0.0f to 2.5f)
                        AdjustmentSlider(
                            label = "Sharpness Boost",
                            value = sharpness,
                            valueRange = 0.0f..2.5f,
                            onValueChange = {
                                viewModel.sharpnessStrength.value = it
                                viewModel.applyManualAdjustments()
                            }
                        )

                        // Noise Reduction Slider (0.0f to 1.0f)
                        AdjustmentSlider(
                            label = "De-Noise Threshold",
                            value = noiseReduction,
                            valueRange = 0.0f..1.0f,
                            onValueChange = {
                                viewModel.noiseReductionStrength.value = it
                                viewModel.applyManualAdjustments()
                            }
                        )
                    }
                }
            }

            // Primary Bottom Actions
            if (selectedImageUri != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Core Action Button
                        Button(
                            onClick = { viewModel.triggerEnhancement() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = SparklesIcon,
                                    contentDescription = "Enhance"
                                )
                                Text(
                                    text = "Enhance Pixels",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // Download Action Button
                        IconButton(
                            onClick = { viewModel.downloadEnhancedImage() },
                            enabled = uiState is UiState.Success,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (uiState is UiState.Success) Color(0xFF1C1B1F) else Color(0x301C1B1F))
                                .border(1.dp, Color(0xFF49454F), RoundedCornerShape(18.dp))
                        ) {
                            Icon(
                                imageVector = DownloadIcon,
                                contentDescription = "Download to Gallery",
                                tint = if (uiState is UiState.Success) Color.White else Color.Gray
                            )
                        }

                        // Reset / Change file
                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF1C1B1F))
                                .border(1.dp, Color(0xFF49454F), RoundedCornerShape(18.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Change Image",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // 3. Enhancement History Row
            if (history.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECENT ENHANCEMENTS",
                                color = Color(0xFF938F99),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Clear All",
                                color = Color(0xFFD0BCFF),
                                fontSize = 11.sp,
                                modifier = Modifier.clickable { viewModel.clearHistory() }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(history) { item ->
                                HistoryCard(
                                    item = item,
                                    onSelect = {
                                        viewModel.selectImage(Uri.parse(item.originalUri))
                                        viewModel.triggerEnhancement()
                                    },
                                    onDelete = { viewModel.deleteHistoryItem(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdjustmentSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color(0xFFCAC4D0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = String.format("%.2f", value),
                color = Color(0xFFD0BCFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                activeTrackColor = Color(0xFF6366F1),
                inactiveTrackColor = Color(0xFF2B2930),
                thumbColor = Color(0xFFD0BCFF)
            )
        )
    }
}

@Composable
fun HistoryCard(
    item: EnhancementHistoryItem,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val file = File(item.enhancedPath)
    if (!file.exists()) return

    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F))
    ) {
        Box(modifier = Modifier.height(140.dp)) {
            // Render the cached enhanced image
            val bitmap = remember(item.enhancedPath) {
                try {
                    BitmapFactory.decodeFile(item.enhancedPath)
                } catch (e: Exception) {
                    null
                }
            }

            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Historical enhancement thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray)
                )
            }

            // Quick Info Overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "${item.scale}x Neural",
                        color = Color(0xFFD0BCFF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Factor: ${item.scale}x",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 8.sp,
                        maxLines = 1
                    )
                }
            }

            // Delete history item indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete from history",
                    tint = Color.LightGray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

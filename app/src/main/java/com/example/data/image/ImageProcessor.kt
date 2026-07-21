package com.example.data.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlin.math.abs

object ImageProcessor {

    /**
     * Fully enhances a bitmap by applying color, sharpness, noise reduction, and scaling.
     */
    fun enhance(
        src: Bitmap,
        brightness: Float,           // -1.0 to 1.0
        contrast: Float,             // 0.5 to 2.0
        saturation: Float,           // 0.5 to 2.0
        sharpness: Float,            // 0.0 to 3.0
        noiseReduction: Float,       // 0.0 to 1.0
        scale: Int                   // 1, 2, or 4
    ): Bitmap {
        // 1. Perform Upscaling first if requested
        val upscaled = if (scale > 1) {
            val targetWidth = (src.width * scale).coerceAtMost(4096)
            val targetHeight = (src.height * scale).coerceAtMost(4096)
            Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
        } else {
            src
        }

        // 2. Apply Brightness, Contrast, and Saturation
        val colorAdjusted = applyColorAdjustments(upscaled, brightness, contrast, saturation)

        // 3. Apply Selective Noise Smoothing (Bilateral filter)
        val denoised = if (noiseReduction > 0f) {
            applyNoiseReduction(colorAdjusted, noiseReduction)
        } else {
            colorAdjusted
        }

        // 4. Apply High-Pass Sharpening (Convolution)
        val sharpened = if (sharpness > 0f) {
            applySharpening(denoised, sharpness)
        } else {
            denoised
        }

        return sharpened
    }

    private fun applyColorAdjustments(
        src: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): Bitmap {
        val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val matrix = ColorMatrix()

        // Saturation adjustment
        val satMatrix = ColorMatrix().apply { setSaturation(saturation) }

        // Contrast and Brightness adjustment
        // formula: pixel_val = pixel_val * contrast + brightness * 255 + 128 * (1 - contrast)
        val c = contrast
        val b = brightness * 255f
        val translate = 128f * (1f - c) + b
        val contrastMatrix = ColorMatrix(floatArrayOf(
            c, 0f, 0f, 0f, translate,
            0f, c, 0f, 0f, translate,
            0f, 0f, c, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))

        matrix.postConcat(satMatrix)
        matrix.postConcat(contrastMatrix)

        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(src, 0f, 0f, paint)

        return output
    }

    private fun applyNoiseReduction(src: Bitmap, strength: Float): Bitmap {
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        System.arraycopy(pixels, 0, outPixels, 0, pixels.size)

        val spatialRadius = 2
        val rangeThreshold = 15f + strength * 45f // Edge protection threshold (15 to 60)
        val blendStrength = strength.coerceIn(0f, 1f)

        // Run bilateral smoothing on interior pixels to avoid boundary checks
        for (y in spatialRadius until height - spatialRadius) {
            val offset = y * width
            for (x in spatialRadius until width - spatialRadius) {
                val idx = offset + x
                val centerColor = pixels[idx]

                val cR = (centerColor shr 16) and 0xFF
                val cG = (centerColor shr 8) and 0xFF
                val cB = centerColor and 0xFF
                val cA = (centerColor shr 24) and 0xFF

                var sumR = 0f
                var sumG = 0f
                var sumB = 0f
                var weightSum = 0f

                // 5x5 neighborhood bilateral-like filter
                for (ky in -spatialRadius..spatialRadius) {
                    val kOffset = (y + ky) * width
                    for (kx in -spatialRadius..spatialRadius) {
                        val pColor = pixels[kx + x + kOffset]
                        val pR = (pColor shr 16) and 0xFF
                        val pG = (pColor shr 8) and 0xFF
                        val pB = pColor and 0xFF

                        // Color similarity using simple absolute difference of luminance
                        val diff = abs((pR + pG + pB) / 3f - (cR + cG + cB) / 3f)

                        // If range difference is small, include in smooth smoothing
                        val rangeWeight = if (diff < rangeThreshold) {
                            1f - (diff / rangeThreshold)
                        } else {
                            0f
                        }

                        sumR += pR * rangeWeight
                        sumG += pG * rangeWeight
                        sumB += pB * rangeWeight
                        weightSum += rangeWeight
                    }
                }

                if (weightSum > 0f) {
                    val outR = ((sumR / weightSum) * blendStrength + cR * (1f - blendStrength)).toInt().coerceIn(0, 255)
                    val outG = ((sumG / weightSum) * blendStrength + cG * (1f - blendStrength)).toInt().coerceIn(0, 255)
                    val outB = ((sumB / weightSum) * blendStrength + cB * (1f - blendStrength)).toInt().coerceIn(0, 255)
                    outPixels[idx] = (cA shl 24) or (outR shl 16) or (outG shl 8) or outB
                }
            }
        }

        val dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        dest.setPixels(outPixels, 0, width, 0, 0, width, height)
        return dest
    }

    private fun applySharpening(src: Bitmap, strength: Float): Bitmap {
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        System.arraycopy(pixels, 0, outPixels, 0, pixels.size)

        val centerWeight = 1f + 4f * strength

        for (y in 1 until height - 1) {
            val offset = y * width
            for (x in 1 until width - 1) {
                val idx = offset + x

                val c = pixels[idx]
                val l = pixels[idx - 1]
                val r = pixels[idx + 1]
                val t = pixels[idx - width]
                val b = pixels[idx + width]

                // Red channel
                val rC = (c shr 16) and 0xFF
                val rL = (l shr 16) and 0xFF
                val rR = (r shr 16) and 0xFF
                val rT = (t shr 16) and 0xFF
                val rB = (b shr 16) and 0xFF
                val newR = (rC * centerWeight - (rL + rR + rT + rB) * strength).toInt().coerceIn(0, 255)

                // Green channel
                val gC = (c shr 8) and 0xFF
                val gL = (l shr 8) and 0xFF
                val gR = (r shr 8) and 0xFF
                val gT = (t shr 8) and 0xFF
                val gB = (b shr 8) and 0xFF
                val newG = (gC * centerWeight - (gL + gR + gT + gB) * strength).toInt().coerceIn(0, 255)

                // Blue channel
                val bC = c and 0xFF
                val bL = l and 0xFF
                val bR = r and 0xFF
                val bT = t and 0xFF
                val bB = b and 0xFF
                val newB = (bC * centerWeight - (bL + bR + bT + bB) * strength).toInt().coerceIn(0, 255)

                val a = (c shr 24) and 0xFF

                outPixels[idx] = (a shl 24) or (newR shl 16) or (newG shl 8) or newB
            }
        }

        val dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        dest.setPixels(outPixels, 0, width, 0, 0, width, height)
        return dest
    }
}

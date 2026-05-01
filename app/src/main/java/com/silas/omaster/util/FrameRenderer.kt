package com.silas.omaster.util

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface

enum class OutputRatio(val label: String, val widthToHeight: Float, val platform: String) {
    SQUARE("1:1", 1f, "Instagram"),
    PORTRAIT_4_5("4:5", 0.8f, "Instagram"),
    PORTRAIT_3_4("3:4", 0.75f, "小红书"),
    FULL("9:16", 9f / 16f, "朋友圈/Stories"),
    LANDSCAPE_16_9("16:9", 16f / 9f, "封面/B站")
}

object FrameRenderer {

    private const val BASE = 1080
    private const val TOP_AREA_RATIO = 0.30f
    private const val IMAGE_PADDING = 36f
    private const val BOTTOM_RESERVED = 60f

    private const val TITLE_SIZE = 52f
    private const val WATERMARK_SIZE = 22f
    private const val ROUNDED_RADIUS = 48f

    data class Params(
        val source: Bitmap,
        val dominantColor: Int,
        val textColor: Int,
        val title: String = "",
        val useRoundedCorners: Boolean = true,
        val ratio: OutputRatio = OutputRatio.FULL,
        val showWatermark: Boolean = true
    )

    fun render(params: Params): Bitmap = with(params) {
        val isLandscape = ratio.widthToHeight > 1f
        val ratioValue = if (isLandscape) 1f / ratio.widthToHeight else ratio.widthToHeight

        val outputWidth: Int
        val outputHeight: Int
        if (isLandscape) {
            outputHeight = BASE
            outputWidth = (BASE / ratioValue).toInt()
        } else {
            outputWidth = BASE
            outputHeight = (BASE / ratioValue).toInt()
        }

        val output = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        drawBackground(canvas, dominantColor, outputWidth, outputHeight)

        val topAreaBottom = outputHeight * TOP_AREA_RATIO
        val hasTitle = title.isNotBlank()

        if (hasTitle) {
            drawTitleText(canvas, title, textColor, outputWidth, topAreaBottom)
        }

        val imageTop = topAreaBottom + IMAGE_PADDING
        val imageMaxWidth = outputWidth - IMAGE_PADDING * 2
        val imageMaxHeight = outputHeight - imageTop - BOTTOM_RESERVED

        val scale = minOf(
            imageMaxWidth / source.width.toFloat(),
            imageMaxHeight / source.height.toFloat()
        )
        val drawWidth = source.width * scale
        val drawHeight = source.height * scale
        val drawLeft = (outputWidth - drawWidth) / 2f
        val drawTop = imageTop + (imageMaxHeight - drawHeight) / 2f

        val imageRect = RectF(drawLeft, drawTop, drawLeft + drawWidth, drawTop + drawHeight)
        val cr = if (useRoundedCorners) ROUNDED_RADIUS else 0f

        if (cr > 0f) {
            drawImageShadow(canvas, imageRect)
        }

        val clipPath = Path().apply {
            addRoundRect(imageRect, cr, cr, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(clipPath)
        canvas.drawBitmap(source, null, imageRect, Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        })
        canvas.restore()

        if (showWatermark) {
            drawWatermark(canvas, textColor, outputWidth, outputHeight)
        }

        output
    }

    private fun drawBackground(canvas: Canvas, color: Int, w: Int, h: Int) {
        val lighter = adjustBrightness(color, 1.06f)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = android.graphics.LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(lighter, color),
                floatArrayOf(0f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    }

    private fun adjustBrightness(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(color, hsv)
        hsv[2] = (hsv[2] * factor).coerceIn(0f, 1f)
        return AndroidColor.HSVToColor(hsv)
    }

    private fun drawTitleText(canvas: Canvas, title: String, textColor: Int, w: Int, topBottom: Float) {
        val paint = Paint().apply {
            color = textColor
            textSize = TITLE_SIZE
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, w / 2f, topBottom / 2 + TITLE_SIZE / 3, paint)
    }

    private fun drawImageShadow(canvas: Canvas, rect: RectF) {
        val shadowPaint = Paint().apply {
            isAntiAlias = true
            color = 0x22000000
            maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
        }
        val shadowRect = RectF(rect.left + 4f, rect.top + 8f, rect.right - 2f, rect.bottom + 8f)
        val shadowPath = Path().apply {
            addRoundRect(shadowRect, ROUNDED_RADIUS, ROUNDED_RADIUS, Path.Direction.CW)
        }
        canvas.drawPath(shadowPath, shadowPaint)
    }

    private fun drawWatermark(canvas: Canvas, textColor: Int, w: Int, h: Int) {
        val paint = Paint().apply {
            color = textColor and 0x00FFFFFF or 0x35000000
            textSize = WATERMARK_SIZE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("OMaster", w / 2f, h - 36f, paint)
    }
}

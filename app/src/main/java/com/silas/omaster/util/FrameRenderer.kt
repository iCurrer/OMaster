package com.silas.omaster.util

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface

object FrameRenderer {

    private const val OUTPUT_WIDTH = 1080
    private const val OUTPUT_HEIGHT = 1920

    private const val TOP_AREA_RATIO = 0.35f
    private const val IMAGE_PADDING_H = 40f
    private const val IMAGE_PADDING_BOTTOM = 80f

    private const val TITLE_SIZE = 56f
    private const val WATERMARK_SIZE = 24f
    private const val ROUNDED_RADIUS = 48f

    data class Params(
        val source: Bitmap,
        val dominantColor: Int,
        val textColor: Int,
        val title: String = "",
        val useRoundedCorners: Boolean = true,
        val showWatermark: Boolean = true
    )

    fun render(params: Params): Bitmap = with(params) {
        val output = Bitmap.createBitmap(OUTPUT_WIDTH, OUTPUT_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        drawBackground(canvas, dominantColor)

        val topAreaBottom = OUTPUT_HEIGHT * TOP_AREA_RATIO
        val hasTitle = title.isNotBlank()

        if (hasTitle) {
            drawTitleText(canvas, title, textColor, topAreaBottom)
        }

        val imageTop = topAreaBottom + IMAGE_PADDING_H
        val imageLeft = IMAGE_PADDING_H
        val imageMaxWidth = OUTPUT_WIDTH - IMAGE_PADDING_H * 2
        val imageMaxHeight = OUTPUT_HEIGHT - imageTop - IMAGE_PADDING_BOTTOM

        val scale = minOf(
            imageMaxWidth / source.width.toFloat(),
            imageMaxHeight / source.height.toFloat()
        )
        val drawWidth = source.width * scale
        val drawHeight = source.height * scale
        val drawLeft = (OUTPUT_WIDTH - drawWidth) / 2f
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
            drawWatermark(canvas, textColor)
        }

        output
    }

    private fun drawBackground(canvas: Canvas, color: Int) {
        val lighter = adjustBrightness(color, 1.06f)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = android.graphics.LinearGradient(
                0f, 0f, 0f, OUTPUT_HEIGHT.toFloat(),
                intArrayOf(lighter, color),
                floatArrayOf(0f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, OUTPUT_WIDTH.toFloat(), OUTPUT_HEIGHT.toFloat(), paint)
    }

    private fun adjustBrightness(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(color, hsv)
        hsv[2] = (hsv[2] * factor).coerceIn(0f, 1f)
        return AndroidColor.HSVToColor(hsv)
    }

    private fun drawTitleText(
        canvas: Canvas,
        title: String,
        textColor: Int,
        topAreaBottom: Float
    ) {
        val paint = Paint().apply {
            color = textColor
            textSize = TITLE_SIZE
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, OUTPUT_WIDTH / 2f, topAreaBottom / 2 + TITLE_SIZE / 3, paint)
    }

    private fun drawImageShadow(canvas: Canvas, rect: RectF) {
        val shadowPaint = Paint().apply {
            isAntiAlias = true
            color = 0x22000000
            maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
        }
        val shadowRect = RectF(
            rect.left + 4f, rect.top + 8f,
            rect.right - 2f, rect.bottom + 8f
        )
        val shadowPath = Path().apply {
            addRoundRect(shadowRect, ROUNDED_RADIUS, ROUNDED_RADIUS, Path.Direction.CW)
        }
        canvas.drawPath(shadowPath, shadowPaint)
    }

    private fun drawWatermark(canvas: Canvas, textColor: Int) {
        val paint = Paint().apply {
            color = textColor and 0x00FFFFFF or 0x35000000
            textSize = WATERMARK_SIZE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("OMaster", OUTPUT_WIDTH / 2f, OUTPUT_HEIGHT - 40f, paint)
    }
}

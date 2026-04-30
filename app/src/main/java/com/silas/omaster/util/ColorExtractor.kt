package com.silas.omaster.util

import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DominantColorResult(
    val dominant: Int,
    val vibrant: Int?,
    val muted: Int?,
    val textColor: Int
)

object ColorExtractor {

    suspend fun extract(bitmap: Bitmap): DominantColorResult = withContext(Dispatchers.Default) {
        val palette = Palette.from(bitmap)
            .maximumColorCount(8)
            .generate()

        val dominant = palette.getVibrantColor(
            palette.getDominantColor(android.graphics.Color.GRAY)
        )

        val vibrant = palette.getVibrantColor(
            palette.getLightVibrantColor(dominant)
        ).let { if (it != dominant) it else null }

        val muted = palette.getMutedColor(
            palette.getDarkMutedColor(dominant)
        ).let { if (it != dominant) it else null }

        val r = android.graphics.Color.red(dominant)
        val g = android.graphics.Color.green(dominant)
        val b = android.graphics.Color.blue(dominant)
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
        val textColor = if (luminance > 0.55) android.graphics.Color.BLACK
        else android.graphics.Color.WHITE

        DominantColorResult(
            dominant = dominant,
            vibrant = vibrant,
            muted = muted,
            textColor = textColor
        )
    }
}

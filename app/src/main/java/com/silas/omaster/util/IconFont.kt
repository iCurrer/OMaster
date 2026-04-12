package com.silas.omaster.util

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import com.silas.omaster.R

/**
 * Iconfont 图标字体工具类
 *
 * 使用阿里 Iconfont 图标库
 * 字体文件: res/font/iconfont.ttf
 */
object IconFont {

    // 8个基础参数的图标编码（OPPO/一加）
    const val FILTER = "\ue660"      // 滤镜
    const val SOFT_LIGHT = "\ue600"  // 柔光
    const val TONE = "\ue6e4"        // 影调
    const val SATURATION = "\ue748"  // 饱和度
    const val WARM_COOL = "\ue61e"   // 冷暖
    const val CYAN = "\ue601"        // 青品
    const val SHARPNESS = "\ue6bd"   // 锐度
    const val VIGNETTE = "\ue627"    // 暗角
    
    // 8个基础参数的文字标签（Realme）
    const val FILTER_TEXT = "滤镜"       // 滤镜 - 文字显示
    const val SOFT_LIGHT_TEXT = "柔光"   // 柔光 - 文字显示
    const val TONE_TEXT = "影调"         // 影调 - 文字显示
    const val SATURATION_TEXT = "饱和"   // 饱和度 - 文字显示
    const val WARM_COOL_TEXT = "冷暖"    // 冷暖 - 文字显示
    const val CYAN_TEXT = "青品"          // 青品 - 文字显示
    const val SHARPNESS_TEXT = "锐度"    // 锐度 - 文字显示
    const val VIGNETTE_TEXT = "暗角"      // 暗角 - 文字显示
    
    // Realme 特有参数的文字标签
    const val CONTRAST_HIGHLIGHT_TEXT = "亮部"    // 对比度（亮部）- 文字显示
    const val CONTRAST_SHADOW_TEXT = "暗部"      // 对比度（暗部）- 文字显示
    const val GRAIN_INTENSITY_TEXT = "颗粒"       // 颗粒强度 - 文字显示
    const val GRAIN_SIZE_TEXT = "尺寸"            // 颗粒尺寸 - 文字显示
    const val HUE_TEXT = "色相"                   // 色相 - 文字显示（Realme理光专用）
    const val BRIGHTNESS_TEXT = "明暗"            // 明暗 - 文字显示（Realme理光专用，代替冷暖）
    
    // 3个扩展参数的文字标签（用于真我理光GR等11参数预设）
    // 注意：这些不是 iconfont 图标，而是文字缩写，因为原字体文件中没有对应图标
    const val CLARITY = "清晰"       // 清晰度 - 文字显示
    const val CONTRAST = "对比"      // 对比度 - 文字显示（代替褪色）
    const val GRAIN = "颗粒"         // 颗粒 - 文字显示

    // 图标列表（按顺序 - 支持11个参数）
    val ICONS = listOf(
        FILTER,
        SOFT_LIGHT,
        TONE,
        SATURATION,
        WARM_COOL,
        CYAN,
        SHARPNESS,
        VIGNETTE,
        CLARITY,
        CONTRAST,
        GRAIN
    )

    // 缓存字体，避免重复加载
    private var cachedTypeface: Typeface? = null

    /**
     * 获取 Iconfont 字体
     */
    fun getTypeface(context: Context): Typeface {
        return cachedTypeface ?: try {
            val typeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.resources.getFont(R.font.iconfont)
            } else {
                // 兼容旧版本，从 assets 加载
                Typeface.createFromAsset(context.assets, "font/iconfont.ttf")
            }
            cachedTypeface = typeface
            typeface
        } catch (e: Exception) {
            // 如果加载失败，返回默认字体
            android.util.Log.e("IconFont", "Failed to load iconfont", e)
            Typeface.DEFAULT
        }
    }
}

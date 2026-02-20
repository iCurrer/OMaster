package com.silas.omaster.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.silas.omaster.R

/**
 * 预设数据本地化工具
 * 用于将存储在 presets.json 中的中文字符串转换为当前语言的字符串
 */
object PresetI18n {

    /**
     * 获取滤镜名称对应的资源 ID
     */
    fun getFilterResId(name: String): Int? {
        return when (name) {
            "标准" -> R.string.filter_standard
            "霓虹" -> R.string.filter_neon
            "清新" -> R.string.filter_fresh
            "复古" -> R.string.filter_vintage
            "通透" -> R.string.filter_clear
            "明艳" -> R.string.filter_vivid
            "童话" -> R.string.filter_fairy
            "人文" -> R.string.filter_humanities
            "自然" -> R.string.filter_natural
            "美味" -> R.string.filter_delicious
            "冷调" -> R.string.filter_cool
            "暖调" -> R.string.filter_warm
            "浓郁" -> R.string.filter_rich
            "高级灰" -> R.string.filter_advanced_gray
            "黑白" -> R.string.filter_bw
            "单色" -> R.string.filter_mono
            "赛博朋克" -> R.string.filter_cyberpunk
            "原图" -> R.string.floating_original
            else -> null
        }
    }

    /**
     * 获取本地化的滤镜名称
     * @param filterString 原始滤镜字符串（可能是 "复古 100%" 或 "复古"）
     * @return 本地化的显示字符串
     */
    @Composable
    fun getLocalizedFilter(filterString: String): String {
        // 分离名称和强度
        val parts = filterString.split(" ")
        val name = parts[0]
        val percentage = if (parts.size > 1) parts[1] else ""

        val resId = getFilterResId(name)

        return if (resId != null) {
            val localizedName = stringResource(resId)
            if (percentage.isNotEmpty()) "$localizedName $percentage" else localizedName
        } else {
            filterString // 如果没有匹配项，返回原始字符串
        }
    }

    /**
     * 获取本地化的滤镜名称（Context 版本）
     */
    fun getLocalizedFilter(context: android.content.Context, filterString: String): String {
        // 分离名称和强度
        val parts = filterString.split(" ")
        val name = parts[0]
        val percentage = if (parts.size > 1) parts[1] else ""

        val resId = getFilterResId(name)

        return if (resId != null) {
            val localizedName = context.getString(resId)
            if (percentage.isNotEmpty()) "$localizedName $percentage" else localizedName
        } else {
            filterString // 如果没有匹配项，返回原始字符串
        }
    }

    /**
     * 获取仅滤镜名称的本地化字符串（不带强度）
     */
    @Composable
    fun getLocalizedFilterNameOnly(filterName: String): String {
        val resId = getFilterResId(filterName)
        return if (resId != null) stringResource(resId) else filterName
    }

    /**
     * 获取仅滤镜名称的本地化字符串（Context 版本）
     */
    fun getLocalizedFilterNameOnly(context: android.content.Context, filterName: String): String {
        val resId = getFilterResId(filterName)
        return if (resId != null) context.getString(resId) else filterName
    }

    /**
     * 获取柔光名称对应的资源 ID
     */
    fun getSoftLightResId(softLight: String): Int? {
        return when (softLight) {
            "无" -> R.string.soft_none
            "柔美" -> R.string.soft_gentle
            "梦幻" -> R.string.soft_dreamy
            "朦胧" -> R.string.soft_hazy
            else -> null
        }
    }

    /**
     * 获取本地化的柔光名称
     */
    @Composable
    fun getLocalizedSoftLight(softLight: String): String {
        val resId = getSoftLightResId(softLight)
        return if (resId != null) stringResource(resId) else softLight
    }

    /**
     * 获取本地化的柔光名称（Context 版本）
     */
    fun getLocalizedSoftLight(context: android.content.Context, softLight: String): String {
        val resId = getSoftLightResId(softLight)
        return if (resId != null) context.getString(resId) else softLight
    }

    /**
     * 获取暗角开关状态对应的资源 ID
     */
    fun getVignetteResId(vignette: String): Int? {
        return when (vignette) {
            "开" -> R.string.vignette_on
            "关" -> R.string.vignette_off
            else -> null
        }
    }

    /**
     * 获取本地化的暗角开关状态
     */
    @Composable
    fun getLocalizedVignette(vignette: String): String {
        val resId = getVignetteResId(vignette)
        return if (resId != null) stringResource(resId) else vignette
    }

    /**
     * 获取本地化的暗角开关状态（Context 版本）
     */
    fun getLocalizedVignette(context: android.content.Context, vignette: String): String {
        val resId = getVignetteResId(vignette)
        return if (resId != null) context.getString(resId) else vignette
    }

    /**
     * 获取拍摄模式对应的资源 ID
     */
    fun getModeResId(mode: String): Int? {
        return when (mode.lowercase()) {
            "auto" -> R.string.mode_auto
            "pro" -> R.string.mode_pro
            else -> null
        }
    }
    
    /**
     * 获取本地化的拍摄模式
     */
    @Composable
    fun getLocalizedMode(mode: String): String {
        val resId = getModeResId(mode)
        return if (resId != null) stringResource(resId) else mode
    }

    /**
     * 获取本地化的拍摄模式（Context 版本）
     */
    fun getLocalizedMode(context: android.content.Context, mode: String): String {
        val resId = getModeResId(mode)
        return if (resId != null) context.getString(resId) else mode
    }
}

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
     * 获取预设名称对应的资源 ID
     */
    fun getPresetNameResId(name: String): Int? {
        return when (name) {
            "富士胶片" -> R.string.preset_fuji_film
            "胶片感" -> R.string.preset_film_feel
            "童话" -> R.string.preset_fairy_tale
            "高对比黑白" -> R.string.preset_high_contrast_bw
            "理光绿" -> R.string.preset_ricoh_green
            "理光蓝" -> R.string.preset_ricoh_blue
            "蓝调时刻" -> R.string.preset_blue_hour
            "梦幻黑柔" -> R.string.preset_dreamy_blackmist
            "富士NC" -> R.string.preset_fuji_nc
            "人文" -> R.string.preset_humanities
            "清新人文" -> R.string.preset_fresh_humanities
            "氛围雪夜" -> R.string.preset_snowy_night
            "美味流芳" -> R.string.preset_delicious
            "手机徕卡" -> R.string.preset_mobile_leica
            "梦幻富士" -> R.string.preset_dreamy_fuji
            "哈苏浓郁" -> R.string.preset_hasselblad_rich
            "假日清新" -> R.string.preset_holiday_fresh
            "梦幻黑白" -> R.string.preset_dreamy_bw
            "美味梦境" -> R.string.preset_delicious_dream
            "蓝调通透" -> R.string.preset_blue_clear
            "晴天复古" -> R.string.preset_sunny_vintage
            "霓虹灯" -> R.string.preset_neon_lights
            "复古怀旧" -> R.string.preset_retro_nostalgia
            else -> null
        }
    }

    /**
     * 获取本地化的预设名称
     */
    @Composable
    fun getLocalizedPresetName(name: String): String {
        val resId = getPresetNameResId(name)
        if (resId != null) return stringResource(resId)
        return resolveStringComposable(name)
    }

    /**
     * 解析可能是资源引用的字符串
     * 如果字符串以 "@string/" 开头，尝试解析为资源 ID 并获取对应字符串
     * 否则直接返回原字符串
     */
    fun resolveString(context: android.content.Context, text: String): String {
        if (text.startsWith("@string/")) {
            val resName = text.substring(8)
            val resId = context.resources.getIdentifier(resName, "string", context.packageName)
            return if (resId != 0) context.getString(resId) else text
        }
        return text
    }

    /**
     * 解析可能是资源引用的字符串 (Composable)
     */
    @Composable
    fun resolveStringComposable(text: String): String {
        if (text.startsWith("@string/")) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val resName = text.substring(8)
            val resId = context.resources.getIdentifier(resName, "string", context.packageName)
            return if (resId != 0) stringResource(resId) else text
        }
        return text
    }

    /**
     * 获取本地化的预设名称（Context 版本）
     */
    fun getLocalizedPresetName(context: android.content.Context, name: String): String {
        val resId = getPresetNameResId(name)
        if (resId != null) return context.getString(resId)
        return resolveString(context, name)
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

    /**
     * 获取拍摄建议对应的资源 ID
     */
    fun getShootingTipsResId(presetName: String): Int? {
        return when (presetName) {
            "富士胶片" -> R.string.tips_fuji_film
            "胶片感" -> R.string.tips_film_feel
            "童话" -> R.string.tips_fairy_tale
            "高对比黑白" -> R.string.tips_high_contrast_bw
            "理光绿" -> R.string.tips_ricoh_green
            "理光蓝" -> R.string.tips_ricoh_blue
            "蓝调时刻" -> R.string.tips_blue_hour
            "梦幻黑柔" -> R.string.tips_dreamy_blackmist
            "富士NC" -> R.string.tips_fuji_nc
            "人文" -> R.string.tips_humanities
            "清新人文" -> R.string.tips_fresh_humanities
            "氛围雪夜" -> R.string.tips_snowy_night
            "美味流芳" -> R.string.tips_delicious
            "手机徕卡" -> R.string.tips_mobile_leica
            "梦幻富士" -> R.string.tips_dreamy_fuji
            "哈苏浓郁" -> R.string.tips_hasselblad_rich
            "假日清新" -> R.string.tips_holiday_fresh
            "梦幻黑白" -> R.string.tips_dreamy_bw
            "美味梦境" -> R.string.tips_delicious_dream
            "蓝调通透" -> R.string.tips_blue_clear
            "晴天复古" -> R.string.tips_sunny_vintage
            "霓虹灯" -> R.string.tips_neon_lights
            "复古怀旧" -> R.string.tips_retro_nostalgia
            else -> null
        }
    }

    /**
     * 获取本地化的拍摄建议
     */
    @Composable
    fun getLocalizedShootingTips(presetName: String, defaultTips: String?): String {
        val resId = getShootingTipsResId(presetName)
        return if (resId != null) stringResource(resId) else (defaultTips ?: "")
    }

    /**
     * 获取本地化的拍摄建议（Context 版本）
     */
    fun getLocalizedShootingTips(context: android.content.Context, presetName: String, defaultTips: String?): String {
        val resId = getShootingTipsResId(presetName)
        return if (resId != null) context.getString(resId) else (defaultTips ?: "")
    }
}

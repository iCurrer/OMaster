package com.silas.omaster.data.config

import com.silas.omaster.data.local.AppLanguage
import com.silas.omaster.data.local.FloatingWindowMode
import com.silas.omaster.data.local.UpdateChannel
import com.silas.omaster.ui.theme.BrandTheme

/**
 * 悬浮窗完整配置
 */
data class FloatingWindowConfig(
    val opacity: Int = 56,
    val mode: FloatingWindowMode = FloatingWindowMode.STANDARD
)

/**
 * UI 配置组合
 */
data class UiConfig(
    val theme: BrandTheme = BrandTheme.Hasselblad,
    val language: AppLanguage = AppLanguage.SYSTEM
)

/**
 * 系统配置组合
 */
data class SystemConfig(
    val updateChannel: UpdateChannel = UpdateChannel.GITEE,
    val defaultPresetUrl: String = SubscriptionConfig.DEFAULT_PRESET_URL,
    val realmePresetUrl: String = SubscriptionConfig.REALME_PRESET_URL
)

/**
 * 用户偏好配置组合
 */
data class UserPreferences(
    val vibrationEnabled: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val defaultStartTab: Int = 0
)

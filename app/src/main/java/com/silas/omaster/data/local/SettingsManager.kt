package com.silas.omaster.data.local

import android.content.Context
import android.content.SharedPreferences
import com.silas.omaster.ui.theme.BrandTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 更新渠道枚举
 */
enum class UpdateChannel {
    GITEE,    // 默认，国内访问快
    GITHUB    // GitHub，国际访问
}

/**
 * 悬浮窗模式枚举
 */
enum class FloatingWindowMode {
    STANDARD,   // 标准模式（卡片式）
    COMPACT     // 新版紧凑参数条
}

/**
 * 应用语言枚举
 */
enum class AppLanguage {
    SYSTEM,     // 跟随系统
    CHINESE,    // 简体中文
    ENGLISH     // English
}

@Deprecated(
    "使用 ConfigCenter 替代",
    ReplaceWith("ConfigCenter.getInstance(context)")
)
class SettingsManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()
        }

    private val _themeFlow: MutableStateFlow<BrandTheme>
    val themeFlow: StateFlow<BrandTheme>

    init {
        val themeId = prefs.getString(KEY_THEME_ID, BrandTheme.Hasselblad.id) ?: BrandTheme.Hasselblad.id
        _themeFlow = MutableStateFlow(BrandTheme.fromId(themeId))
        themeFlow = _themeFlow.asStateFlow()
    }

    var currentTheme: BrandTheme
        get() = _themeFlow.value
        set(value) {
            prefs.edit().putString(KEY_THEME_ID, value.id).apply()
            _themeFlow.value = value
        }

    // 悬浮窗透明度 (30-70%，默认56%)
    var floatingWindowOpacity: Int
        get() = prefs.getInt(KEY_FLOATING_WINDOW_OPACITY, 56)
        set(value) {
            prefs.edit().putInt(KEY_FLOATING_WINDOW_OPACITY, value.coerceIn(30, 70)).apply()
        }

    // 默认启动 Tab (0=全部, 1=收藏, 2=我的，默认0)
    var defaultStartTab: Int
        get() = prefs.getInt(KEY_DEFAULT_START_TAB, 0)
        set(value) {
            prefs.edit().putInt(KEY_DEFAULT_START_TAB, value.coerceIn(0, 2)).apply()
        }

    // 更新渠道（默认 Gitee）
    var updateChannel: UpdateChannel
        get() {
            val value = prefs.getString(KEY_UPDATE_CHANNEL, UpdateChannel.GITEE.name)
            return try {
                UpdateChannel.valueOf(value ?: UpdateChannel.GITEE.name)
            } catch (e: Exception) {
                UpdateChannel.GITEE
            }
        }
        set(value) {
            prefs.edit().putString(KEY_UPDATE_CHANNEL, value.name).apply()
        }

    // 友盟统计开关（默认开启，因为用户首次已同意隐私政策）
    var isAnalyticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ANALYTICS_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_ANALYTICS_ENABLED, value).apply()
        }

    // 悬浮窗模式（默认标准模式）
    var floatingWindowMode: FloatingWindowMode
        get() {
            val value = prefs.getString(KEY_FLOATING_WINDOW_MODE, FloatingWindowMode.STANDARD.name)
            return try {
                FloatingWindowMode.valueOf(value ?: FloatingWindowMode.STANDARD.name)
            } catch (e: Exception) {
                FloatingWindowMode.STANDARD
            }
        }
        set(value) {
            prefs.edit().putString(KEY_FLOATING_WINDOW_MODE, value.name).apply()
        }

    // 应用语言设置（默认跟随系统）
    var appLanguage: AppLanguage
        get() {
            val value = prefs.getString(KEY_APP_LANGUAGE, AppLanguage.SYSTEM.name)
            return try {
                AppLanguage.valueOf(value ?: AppLanguage.SYSTEM.name)
            } catch (e: Exception) {
                AppLanguage.SYSTEM
            }
        }
        set(value) {
            prefs.edit().putString(KEY_APP_LANGUAGE, value.name).apply()
        }

    companion object {
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_THEME_ID = "theme_id"
        private const val KEY_FLOATING_WINDOW_OPACITY = "floating_window_opacity"
        private const val KEY_DEFAULT_START_TAB = "default_start_tab"
        private const val KEY_UPDATE_CHANNEL = "update_channel"
        private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
        private const val KEY_FLOATING_WINDOW_MODE = "floating_window_mode"
        private const val KEY_APP_LANGUAGE = "app_language"

        @Volatile
        private var instance: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

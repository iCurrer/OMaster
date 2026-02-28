package com.silas.omaster.data.local

import android.content.Context
import android.content.SharedPreferences
import com.silas.omaster.ui.theme.BrandTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    companion object {
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_THEME_ID = "theme_id"
        private const val KEY_FLOATING_WINDOW_OPACITY = "floating_window_opacity"
        private const val KEY_DEFAULT_START_TAB = "default_start_tab"

        @Volatile
        private var instance: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

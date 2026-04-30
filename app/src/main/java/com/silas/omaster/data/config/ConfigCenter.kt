package com.silas.omaster.data.config

import android.content.Context
import android.content.SharedPreferences
import com.silas.omaster.data.local.AppLanguage
import com.silas.omaster.data.local.FloatingWindowMode
import com.silas.omaster.data.local.UpdateChannel
import com.silas.omaster.model.Subscription
import com.silas.omaster.ui.theme.BrandTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * 配置日志标签常量
 */
internal object ConfigLog {
    const val TAG = "OMasterConfig"
}

/**
 * 配置管理中心
 * 统一所有配置的访问入口，解决配置分散问题
 *
 * 【使用示例】
 * ```kotlin
 * // 获取实例
 * val config = ConfigCenter.getInstance(context)
 *
 * // 读取配置
 * val theme = config.currentTheme
 * val opacity = config.floatingWindowOpacity
 *
 * // 观察配置变化
 * config.themeFlow.collect { theme ->
 *     // 主题变化时自动触发
 * }
 *
 * // 修改配置
 * config.currentTheme = BrandTheme.Fuji
 * config.floatingWindowOpacity = 60
 * ```
 */
class ConfigCenter private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // 订阅配置子模块
    private val subscriptionConfig = SubscriptionConfig(appContext)

    init {
        // 执行数据迁移
        ConfigMigration.migrate(appContext, prefs)
    }

    // ═══════════════════════════════════════════════════════════
    // 用户配置 - 主题
    // ═══════════════════════════════════════════════════════════

    private val _themeFlow = MutableStateFlow(loadTheme())
    val themeFlow: StateFlow<BrandTheme> = _themeFlow.asStateFlow()

    var currentTheme: BrandTheme
        get() = _themeFlow.value
        set(value) {
            _themeFlow.value = value
            prefs.edit().putString(KEY_THEME_ID, value.id).apply()
        }

    private fun loadTheme(): BrandTheme {
        val themeId = prefs.getString(KEY_THEME_ID, BrandTheme.Hasselblad.id)
        return BrandTheme.fromId(themeId ?: BrandTheme.Hasselblad.id)
    }

    // ═══════════════════════════════════════════════════════════
    // 用户配置 - 深色模式
    // ═══════════════════════════════════════════════════════════

    private val _darkModeFlow = MutableStateFlow(loadDarkMode())
    val darkModeFlow: StateFlow<com.silas.omaster.data.local.DarkMode> = _darkModeFlow.asStateFlow()

    var darkMode: com.silas.omaster.data.local.DarkMode
        get() = _darkModeFlow.value
        set(value) {
            _darkModeFlow.value = value
            prefs.edit().putString(KEY_DARK_MODE, value.name).apply()
        }

    // 兼容旧版本的布尔值读取
    val isDarkMode: Boolean
        get() = when (_darkModeFlow.value) {
            com.silas.omaster.data.local.DarkMode.SYSTEM -> {
                // 根据系统当前主题判断
                val uiMode = appContext.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
            com.silas.omaster.data.local.DarkMode.LIGHT -> false
            com.silas.omaster.data.local.DarkMode.DARK -> true
        }

    private fun loadDarkMode(): com.silas.omaster.data.local.DarkMode {
        // 尝试读取新版本的字符串配置
        val modeName = prefs.getString(KEY_DARK_MODE, null)
        if (modeName != null) {
            return try {
                com.silas.omaster.data.local.DarkMode.valueOf(modeName)
            } catch (e: Exception) {
                com.silas.omaster.data.local.DarkMode.SYSTEM
            }
        }
        // 兼容旧版本的布尔值
        val oldValue = prefs.getBoolean(KEY_DARK_MODE_OLD, true)
        return if (oldValue) com.silas.omaster.data.local.DarkMode.DARK 
               else com.silas.omaster.data.local.DarkMode.LIGHT
    }

    // ═══════════════════════════════════════════════════════════
    // 用户配置 - 语言
    // ═══════════════════════════════════════════════════════════

    private val _languageFlow = MutableStateFlow(loadLanguage())
    val languageFlow: StateFlow<AppLanguage> = _languageFlow.asStateFlow()

    var appLanguage: AppLanguage
        get() = _languageFlow.value
        set(value) {
            _languageFlow.value = value
            prefs.edit().putString(KEY_APP_LANGUAGE, value.name).apply()
        }

    private fun loadLanguage(): AppLanguage {
        val value = prefs.getString(KEY_APP_LANGUAGE, AppLanguage.SYSTEM.name)
        return try {
            AppLanguage.valueOf(value ?: AppLanguage.SYSTEM.name)
        } catch (e: Exception) {
            AppLanguage.SYSTEM
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 用户配置 - 震动反馈
    // ═══════════════════════════════════════════════════════════

    private val _vibrationFlow = MutableStateFlow(loadVibration())
    val vibrationFlow: StateFlow<Boolean> = _vibrationFlow.asStateFlow()

    var isVibrationEnabled: Boolean
        get() = _vibrationFlow.value
        set(value) {
            _vibrationFlow.value = value
            prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()
        }

    private fun loadVibration(): Boolean {
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    // ═══════════════════════════════════════════════════════════
    // 用户配置 - 悬浮窗
    // ═══════════════════════════════════════════════════════════

    private val _opacityFlow = MutableStateFlow(loadOpacity())
    val floatingWindowOpacityFlow: StateFlow<Int> = _opacityFlow.asStateFlow()

    var floatingWindowOpacity: Int
        get() = _opacityFlow.value
        set(value) {
            val coerced = value.coerceIn(30, 70)
            _opacityFlow.value = coerced
            prefs.edit().putInt(KEY_FLOATING_WINDOW_OPACITY, coerced).apply()
        }

    private fun loadOpacity(): Int {
        return prefs.getInt(KEY_FLOATING_WINDOW_OPACITY, 56)
    }

    private val _floatingModeFlow = MutableStateFlow(loadFloatingMode())
    val floatingWindowModeFlow: StateFlow<FloatingWindowMode> = _floatingModeFlow.asStateFlow()

    var floatingWindowMode: FloatingWindowMode
        get() = _floatingModeFlow.value
        set(value) {
            _floatingModeFlow.value = value
            prefs.edit().putString(KEY_FLOATING_WINDOW_MODE, value.name).apply()
        }

    private fun loadFloatingMode(): FloatingWindowMode {
        val value = prefs.getString(KEY_FLOATING_WINDOW_MODE, FloatingWindowMode.COMPACT.name)
        return try {
            FloatingWindowMode.valueOf(value ?: FloatingWindowMode.COMPACT.name)
        } catch (e: Exception) {
            FloatingWindowMode.COMPACT
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 用户配置 - 默认启动 Tab
    // ═══════════════════════════════════════════════════════════

    private val _defaultTabFlow = MutableStateFlow(loadDefaultTab())
    val defaultStartTabFlow: StateFlow<Int> = _defaultTabFlow.asStateFlow()

    var defaultStartTab: Int
        get() = _defaultTabFlow.value
        set(value) {
            val coerced = value.coerceIn(0, 2)
            _defaultTabFlow.value = coerced
            prefs.edit().putInt(KEY_DEFAULT_START_TAB, coerced).apply()
        }

    private fun loadDefaultTab(): Int {
        return prefs.getInt(KEY_DEFAULT_START_TAB, 0)
    }

    // ═══════════════════════════════════════════════════════════
    // 用户配置 - 统计开关
    // ═══════════════════════════════════════════════════════════

    private val _analyticsFlow = MutableStateFlow(loadAnalytics())
    val analyticsEnabledFlow: StateFlow<Boolean> = _analyticsFlow.asStateFlow()

    var isAnalyticsEnabled: Boolean
        get() = _analyticsFlow.value
        set(value) {
            _analyticsFlow.value = value
            prefs.edit().putBoolean(KEY_ANALYTICS_ENABLED, value).apply()
        }

    /**
     * 是否启用高级 Glass 质感效果
     * true: 使用 6 层 Glass 效果（收藏按钮、导航栏胶囊）
     * false: 使用简化效果（删除按钮样式）
     */
    private val _premiumGlassFlow = MutableStateFlow(loadPremiumGlass())
    val premiumGlassFlow: StateFlow<Boolean> = _premiumGlassFlow.asStateFlow()

    var isPremiumGlassEnabled: Boolean
        get() = _premiumGlassFlow.value
        set(value) {
            _premiumGlassFlow.value = value
            prefs.edit().putBoolean(KEY_PREMIUM_GLASS_ENABLED, value).apply()
        }

    private fun loadPremiumGlass(): Boolean {
        return prefs.getBoolean(KEY_PREMIUM_GLASS_ENABLED, false)  // 默认关闭，用户需手动开启
    }

    private fun loadAnalytics(): Boolean {
        return prefs.getBoolean(KEY_ANALYTICS_ENABLED, true)
    }

    // ═══════════════════════════════════════════════════════════
    // 系统配置 - 更新渠道
    // ═══════════════════════════════════════════════════════════

    private val _updateChannelFlow = MutableStateFlow(loadUpdateChannel())
    val updateChannelFlow: StateFlow<UpdateChannel> = _updateChannelFlow.asStateFlow()

    var updateChannel: UpdateChannel
        get() = _updateChannelFlow.value
        set(value) {
            _updateChannelFlow.value = value
            prefs.edit().putString(KEY_UPDATE_CHANNEL, value.name).apply()
        }

    private fun loadUpdateChannel(): UpdateChannel {
        val value = prefs.getString(KEY_UPDATE_CHANNEL, UpdateChannel.GITEE.name)
        return try {
            UpdateChannel.valueOf(value ?: UpdateChannel.GITEE.name)
        } catch (e: Exception) {
            UpdateChannel.GITEE
        }
    }

    // ═══════════════════════════════════════════════════════════
    // 系统配置 - 自动检查更新
    // ═══════════════════════════════════════════════════════════

    private val _autoCheckUpdateFlow = MutableStateFlow(loadAutoCheckUpdate())
    val autoCheckUpdateFlow: StateFlow<Boolean> = _autoCheckUpdateFlow.asStateFlow()

    var isAutoCheckUpdateEnabled: Boolean
        get() = _autoCheckUpdateFlow.value
        set(value) {
            _autoCheckUpdateFlow.value = value
            prefs.edit().putBoolean(KEY_AUTO_CHECK_UPDATE, value).apply()
        }

    private fun loadAutoCheckUpdate(): Boolean {
        return prefs.getBoolean(KEY_AUTO_CHECK_UPDATE, true)  // 默认开启
    }

    // ═══════════════════════════════════════════════════════════
    // 订阅配置 - 委托给 SubscriptionConfig
    // ═══════════════════════════════════════════════════════════

    val subscriptionsFlow: StateFlow<List<Subscription>> = subscriptionConfig.subscriptionsFlow

    fun addSubscription(url: String, name: String = "", author: String = "", build: Int = 1) {
        subscriptionConfig.addSubscription(url, name, author, build)
    }

    fun removeSubscription(url: String) {
        subscriptionConfig.removeSubscription(url)
    }

    fun toggleSubscription(url: String) {
        subscriptionConfig.toggleSubscription(url)
    }

    fun updateSubscriptionStatus(
        url: String,
        presetCount: Int,
        lastUpdateTime: Long,
        name: String? = null,
        author: String? = null,
        build: Int? = null
    ) {
        subscriptionConfig.updateSubscriptionStatus(url, presetCount, lastUpdateTime, name, author, build)
    }

    fun updateSubscriptionUrl(oldUrl: String, newUrl: String) {
        subscriptionConfig.updateSubscriptionUrl(oldUrl, newUrl)
    }

    fun getSubscriptionFileName(url: String): String {
        return subscriptionConfig.getFileNameForUrl(url)
    }

    // ═══════════════════════════════════════════════════════════
    // 组合配置 Flow
    // ═══════════════════════════════════════════════════════════

    /**
     * 悬浮窗完整配置（透明度 + 模式）
     */
    val floatingWindowConfigFlow: Flow<FloatingWindowConfig> = combine(
        floatingWindowOpacityFlow,
        floatingWindowModeFlow
    ) { opacity, mode ->
        FloatingWindowConfig(opacity, mode)
    }

    /**
     * UI 配置组合（主题 + 语言 + 深色模式）
     */
    val uiConfigFlow: Flow<UiConfig> = combine(
        themeFlow,
        languageFlow,
        darkModeFlow
    ) { theme, language, darkMode ->
        UiConfig(theme, language, darkMode == com.silas.omaster.data.local.DarkMode.DARK ||
                (darkMode == com.silas.omaster.data.local.DarkMode.SYSTEM && isDarkMode))
    }

    /**
     * 用户偏好配置组合
     */
    val userPreferencesFlow: Flow<UserPreferences> = combine(
        vibrationFlow,
        analyticsEnabledFlow,
        defaultStartTabFlow
    ) { vibration, analytics, tab ->
        UserPreferences(vibration, analytics, tab)
    }

    /**
     * 系统配置组合
     */
    val systemConfigFlow: Flow<SystemConfig> = updateChannelFlow.map { channel ->
        SystemConfig(
            updateChannel = channel,
            defaultPresetUrl = SubscriptionConfig.DEFAULT_PRESET_URL,
            realmePresetUrl = SubscriptionConfig.REALME_PRESET_URL
        )
    }

    // ═══════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════

    companion object {
        private const val PREFS_NAME = "omaster_config_center"

        // UserConfig Keys
        private const val KEY_THEME_ID = "theme_id"
        private const val KEY_DARK_MODE = "dark_mode_v2"
        private const val KEY_DARK_MODE_OLD = "dark_mode"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_FLOATING_WINDOW_OPACITY = "floating_window_opacity"
        private const val KEY_FLOATING_WINDOW_MODE = "floating_window_mode"
        private const val KEY_DEFAULT_START_TAB = "default_start_tab"
        private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
        private const val KEY_PREMIUM_GLASS_ENABLED = "premium_glass_enabled"

        // SystemConfig Keys
        private const val KEY_UPDATE_CHANNEL = "update_channel"
        private const val KEY_AUTO_CHECK_UPDATE = "auto_check_update"

        @Volatile
        private var INSTANCE: ConfigCenter? = null

        /**
         * 获取 ConfigCenter 单例实例
         * @param context Application 或 Activity Context
         * @return ConfigCenter 实例
         */
        fun getInstance(context: Context): ConfigCenter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConfigCenter(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        /**
         * 仅用于测试：重置单例
         */
        internal fun resetInstance() {
            INSTANCE = null
        }
    }
}

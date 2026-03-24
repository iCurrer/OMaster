package com.silas.omaster.data.config

import android.content.Context
import android.content.SharedPreferences
import com.silas.omaster.data.local.UpdateChannel

/**
 * 配置数据迁移工具
 * 从旧版本管理器迁移数据到 ConfigCenter
 */
object ConfigMigration {

    private const val TAG = "ConfigMigration"

    /**
     * 执行数据迁移
     * @return true 表示执行了迁移，false 表示无需迁移
     */
    fun migrate(context: Context, targetPrefs: SharedPreferences): Boolean {
        if (targetPrefs.getBoolean(KEY_MIGRATION_DONE, false)) {
            android.util.Log.d(TAG, "Migration already done, skipping")
            return false
        }

        android.util.Log.d(TAG, "Starting config migration...")

        val editor = targetPrefs.edit()
        var hasData = false

        // 1. 迁移 SettingsManager 数据
        val settingsPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        if (settingsPrefs.all.isNotEmpty()) {
            android.util.Log.d(TAG, "Migrating SettingsManager data...")

            // 主题
            settingsPrefs.getString("theme_id", null)?.let {
                editor.putString("theme_id", it)
                hasData = true
            }

            // 语言
            settingsPrefs.getString("app_language", null)?.let {
                editor.putString("app_language", it)
                hasData = true
            }

            // 震动
            if (settingsPrefs.contains("vibration_enabled")) {
                editor.putBoolean("vibration_enabled",
                    settingsPrefs.getBoolean("vibration_enabled", true))
                hasData = true
            }

            // 悬浮窗透明度
            if (settingsPrefs.contains("floating_window_opacity")) {
                editor.putInt("floating_window_opacity",
                    settingsPrefs.getInt("floating_window_opacity", 56))
                hasData = true
            }

            // 悬浮窗模式
            settingsPrefs.getString("floating_window_mode", null)?.let {
                editor.putString("floating_window_mode", it)
                hasData = true
            }

            // 默认启动 Tab
            if (settingsPrefs.contains("default_start_tab")) {
                editor.putInt("default_start_tab",
                    settingsPrefs.getInt("default_start_tab", 0))
                hasData = true
            }

            // 更新渠道
            settingsPrefs.getString("update_channel", null)?.let {
                editor.putString("update_channel", it)
                hasData = true
            }

            // 统计开关
            if (settingsPrefs.contains("analytics_enabled")) {
                editor.putBoolean("analytics_enabled",
                    settingsPrefs.getBoolean("analytics_enabled", true))
                hasData = true
            }
        }

        // 2. 迁移 UpdateConfigManager 数据
        val updatePrefs = context.getSharedPreferences("omaster_update_prefs", Context.MODE_PRIVATE)
        if (updatePrefs.all.isNotEmpty()) {
            android.util.Log.d(TAG, "Migrating UpdateConfigManager data...")

            updatePrefs.getString("preset_update_url", null)?.let { url ->
                // 推断更新渠道
                val channel = when {
                    url.contains("github", ignoreCase = true) -> UpdateChannel.GITHUB
                    else -> UpdateChannel.GITEE
                }
                editor.putString("update_channel", channel.name)
                hasData = true
            }
        }

        // 标记迁移完成
        editor.putBoolean(KEY_MIGRATION_DONE, true)
        editor.apply()

        android.util.Log.d(TAG, "Migration completed. Has data: $hasData")
        return hasData
    }

    /**
     * 清理旧版数据（可选，建议在确认迁移成功后调用）
     */
    fun cleanupLegacyData(context: Context) {
        android.util.Log.d(TAG, "Cleaning up legacy data...")

        // 删除旧版 SharedPreferences
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().clear().apply()
        context.getSharedPreferences("omaster_update_prefs", Context.MODE_PRIVATE)
            .edit().clear().apply()

        android.util.Log.d(TAG, "Legacy data cleaned up")
    }

    private const val KEY_MIGRATION_DONE = "config_migration_done"
}

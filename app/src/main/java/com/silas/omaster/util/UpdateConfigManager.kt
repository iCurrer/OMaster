package com.silas.omaster.util

import android.content.Context

@Deprecated(
    "使用 ConfigCenter 替代",
    ReplaceWith("ConfigCenter.getInstance(context).updateChannel")
)
object UpdateConfigManager {
    private const val PREFS_NAME = "omaster_update_prefs"
    private const val KEY_PRESET_URL = "preset_update_url"

    const val DEFAULT_PRESET_URL = "https://cdn.jsdelivr.net/gh/fengyec2/OMaster-Community@main/presets/v2/oppo.json"
    const val REALME_PRESET_URL = "https://cdn.jsdelivr.net/gh/fengyec2/OMaster-Community@main/presets/v2/realme.json"

    fun getPresetUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PRESET_URL, DEFAULT_PRESET_URL) ?: DEFAULT_PRESET_URL
    }

    fun setPresetUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PRESET_URL, url).apply()
        android.util.Log.d("UpdateConfigManager", "Saved preset update URL: $url")
    }
}

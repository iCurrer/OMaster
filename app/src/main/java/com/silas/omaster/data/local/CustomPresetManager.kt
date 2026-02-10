package com.silas.omaster.data.local

import android.content.Context
import android.content.SharedPreferences
import com.silas.omaster.model.MasterPreset
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.UUID

/**
 * 自定义预设管理器
 * 使用 SharedPreferences 存储用户创建的预设
 */
class CustomPresetManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    private val _customPresetsFlow = MutableStateFlow<List<MasterPreset>>(emptyList())
    val customPresetsFlow: StateFlow<List<MasterPreset>> = _customPresetsFlow

    init {
        loadCustomPresets()
    }

    /**
     * 获取所有自定义预设
     */
    fun getCustomPresets(): List<MasterPreset> {
        return _customPresetsFlow.value
    }

    /**
     * 添加自定义预设
     */
    fun addCustomPreset(preset: MasterPreset) {
        val presets = getCustomPresets().toMutableList()
        val newPreset = preset.copy(
            id = preset.id ?: UUID.randomUUID().toString(),
            isCustom = true
        )
        presets.add(0, newPreset) // 添加到开头
        saveCustomPresets(presets)
    }

    /**
     * 更新自定义预设
     */
    fun updateCustomPreset(preset: MasterPreset) {
        val presets = getCustomPresets().toMutableList()
        val index = presets.indexOfFirst { it.id == preset.id }
        if (index != -1) {
            presets[index] = preset.copy(isCustom = true)
            saveCustomPresets(presets)
        }
    }

    /**
     * 删除自定义预设
     */
    fun deleteCustomPreset(context: Context, presetId: String) {
        // 先获取要删除的预设，以便删除其图片文件
        val presetToDelete = getCustomPresets().find { it.id == presetId }
        
        // 从列表中移除
        val presets = getCustomPresets().filter { it.id != presetId }
        saveCustomPresets(presets)
        
        // 删除关联的图片文件
        presetToDelete?.let { preset ->
            deletePresetImages(context, preset)
        }
    }
    
    /**
     * 删除预设关联的图片文件
     */
    private fun deletePresetImages(context: Context, preset: MasterPreset) {
        // 删除封面图片
        preset.coverPath?.let { coverPath ->
            val coverFile = File(context.filesDir, coverPath)
            if (coverFile.exists()) {
                val deleted = coverFile.delete()
                android.util.Log.d("CustomPresetManager", "Deleted cover image: $coverPath, success: $deleted")
            }
        }
        
        // 删除画廊图片
        preset.galleryImages?.forEach { galleryPath ->
            val galleryFile = File(context.filesDir, galleryPath)
            if (galleryFile.exists()) {
                val deleted = galleryFile.delete()
                android.util.Log.d("CustomPresetManager", "Deleted gallery image: $galleryPath, success: $deleted")
            }
        }
    }

    /**
     * 根据 ID 获取预设
     */
    fun getPresetById(presetId: String): MasterPreset? {
        return getCustomPresets().find { it.id == presetId }
    }

    private fun loadCustomPresets() {
        val json = prefs.getString(KEY_CUSTOM_PRESETS, null)
        val presets = if (json != null) {
            try {
                val type = object : TypeToken<List<MasterPreset>>() {}.type
                gson.fromJson<List<MasterPreset>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("CustomPresetManager", "加载自定义预设失败", e)
                emptyList()
            }
        } else {
            emptyList()
        }
        _customPresetsFlow.value = presets
    }

    private fun saveCustomPresets(presets: List<MasterPreset>) {
        val json = gson.toJson(presets)
        prefs.edit().putString(KEY_CUSTOM_PRESETS, json).apply()
        _customPresetsFlow.value = presets
    }

    companion object {
        private const val PREFS_NAME = "omaster_custom_presets"
        private const val KEY_CUSTOM_PRESETS = "custom_presets"

        @Volatile
        private var INSTANCE: CustomPresetManager? = null

        fun getInstance(context: Context): CustomPresetManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CustomPresetManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}

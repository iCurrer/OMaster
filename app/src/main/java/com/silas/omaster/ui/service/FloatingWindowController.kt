package com.silas.omaster.ui.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.silas.omaster.model.MasterPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 悬浮窗控制器
 * 管理悬浮窗状态和预设切换
 */
class FloatingWindowController private constructor(private val context: Context) {

    private val _currentPreset = MutableStateFlow<MasterPreset?>(null)
    val currentPreset: StateFlow<MasterPreset?> = _currentPreset.asStateFlow()

    private var presetList: List<MasterPreset> = emptyList()
    private var currentIndex: Int = 0

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == FloatingWindowService.ACTION_SWITCH_PRESET) {
                val direction = intent.getStringExtra(FloatingWindowService.EXTRA_SWITCH_DIRECTION)
                handlePresetSwitch(direction)
            }
        }
    }

    /**
     * 注册广播接收器
     */
    fun register() {
        val filter = IntentFilter(FloatingWindowService.ACTION_SWITCH_PRESET)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(broadcastReceiver, filter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 注销广播接收器
     */
    fun unregister() {
        try {
            context.unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 显示悬浮窗
     */
    fun showFloatingWindow(preset: MasterPreset, presets: List<MasterPreset> = emptyList()) {
        // 如果没有传入预设列表，使用已保存的列表
        presetList = if (presets.isNotEmpty()) presets else presetList
        currentIndex = presetList.indexOfFirst { it.id == preset.id }.coerceAtLeast(0)
        _currentPreset.value = preset

        FloatingWindowService.show(context, preset, currentIndex, presetList.map { it.id ?: "" })
    }

    /**
     * 设置预设列表（用于悬浮窗切换）
     */
    fun setPresetList(presets: List<MasterPreset>) {
        presetList = presets
    }

    /**
     * 隐藏悬浮窗
     */
    fun hideFloatingWindow() {
        FloatingWindowService.hide(context)
    }

    /**
     * 处理预设切换
     */
    private fun handlePresetSwitch(direction: String?) {
        if (presetList.isEmpty()) return

        val newIndex = when (direction) {
            "prev" -> (currentIndex - 1 + presetList.size) % presetList.size
            "next" -> (currentIndex + 1) % presetList.size
            else -> return
        }

        currentIndex = newIndex
        val newPreset = presetList[newIndex]
        _currentPreset.value = newPreset

        // 使用 update 方法更新悬浮窗内容（避免闪动）
        FloatingWindowService.update(context, newPreset, currentIndex, presetList.map { it.id ?: "" })
    }

    companion object {
        @Volatile
        private var INSTANCE: FloatingWindowController? = null

        fun getInstance(context: Context): FloatingWindowController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FloatingWindowController(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}

package com.silas.omaster.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.silas.omaster.data.repository.PresetRepository
import com.silas.omaster.model.MasterPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

/**
 * 详情页 ViewModel
 * 管理预设详情和收藏状态
 *
 * 修复：
 * 1. 使用 Job 管理加载任务，快速切换时取消旧任务
 * 2. 添加加载状态标识，避免竞态条件
 */
class DetailViewModel(
    private val repository: PresetRepository
) : ViewModel() {

    // 当前预设
    private val _preset = MutableStateFlow<MasterPreset?>(null)
    val preset: StateFlow<MasterPreset?> = _preset.asStateFlow()

    // 收藏状态
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 当前预设 ID
    private var currentPresetId: String? = null

    // 用于管理加载任务的 Job
    private var loadJob: Job? = null

    /**
     * 加载预设数据
     * 修复：取消之前的加载任务，避免竞态条件
     */
    fun loadPreset(presetId: String) {
        // 如果正在加载同一个预设，跳过
        if (presetId == currentPresetId && _preset.value != null) {
            return
        }

        // 取消之前的加载任务
        loadJob?.cancel()
        currentPresetId = presetId

        android.util.Log.d("DetailViewModel", "Loading preset with id: $presetId")

        loadJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val presetData = repository.getPresetById(presetId)
                // 检查是否仍然是当前要加载的预设（可能被取消了）
                if (presetId == currentPresetId) {
                    android.util.Log.d("DetailViewModel", "Loaded preset: ${presetData?.name}, id: ${presetData?.id}")
                    _preset.value = presetData
                    _isFavorite.value = presetData?.isFavorite ?: false
                }
            } catch (e: Exception) {
                android.util.Log.e("DetailViewModel", "Error loading preset: $presetId", e)
                if (presetId == currentPresetId) {
                    _preset.value = null
                    _isFavorite.value = false
                }
            } finally {
                if (presetId == currentPresetId) {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite() {
        val id = currentPresetId ?: return
        viewModelScope.launch {
            try {
                val isNowFavorite = repository.toggleFavorite(id)
                _isFavorite.value = isNowFavorite
                // 更新预设数据
                _preset.value = _preset.value?.copy(isFavorite = isNowFavorite)
            } catch (e: Exception) {
                android.util.Log.e("DetailViewModel", "Error toggling favorite: $id", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // 清理时取消加载任务
        loadJob?.cancel()
    }
}

/**
 * DetailViewModel 工厂
 */
class DetailViewModelFactory(
    private val repository: PresetRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

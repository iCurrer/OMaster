package com.example.omaster.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.omaster.data.repository.PresetRepository
import com.example.omaster.model.MasterPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 详情页 ViewModel
 * 管理预设详情和收藏状态
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

    // 当前预设 ID
    private var currentPresetId: String? = null

    /**
     * 加载预设数据
     */
    fun loadPreset(presetId: String) {
        currentPresetId = presetId
        android.util.Log.d("DetailViewModel", "Loading preset with id: $presetId")
        viewModelScope.launch {
            val presetData = repository.getPresetById(presetId)
            android.util.Log.d("DetailViewModel", "Loaded preset: ${presetData?.name}, id: ${presetData?.id}")
            _preset.value = presetData
            _isFavorite.value = presetData?.isFavorite ?: false
        }
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite() {
        val id = currentPresetId ?: return
        viewModelScope.launch {
            val isNowFavorite = repository.toggleFavorite(id)
            _isFavorite.value = isNowFavorite
            // 更新预设数据
            _preset.value = _preset.value?.copy(isFavorite = isNowFavorite)
        }
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

package com.silas.omaster.ui.frame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.silas.omaster.util.ColorExtractor
import com.silas.omaster.util.DominantColorResult
import com.silas.omaster.util.ExifReader
import com.silas.omaster.util.FrameRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FrameViewModel(
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(FrameState())
    val state: StateFlow<FrameState> = _state.asStateFlow()

    private var extractJob: Job? = null
    private var renderJob: Job? = null

    fun loadImage(uri: Uri) {
        extractJob?.cancel()
        _state.value = _state.value.copy(imageUri = uri, isLoading = true, error = null)

        extractJob = viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) { decodeSampledBitmap(uri) }
                val exifInfo = ExifReader.read(context, uri)
                val colors = ColorExtractor.extract(bitmap)

                _state.value = _state.value.copy(
                    sourceBitmap = bitmap,
                    colors = colors,
                    dateTime = exifInfo.dateTime,
                    isLoading = false
                )
                renderInternal()
            } catch (e: Exception) {
                android.util.Log.e("FrameViewModel", "Failed to load image", e)
                _state.value = _state.value.copy(isLoading = false, error = "图片加载失败，请重试")
            }
        }
    }

    fun updateTitle(title: String) {
        _state.value = _state.value.copy(dateTime = title)
        renderInternal()
    }

    fun toggleRoundedCorners(enabled: Boolean) {
        _state.value = _state.value.copy(useRoundedCorners = enabled)
        renderInternal()
    }

    private fun renderInternal() {
        val s = _state.value
        if (s.sourceBitmap == null || s.colors == null) return

        renderJob?.cancel()
        renderJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                val result = FrameRenderer.render(
                    FrameRenderer.Params(
                        source = s.sourceBitmap,
                        dominantColor = s.colors.dominant,
                        textColor = s.colors.textColor,
                        title = s.dateTime ?: "",
                        useRoundedCorners = s.useRoundedCorners
                    )
                )
                _state.value = _state.value.copy(renderedBitmap = result)
            } catch (e: Exception) {
                android.util.Log.e("FrameViewModel", "Failed to render", e)
                _state.value = _state.value.copy(error = "渲染失败，请重试")
            }
        }
    }

    private suspend fun decodeSampledBitmap(uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { s -> BitmapFactory.decodeStream(s, null, options) }
        options.apply {
            inSampleSize = calculateInSampleSize(options, 1080, 1920)
            inJustDecodeBounds = false
        }
        context.contentResolver.openInputStream(uri)?.use { s ->
            BitmapFactory.decodeStream(s, null, options)
        } ?: throw IllegalStateException("无法读取图片")
    }

    private fun calculateInSampleSize(o: BitmapFactory.Options, rw: Int, rh: Int): Int {
        var s = 1
        while (o.outHeight / s > rh || o.outWidth / s > rw) s *= 2
        return s.coerceAtLeast(1)
    }

    override fun onCleared() {
        super.onCleared()
        extractJob?.cancel()
        renderJob?.cancel()
    }
}

data class FrameState(
    val imageUri: Uri? = null,
    val sourceBitmap: Bitmap? = null,
    val colors: DominantColorResult? = null,
    val renderedBitmap: Bitmap? = null,
    val dateTime: String? = null,
    val useRoundedCorners: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

class FrameViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FrameViewModel(context) as T
}

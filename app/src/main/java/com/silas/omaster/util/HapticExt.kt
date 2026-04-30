package com.silas.omaster.util

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 全局震感开关
 * 日后可在设置中修改
 */
object HapticSettings {
    var enabled: Boolean = true
}

/**
 * 执行震感反馈
 */
fun HapticFeedback.perform(type: HapticFeedbackType) {
    if (HapticSettings.enabled) {
        performHapticFeedback(type)
    }
}

/**
 * 带震感反馈的点击
 */
fun Modifier.hapticClickable(
    type: HapticFeedbackType = HapticFeedbackType.TextHandleMove,
    enabled: Boolean = true,
    onClick: () -> Unit
) = composed {
    val haptic = LocalHapticFeedback.current
    clickable(enabled = enabled) {
        haptic.perform(type)
        onClick()
    }
}

@Composable
fun rememberScrollHaptics(scrollState: ScrollState) {
    val haptic = LocalHapticFeedback.current
    var hasHapticAtTop by remember { mutableStateOf(false) }
    var hasHapticAtBottom by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState.value) {
        val currentValue = scrollState.value
        val maxValue = scrollState.maxValue

        if (currentValue == 0 && !hasHapticAtTop) {
            haptic.perform(HapticFeedbackType.TextHandleMove)
            hasHapticAtTop = true
            hasHapticAtBottom = false
        } else if (maxValue > 0 && currentValue >= maxValue && !hasHapticAtBottom) {
            haptic.perform(HapticFeedbackType.TextHandleMove)
            hasHapticAtBottom = true
            hasHapticAtTop = false
        } else if (currentValue > 0 && currentValue < maxValue) {
            hasHapticAtTop = false
            hasHapticAtBottom = false
        }
    }
}

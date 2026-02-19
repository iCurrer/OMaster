package com.silas.omaster.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset

/**
 * 全局动画配置
 * 统一管理应用内所有动画规格，确保一致性和性能
 */
object AnimationSpecs {

    /**
     * 快速动画 - 用于微交互（按钮点击、图标变化等）
     * 时长：150ms
     */
    val FastTween = tween<Float>(
        durationMillis = 150,
        easing = FastOutSlowInEasing
    )

    /**
     * 标准动画 - 用于一般过渡（页面切换、内容显示等）
     * 时长：250ms
     */
    val NormalTween = tween<Float>(
        durationMillis = 250,
        easing = FastOutSlowInEasing
    )

    /**
     * 慢速动画 - 用于强调动画（卡片入场、重要提示等）
     * 时长：400ms
     */
    val SlowTween = tween<Float>(
        durationMillis = 400,
        easing = FastOutSlowInEasing
    )

    /**
     * 列表项入场动画 - 轻量级，适合大量列表项
     * 使用较硬的 spring 减少计算量
     */
    val ListItemSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
        visibilityThreshold = 0.01f
    )

    /**
     * 卡片弹性动画 - 用于卡片等需要弹性的元素
     * 优化：提高刚度，减少拖沓感
     */
    val CardSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = 0.001f
    )

    /**
     * 淡入动画规格
     */
    val FadeInSpec = tween<Float>(
        durationMillis = 200,
        easing = LinearOutSlowInEasing
    )

    /**
     * 淡出动画规格
     */
    val FadeOutSpec = tween<Float>(
        durationMillis = 150,
        easing = FastOutLinearInEasing
    )

    /**
     * 滑动动画规格
     */
    val SlideSpec = tween<Int>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )

    /**
     * 缩放动画规格
     */
    val ScaleSpec = tween<Float>(
        durationMillis = 200,
        easing = FastOutSlowInEasing
    )

    /**
     * 列表项错开延迟基准值
     * 优化：从 50ms 减少到 20ms，提升加载流畅度
     */
    const val StaggerDelayMillis = 20

    /**
     * 列表项最大延迟
     * 优化：从 300ms 减少到 150ms，提升加载流畅度
     */
    const val MaxStaggerDelayMillis = 150

    /**
     * 自动播放间隔
     */
    const val AutoPlayIntervalMillis = 3000L

    /**
     * 页面切换动画时长
     */
    const val PageTransitionMillis = 250
}

/**
 * 记住动画状态的便捷函数
 * 避免在重组时重复创建 Animatable
 */
@Composable
fun rememberAnimatable(initialValue: Float = 0f): Animatable<Float, AnimationVector1D> {
    return remember { Animatable(initialValue) }
}

/**
 * 计算列表项错开延迟
 * @param index 列表项索引
 * @param visibleStartIndex 可见区域起始索引
 * @return 延迟毫秒数
 */
fun calculateStaggerDelay(index: Int, visibleStartIndex: Int): Int {
    val relativeIndex = (index - visibleStartIndex).coerceAtLeast(0)
    return (relativeIndex * AnimationSpecs.StaggerDelayMillis)
        .coerceAtMost(AnimationSpecs.MaxStaggerDelayMillis)
}

/**
 * 列表项动画放置规格
 * 用于 LazyColumn/LazyRow 的 animateItemPlacement
 */
val ListItemPlacementSpec: SpringSpec<IntOffset> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow,
    visibilityThreshold = IntOffset.VisibilityThreshold
)

/**
 * 列表项淡入规格
 * 用于 LazyColumn/LazyRow 的 animateItemFadeIn
 */
val ListItemFadeInSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium,
    visibilityThreshold = 0.01f
)

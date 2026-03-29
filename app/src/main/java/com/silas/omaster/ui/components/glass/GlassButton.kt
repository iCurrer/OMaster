package com.silas.omaster.ui.components.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.silas.omaster.ui.theme.GlassColors

/**
 * 统一 Glass 按钮组件
 * 
 * 支持两种模式：
 * - 高级模式 (isPremium = true): 6 层 Glassmorphism 效果
 *   1. 光晕层 - 径向渐变外发光
 *   2. 玻璃主体层 - 半透明背景
 *   3. 上下边缘发光层 - 垂直渐变边框
 *   4. 左右内阴影层 - 水平渐变边框
 *   5. 顶部高光层 - 垂直渐变反光
 *   6. 内容层 - 自定义内容
 * 
 * - 普通模式 (isPremium = false): 2 层简化效果
 *   1. 玻璃主体层 - 半透明背景
 *   2. 边框层 - 纯色边框
 *
 * @param isActive 是否为激活状态（影响发光颜色强度）
 * @param glowColor 发光颜色（默认主题色）
 * @param isPremium 是否启用高级 Glass 质感（默认 true）
 * @param size 按钮尺寸（正方形）
 * @param width 自定义宽度（非 null 时覆盖 size）
 * @param height 自定义高度（非 null 时覆盖 size）
 * @param config 玻璃效果配置
 * @param scalePressed 按压时缩放比例
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param content 按钮内容（通常为 Icon）
 */
@Composable
fun GlassButton(
    isActive: Boolean,
    glowColor: Color,
    isPremium: Boolean = true,
    size: Dp = 36.dp,
    width: Dp? = null,
    height: Dp? = null,
    config: GlassEffectConfig = GlassEffectConfig.DefaultButton,
    scalePressed: Float = 0.92f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val targetWidth = width ?: size
    val targetHeight = height ?: size
    
    Box(
        modifier = modifier
            .width(targetWidth)
            .height(targetHeight)
            .scale(if (isPressed) scalePressed else 1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        val cornerShape = RoundedCornerShape(config.cornerRadius)
        
        if (isPremium) {
            // ========== 高级模式：6 层 Glass 效果 ==========
            
            // Layer 1: 光晕层 - 柔和外发光
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = if (isActive) config.haloAlphaFavorite else config.haloAlphaNormal),
                                glowColor.copy(alpha = if (isActive) config.haloAlphaFavoriteInner else config.haloAlphaNormalInner),
                                Color.Transparent
                            ),
                            center = Offset(0.5f, 0.5f),
                            radius = config.haloRadius
                        ),
                        shape = cornerShape
                    )
            )
            
            // Layer 2: 玻璃主体层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = if (isActive)
                            glowColor.copy(alpha = config.bodyAlphaFavorite)
                        else
                            Color.White.copy(alpha = config.bodyAlphaNormal),
                        shape = cornerShape
                    )
            )
            
            // Layer 3: 上下边缘发光层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cornerShape)
                    .border(
                        width = config.borderWidth,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                if (isActive) glowColor.copy(alpha = config.topGlowAlphaFavorite) 
                                else Color.White.copy(alpha = config.topGlowAlphaNormal),
                                Color.Transparent,
                                if (isActive) glowColor.copy(alpha = config.bottomGlowAlphaFavorite) 
                                else Color.White.copy(alpha = config.bottomGlowAlphaNormal)
                            )
                        ),
                        shape = cornerShape
                    )
            )
            
            // Layer 4: 左右内阴影层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cornerShape)
                    .border(
                        width = config.innerShadowWidth,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = config.innerShadowAlpha),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = config.innerShadowAlpha)
                            )
                        ),
                        shape = cornerShape
                    )
            )
            
            // Layer 5: 顶部高光层 - 贴合圆弧
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cornerShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = config.highlightAlpha),
                                Color.White.copy(alpha = config.highlightMidAlpha),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = config.highlightEndY
                        )
                    )
            )
            
            // Layer 6: 内容层
            content()
            
        } else {
            // ========== 普通模式：2 层简化效果 ==========
            
            // Layer 1: 玻璃主体层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = if (isActive)
                            glowColor.copy(alpha = 0.15f)
                        else
                            Color.White.copy(alpha = 0.1f),
                        shape = cornerShape
                    )
            )
            
            // Layer 2: 边框层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cornerShape)
                    .border(
                        width = 0.5.dp,
                        color = if (isActive)
                            glowColor.copy(alpha = 0.5f)
                        else
                            GlassColors.BorderOuter,
                        shape = cornerShape
                    )
            )
            
            // 内容层
            content()
        }
    }
}

/**
 * Glass 按钮 - 简化调用版本
 * 适用于普通状态（非激活）按钮
 */
@Composable
fun GlassButton(
    glowColor: Color,
    isPremium: Boolean = true,
    size: Dp = 36.dp,
    config: GlassEffectConfig = GlassEffectConfig.DefaultButton,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassButton(
        isActive = false,
        glowColor = glowColor,
        isPremium = isPremium,
        size = size,
        config = config,
        onClick = onClick,
        modifier = modifier,
        content = content
    )
}

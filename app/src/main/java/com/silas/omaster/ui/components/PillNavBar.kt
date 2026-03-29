package com.silas.omaster.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.silas.omaster.util.perform
import com.silas.omaster.ui.theme.GlassColors
import com.silas.omaster.ui.components.glass.GlassEffectConfig
import com.silas.omaster.ui.components.glass.GlassButton

private val NavBarBackground = Color(0xFF1A1A1A)
private val NavBarBorder = Color(0xFF2A2A2A)

data class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun PillNavBar(
    visible: Boolean,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    usePremiumGlass: Boolean = true
) {
    val navItems = listOf(
        NavItem("home", stringResource(R.string.nav_home), Icons.Default.Home),
        NavItem("subscription", stringResource(R.string.nav_subscription), Icons.Default.RssFeed),
        NavItem("about", stringResource(R.string.nav_about), Icons.Default.Info)
    )

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            // 外层阴影效果
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = Color.Black.copy(alpha = 0.5f),
                        spotColor = Color.Black.copy(alpha = 0.8f)
                    )
            ) {
                // 磨砂玻璃背景层
                Box(
                    modifier = Modifier
                        .width(260.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    NavBarBackground.copy(alpha = 0.85f),
                                    NavBarBackground.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // 顶部高光线条
                Box(
                    modifier = Modifier
                        .width(260.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // 边框
                Box(
                    modifier = Modifier
                        .width(260.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    NavBarBorder.copy(alpha = 0.5f),
                                    NavBarBorder.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .padding(1.dp)
                ) {
                    // 内部背景
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .clip(RoundedCornerShape(31.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        NavBarBackground.copy(alpha = 0.9f),
                                        NavBarBackground.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                }

                // 导航项
                Box(
                    modifier = Modifier
                        .width(260.dp)
                        .height(64.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    // 滑动选中胶囊背景
                    val selectedIndex = navItems.indexOfFirst { it.route == currentRoute }
                    val itemWidth = 244f / 3f // (260dp - 16dp padding) / 3 items
                    val capsuleOffset by animateFloatAsState(
                        targetValue = selectedIndex * itemWidth,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "capsuleOffset"
                    )

                    // 选中胶囊背景 - 统一使用 GlassButton 组件
                    if (selectedIndex >= 0) {
                        GlassButton(
                            isActive = true,
                            glowColor = MaterialTheme.colorScheme.primary,
                            isPremium = usePremiumGlass,
                            width = 80.dp,
                            height = 48.dp,
                            config = GlassEffectConfig.NavigationCapsule,
                            modifier = Modifier
                                .offset(x = capsuleOffset.dp),
                            onClick = { }
                        ) {
                            // 内容为空，只需要背景效果
                        }
                    }

                    // 导航按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.route

                            NavItemButton(
                                item = item,
                                selected = selected,
                                onClick = { onNavigate(item.route) }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun NavItemButton(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val contentColor = when {
        selected -> MaterialTheme.colorScheme.primary
        else -> Color.White  // 非选中状态改为纯白
    }

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )

    Column(
        modifier = Modifier
            .width(80.dp)
            .height(48.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.perform(HapticFeedbackType.ToggleOn)
                    onClick()
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            modifier = Modifier
                .size(if (selected) 22.dp else 20.dp)
                .scale(iconScale),
            tint = contentColor
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal
        )
    }
}

package com.silas.omaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import com.silas.omaster.ui.components.glass.GlassButton
import com.silas.omaster.ui.components.glass.GlassEffectConfig
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silas.omaster.R
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.ui.theme.CardBorderHighlight
import com.silas.omaster.ui.theme.CardBorderLight
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.GlassColors
import com.silas.omaster.util.PresetI18n
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.silas.omaster.util.perform

@Composable
fun PresetCard(
    preset: MasterPreset,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    showFavoriteButton: Boolean = false,
    showDeleteButton: Boolean = false,
    modifier: Modifier = Modifier,
    imageHeight: Int = 200,
    usePremiumGlass: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 10.dp else 5.dp
        )
    ) {
        Box {
            // Glass 效果层
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = if (isPressed) 1.5.dp else 1.dp,
                        color = if (isPressed)
                            GlassColors.BorderHighlight
                        else
                            GlassColors.BorderOuter,
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            // 图片区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                PresetImage(
                    preset = preset,
                    modifier = Modifier.fillMaxWidth()
                )

                if (showFavoriteButton) {
                    // 统一使用 GlassButton 组件，根据 usePremiumGlass 参数切换模式
                    GlassButton(
                        isActive = preset.isFavorite,
                        glowColor = MaterialTheme.colorScheme.primary,
                        isPremium = usePremiumGlass,
                        size = 36.dp,
                        config = GlassEffectConfig.DefaultButton,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        onClick = {
                            haptic.perform(HapticFeedbackType.ToggleOn)
                            onFavoriteClick()
                        }
                    ) {
                        Icon(
                            imageVector = if (preset.isFavorite)
                                Icons.Filled.Favorite
                            else
                                Icons.Outlined.FavoriteBorder,
                            contentDescription = if (preset.isFavorite)
                                stringResource(R.string.preset_favorited)
                            else
                                stringResource(R.string.preset_favorite),
                            tint = if (preset.isFavorite)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (showDeleteButton && preset.isCustom) {
                    // 统一使用 GlassButton 组件，根据 usePremiumGlass 参数切换模式
                    GlassButton(
                        isActive = false,
                        glowColor = MaterialTheme.colorScheme.error,
                        isPremium = usePremiumGlass,
                        size = 36.dp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        onClick = {
                            haptic.perform(HapticFeedbackType.Confirm)
                            onDeleteClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.preset_delete),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (preset.isNew) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                                    )
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(
                                width = 0.5.dp,
                                color = Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.preset_new),
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // 玻璃质感文字区域 - 叠加在图片底部
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GlassColors.Base.copy(alpha = 0.7f),
                                GlassColors.Base.copy(alpha = 0.95f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = PresetI18n.getLocalizedPresetName(preset.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PresetCardPlaceholder(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .border(
                width = 1.dp,
                color = CardBorderLight,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.empty_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

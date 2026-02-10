package com.silas.omaster.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.ui.theme.DarkGray
import com.silas.omaster.ui.theme.HasselbladOrange

/**
 * 预设卡片组件
 *
 * 展示预设的封面图片和名称，采用瀑布流风格的卡片设计
 *
 * @param preset 预设数据
 * @param onClick 点击回调
 * @param onFavoriteClick 收藏点击回调
 * @param onDeleteClick 删除点击回调（仅自定义预设有效）
 * @param showFavoriteButton 是否显示收藏按钮
 * @param showDeleteButton 是否显示删除按钮
 * @param modifier 修饰符
 * @param imageHeight 图片高度（用于实现瀑布流效果的不同高度）
 */
@Composable
fun PresetCard(
    preset: MasterPreset,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    showFavoriteButton: Boolean = false,
    showDeleteButton: Boolean = false,
    modifier: Modifier = Modifier,
    imageHeight: Int = 200
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGray
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column {
            // 图片容器
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                // 使用通用 PresetImage 组件加载本地 assets 图片
                PresetImage(
                    preset = preset,
                    modifier = Modifier.fillMaxWidth()
                )

                // 收藏按钮（右上角）
                if (showFavoriteButton) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (preset.isFavorite)
                                Icons.Filled.Favorite
                            else
                                Icons.Outlined.FavoriteBorder,
                            contentDescription = if (preset.isFavorite) "已收藏" else "收藏",
                            tint = if (preset.isFavorite) HasselbladOrange else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 删除按钮（左上角，仅自定义预设显示）
                if (showDeleteButton && preset.isCustom) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "删除",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // 预设名称
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 预设卡片占位符
 *
 * 用于加载失败或数据为空时的显示
 *
 * @param modifier 修饰符
 */
@Composable
fun PresetCardPlaceholder(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
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
                text = "暂无数据",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

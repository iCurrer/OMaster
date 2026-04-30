package com.silas.omaster.ui.discover

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import com.silas.omaster.model.ColorCard
import com.silas.omaster.model.ColorRole
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CardDisplayConfig(
    val card: ColorCard,
    val drawCount: Int = 0,
    val dateLabel: String = LocalDate.now().format(DateTimeFormatter.ofPattern("MM.dd")),
    val isAnimating: Boolean = false,
    val isChallengeCompleted: Boolean = false,
    val canRedraw: Boolean = false,
    val remainingDraws: Int = 0,
    val showActions: Boolean = true,
    val onCompleteChallenge: () -> Unit = {},
    val onRedraw: () -> Unit = {},
    val onClear: () -> Unit = {}
)

@Composable
fun ColorWalkCard(
    config: CardDisplayConfig,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (config.isAnimating) 0.95f else 1f,
        animationSpec = tween(400),
        label = "card_scale"
    )

    Card(
        modifier = modifier.scale(scale),
        colors = CardDefaults.cardColors(containerColor = themedCardBackground()),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            CardHeader(config.card, config.dateLabel, config.drawCount)

            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                SectionLabel(stringResource(R.string.colorwalk_theme_primary))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    config.card.colors.filter { it.role == ColorRole.PRIMARY }.forEach { ci ->
                        ColorBlock(ci, Modifier.weight(1f))
                    }
                }

                SectionLabel(stringResource(R.string.colorwalk_theme_accent))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    config.card.colors.filter { it.role == ColorRole.ACCENT }.forEach { ci ->
                        ColorBlock(ci, Modifier.weight(1f))
                    }
                }

                SectionLabel(stringResource(R.string.colorwalk_best_scenes))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    config.card.sceneTags.forEach { tag -> SceneTagChip(stringResource(tag)) }
                }

                ChallengeCard(
                    challenge = stringResource(config.card.challengeResId),
                    isCompleted = config.isChallengeCompleted,
                    onComplete = config.onCompleteChallenge,
                    showAction = config.showActions,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }

            ShootingTipsCard(card = config.card, modifier = Modifier.fillMaxWidth())

            if (config.showActions) {
                ActionButtons(
                    canRedraw = config.canRedraw,
                    remainingDraws = config.remainingDraws,
                    onRedraw = config.onRedraw,
                    onClear = config.onClear
                )
            }
        }
    }
}

@Composable
private fun CardHeader(card: ColorCard, dateLabel: String, drawCount: Int) {
    Box(Modifier.fillMaxWidth().height(200.dp)) {
        Box(
            Modifier.fillMaxSize()
                .background(Brush.horizontalGradient(card.colors.map { it.toColor() }))
        )
        Box(
            Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))))
        )

        Column(
            Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 16.dp, end = 100.dp)
        ) {
            Text(
                stringResource(card.themeResId),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(card.descriptionResId),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f), maxLines = 1
            )
        }

        Column(
            Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(dateLabel, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
            Text(
                if (drawCount > 0) stringResource(R.string.colorwalk_draw_count, drawCount)
                else stringResource(R.string.colorwalk_history),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = themedTextSecondary(), modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun ActionButtons(
    canRedraw: Boolean,
    remainingDraws: Int,
    onRedraw: () -> Unit,
    onClear: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(14.dp),
                onClick = onClear
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Clear, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.colorwalk_hide_today_card),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = if (canRedraw) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else themedCardBackground()
                ),
                shape = RoundedCornerShape(14.dp),
                onClick = { if (canRedraw) onRedraw() }
            ) {
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Refresh, null,
                            tint = if (canRedraw) MaterialTheme.colorScheme.primary else themedTextSecondary(),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.colorwalk_redraw),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (canRedraw) MaterialTheme.colorScheme.primary else themedTextSecondary(),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (canRedraw) {
                        Text("($remainingDraws)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                    } else {
                        Text(stringResource(R.string.colorwalk_no_more_draws), style = MaterialTheme.typography.labelSmall, color = themedTextSecondary())
                    }
                }
            }
        }
    }
}

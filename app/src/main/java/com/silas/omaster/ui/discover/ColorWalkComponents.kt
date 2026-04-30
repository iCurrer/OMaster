package com.silas.omaster.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silas.omaster.R
import com.silas.omaster.model.ColorCard
import com.silas.omaster.model.ColorInfo
import com.silas.omaster.ui.theme.themedBorderLight
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary

@Composable
fun ColorBlock(
    colorInfo: ColorInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorInfo.toColor())
                .border(1.dp, themedBorderLight(), RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(colorInfo.nameResId),
            style = MaterialTheme.typography.labelSmall,
            color = themedTextPrimary(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )

        Text(
            text = "#${colorInfo.hex}",
            style = MaterialTheme.typography.labelSmall,
            color = themedTextSecondary(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ChallengeCard(
    challenge: String,
    isCompleted: Boolean,
    onComplete: () -> Unit,
    showAction: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFF4CAF50).copy(alpha = 0.10f)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) Color(0xFF4CAF50).copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = challenge,
                    style = MaterialTheme.typography.bodyMedium,
                    color = themedTextPrimary(),
                    modifier = Modifier.weight(1f)
                )
            }

            if (showAction) {
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onComplete,
                    enabled = !isCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) Color(0xFF4CAF50).copy(alpha = 0.16f)
                        else MaterialTheme.colorScheme.primary,
                        contentColor = if (isCompleted) Color(0xFF2E7D32) else Color.White,
                        disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.16f),
                        disabledContentColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        if (isCompleted) stringResource(R.string.checkin_completed)
                        else stringResource(R.string.checkin_mark_complete)
                    )
                }
            }
        }
    }
}

@Composable
fun SceneTagChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(themedCardBackground())
            .border(1.dp, themedBorderLight(), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = themedTextPrimary())
    }
}

@Composable
fun ShootingTipsCard(
    card: ColorCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, themedBorderLight(), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = themedCardBackground()),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    stringResource(R.string.colorwalk_shooting_tips),
                    style = MaterialTheme.typography.labelLarge,
                    color = themedTextPrimary(),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(card.tipsResId),
                style = MaterialTheme.typography.bodySmall,
                color = themedTextSecondary(),
                lineHeight = 20.sp
            )
        }
    }
}

package com.silas.omaster.ui.discover

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import com.silas.omaster.data.local.CheckInRecord
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CheckInDetailContent(
    selectedDate: LocalDate,
    record: CheckInRecord?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            style = MaterialTheme.typography.titleLarge,
            color = themedTextPrimary(),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (record == null) {
            Text(
                text = stringResource(R.string.checkin_no_record),
                style = MaterialTheme.typography.bodyMedium,
                color = themedTextSecondary()
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (record.hasDrawnCard) {
                    ActivityItem(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.checkin_activity_draw),
                        description = record.cardTheme ?: stringResource(R.string.checkin_activity_draw_default),
                        tint = Color(0xFF667EEA)
                    )
                }

                if (record.hasCompletedChallenge) {
                    ActivityItem(
                        icon = Icons.Default.CheckCircle,
                        title = stringResource(R.string.checkin_activity_challenge),
                        description = stringResource(R.string.checkin_activity_challenge_desc),
                        tint = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(
    icon: ImageVector,
    title: String,
    description: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themedCardBackground()),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = themedTextPrimary(),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = themedTextSecondary()
                )
            }
        }
    }
}

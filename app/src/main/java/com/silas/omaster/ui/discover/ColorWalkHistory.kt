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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import com.silas.omaster.data.ColorCardLibrary
import com.silas.omaster.data.local.ColorCardHistory
import com.silas.omaster.model.ColorCard
import com.silas.omaster.ui.theme.themedBackground
import com.silas.omaster.ui.theme.themedBorderLight
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary

@Composable
fun HistorySheetContent(
    history: List<ColorCardHistory>,
    onClearHistory: () -> Unit,
    onCardClick: (ColorCard, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.colorwalk_history),
                style = MaterialTheme.typography.titleLarge,
                color = themedTextPrimary(),
                fontWeight = FontWeight.Bold
            )

            if (history.isNotEmpty()) {
                TextButton(onClick = onClearHistory) {
                    Icon(
                        Icons.Default.Delete, null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.colorwalk_clear_history), color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (history.isEmpty()) {
            Text(
                stringResource(R.string.colorwalk_no_history),
                style = MaterialTheme.typography.bodyMedium,
                color = themedTextSecondary(),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(history) { item ->
                    val card = ColorCardLibrary.getCardById(item.cardId)
                    if (card != null) {
                        HistoryCardItem(
                            card = card,
                            date = item.date,
                            onClick = { onCardClick(card, item.date) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HistoryCardItem(
    card: ColorCard,
    date: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, themedBorderLight(), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = themedCardBackground()),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                card.colors.take(4).forEach { colorInfo ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorInfo.toColor())
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                Modifier.width(80.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    stringResource(card.themeResId),
                    style = MaterialTheme.typography.labelMedium,
                    color = themedTextPrimary(),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    date,
                    style = MaterialTheme.typography.labelSmall,
                    color = themedTextSecondary()
                )
            }
        }
    }
}

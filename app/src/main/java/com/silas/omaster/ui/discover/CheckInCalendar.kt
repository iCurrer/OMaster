package com.silas.omaster.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.silas.omaster.R
import com.silas.omaster.data.local.CheckInManager
import com.silas.omaster.data.local.CheckInRecord
import com.silas.omaster.ui.theme.themedCardBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CheckInCalendar(
    onDateClick: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val checkInManager = remember { CheckInManager(context) }

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val records = remember(currentMonth) {
        checkInManager.getRecordsForMonth(currentMonth.year, currentMonth.monthValue)
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        CheckInHeader(
            currentStreak = checkInManager.getCurrentStreak(),
            longestStreak = checkInManager.getLongestStreak(),
            monthlyCount = checkInManager.getMonthlyCheckInCount(currentMonth.year, currentMonth.monthValue),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        WeekDayHeader(modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(6.dp))

        CalendarGrid(
            yearMonth = currentMonth,
            records = records,
            onDateClick = onDateClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CheckInHeader(
    currentStreak: Int,
    longestStreak: Int,
    monthlyCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = Icons.Default.Today,
            label = stringResource(R.string.checkin_current_streak),
            value = stringResource(R.string.checkin_days_unit, currentStreak),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            icon = Icons.Default.EmojiEvents,
            label = stringResource(R.string.checkin_longest_streak),
            value = stringResource(R.string.checkin_days_unit, longestStreak),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            icon = Icons.Default.Timeline,
            label = stringResource(R.string.checkin_month_total),
            value = stringResource(R.string.checkin_days_unit, monthlyCount),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = themedCardBackground()),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = themedTextPrimary(),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = themedTextSecondary(),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = stringResource(R.string.checkin_calendar_previous_month),
                tint = themedTextPrimary()
            )
        }

        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy.MM")),
            style = MaterialTheme.typography.titleMedium,
            color = themedTextPrimary(),
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.checkin_calendar_next_month),
                tint = themedTextPrimary()
            )
        }
    }
}

@Composable
private fun WeekDayHeader(modifier: Modifier = Modifier) {
    val days = listOf(
        stringResource(R.string.checkin_weekday_sun),
        stringResource(R.string.checkin_weekday_mon),
        stringResource(R.string.checkin_weekday_tue),
        stringResource(R.string.checkin_weekday_wed),
        stringResource(R.string.checkin_weekday_thu),
        stringResource(R.string.checkin_weekday_fri),
        stringResource(R.string.checkin_weekday_sat)
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        days.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelSmall,
                color = themedTextSecondary(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    records: Map<String, CheckInRecord>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val leadingBlankDays = firstDayOfMonth.dayOfWeek.value % 7
    val totalSlots = ((leadingBlankDays + daysInMonth + 6) / 7) * 7

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (weekStart in 0 until totalSlots step 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (offset in 0 until 7) {
                    val slotIndex = weekStart + offset
                    val dayNumber = slotIndex - leadingBlankDays + 1
                    if (dayNumber in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNumber)
                        val record = records[date.format(DateTimeFormatter.ISO_LOCAL_DATE)]
                        DayCell(
                            day = dayNumber,
                            record = record,
                            isToday = date == LocalDate.now(),
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    record: CheckInRecord?,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasRecord = record != null

    Card(
        modifier = modifier
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isToday && hasRecord -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                hasRecord -> themedCardBackground()
                else -> Color.Transparent
            }
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isToday -> MaterialTheme.colorScheme.primary
                    hasRecord -> themedTextPrimary()
                    else -> themedTextSecondary()
                },
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IndicatorDot(
                    visible = record?.hasDrawnCard == true,
                    color = MaterialTheme.colorScheme.primary
                )
                IndicatorDot(
                    visible = record?.hasCompletedChallenge == true,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun IndicatorDot(
    visible: Boolean,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(if (visible) color else Color.Transparent)
    )
}

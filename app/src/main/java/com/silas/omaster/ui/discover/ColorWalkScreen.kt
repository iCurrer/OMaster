package com.silas.omaster.ui.discover

import android.content.Context
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.silas.omaster.R
import com.silas.omaster.data.local.CheckInManager
import com.silas.omaster.data.local.ColorWalkManager
import com.silas.omaster.ui.components.OMasterTopAppBar
import com.silas.omaster.ui.theme.AppDesign
import com.silas.omaster.ui.theme.themedBackground
import com.silas.omaster.ui.theme.themedTextPrimary
import com.silas.omaster.ui.theme.themedTextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val PREFS_NAME = "color_walk_prefs"
private const val KEY_HAS_SEEN_GUIDE = "has_seen_guide"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorWalkScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val colorWalkManager = remember { ColorWalkManager(context) }
    val checkInManager = remember { CheckInManager(context) }

    val viewModel: ColorWalkViewModel = viewModel(
        factory = ColorWalkViewModelFactory(colorWalkManager, checkInManager)
    )
    val state by viewModel.state.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val showGuideFirst = remember {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        !prefs.getBoolean(KEY_HAS_SEEN_GUIDE, false)
    }
    var showGuide by remember { mutableStateOf(showGuideFirst) }

    val sheetState = rememberModalBottomSheetState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themedBackground())
    ) {
        OMasterTopAppBar(
            title = stringResource(R.string.colorwalk_title),
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            onBack = onBack,
            actions = {
                IconButton(onClick = { showGuide = true }) {
                    Icon(Icons.AutoMirrored.Filled.HelpOutline, "玩法说明", tint = themedTextPrimary())
                }
                if (state.history.isNotEmpty()) {
                    IconButton(onClick = { viewModel.showHistorySheet() }) {
                        Icon(Icons.Default.History, stringResource(R.string.colorwalk_history), tint = themedTextPrimary())
                    }
                }
            }
        )

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(AppDesign.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.todayCard != null) {
                ColorWalkCard(
                    config = CardDisplayConfig(
                        card = state.todayCard!!,
                        drawCount = state.drawCount,
                        dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("MM.dd")),
                        isAnimating = state.isAnimating,
                        isChallengeCompleted = state.isChallengeCompleted,
                        canRedraw = colorWalkManager.canDrawMore(),
                        remainingDraws = colorWalkManager.getRemainingDraws(),
                        onCompleteChallenge = { viewModel.completeChallenge() },
                        onRedraw = {
                            val card = state.todayCard
                            if (card != null) viewModel.redrawCard(context.getString(card.themeResId))
                        },
                        onClear = { viewModel.clearTodayCard() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                ColorWalkEmptyCard(
                    onDrawClick = { viewModel.drawCard("") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }

    if (showGuide) {
        ColorWalkGuide(
            onDismiss = {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_HAS_SEEN_GUIDE, true).apply()
                showGuide = false
            }
        )
    }

    when (val current = uiState) {
        is ColorWalkUiState.HistorySheet -> {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissUiState() },
                sheetState = sheetState
            ) {
                HistorySheetContent(
                    history = state.history,
                    onClearHistory = { viewModel.showClearConfirm() },
                    onCardClick = { card, date -> viewModel.showHistoryPreview(card, date) }
                )
            }
        }

        is ColorWalkUiState.HistoryPreview -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUiState() },
                title = {
                    Text(
                        stringResource(R.string.colorwalk_history_preview_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 560.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        ColorWalkCard(
                            config = CardDisplayConfig(
                                card = current.card,
                                dateLabel = current.date,
                                showActions = false
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissUiState() }) {
                        Text(stringResource(R.string.colorwalk_close_preview))
                    }
                }
            )
        }

        is ColorWalkUiState.ClearConfirm -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUiState() },
                title = { Text(stringResource(R.string.colorwalk_clear_history)) },
                text = { Text(stringResource(R.string.colorwalk_clear_history_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearHistory()
                            viewModel.dismissUiState()
                        }
                    ) {
                        Text(stringResource(R.string.colorwalk_confirm_clear))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUiState() }) {
                        Text(stringResource(R.string.colorwalk_cancel))
                    }
                }
            )
        }

        else -> { /* Normal */ }
    }
}

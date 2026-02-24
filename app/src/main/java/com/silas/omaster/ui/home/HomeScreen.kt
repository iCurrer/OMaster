package com.silas.omaster.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
// pullrefresh experimental annotation not available in this compose version
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.silas.omaster.R
import com.silas.omaster.data.repository.PresetRepository
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.ui.animation.AnimationSpecs
import com.silas.omaster.ui.animation.ListItemFadeInSpec
import com.silas.omaster.ui.animation.ListItemPlacementSpec
import com.silas.omaster.ui.animation.calculateStaggerDelay
import com.silas.omaster.ui.components.PresetCard
import com.silas.omaster.ui.service.FloatingWindowController
import com.silas.omaster.ui.theme.PureBlack
import com.silas.omaster.util.hapticClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.silas.omaster.util.perform
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (MasterPreset) -> Unit,
    onNavigateToCreate: () -> Unit,
    onScrollStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    refreshTrigger: Int = 0
) {
    val context = LocalContext.current
    val repository = remember { PresetRepository.getInstance(context) }
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repository)
    )
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val allPresets by viewModel.allPresets.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val customPresets by viewModel.customPresets.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    
    // 当 refreshTrigger 变化时刷新数据
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            viewModel.refresh()
        }
    }

    val pagerState = rememberPagerState(pageCount = { 3 })

    // 全局悬浮窗控制器
    val floatingWindowController = remember { FloatingWindowController.getInstance(context) }

    // 当预设列表或选中的 Tab 变化时，更新到全局控制器
    LaunchedEffect(allPresets, favorites, customPresets, selectedTab) {
        val currentList = when (selectedTab) {
            0 -> allPresets
            1 -> favorites
            2 -> customPresets
            else -> allPresets
        }
        floatingWindowController.setPresetList(currentList)
    }

    // 删除确认对话框状态
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var presetToDelete by remember { mutableStateOf<String?>(null) }

    // 同步 Tab 和 Pager 的状态
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != selectedTab) {
            viewModel.selectTab(pagerState.currentPage)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 紧凑的标题栏
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Tab 切换栏 - 带动画指示器
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureBlack,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                modifier = Modifier.height(44.dp),
                indicator = { tabPositions ->
                    // 自定义指示器动画
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {} // 移除默认分割线
            ) {
                val tabs = listOf(
                    stringResource(R.string.tab_all) to allPresets.size,
                    stringResource(R.string.tab_favorites) to favorites.size,
                    stringResource(R.string.tab_my) to customPresets.size
                )
                
                tabs.forEachIndexed { index, (title, count) ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = {
                            haptic.perform(HapticFeedbackType.ToggleOn)
                            scope.launch {
                                pagerState.scrollToPage(index)
                            }
                            viewModel.selectTab(index)
                        },
                        text = {
                            // 修复：计数使用小号字体放在右上角，避免Tab宽度不均
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                // 计数徽章
                                if (count > 0) {
                                    Text(
                                        text = count.toString(),
                                        fontSize = 10.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // 可滑动的页面内容
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                // 使用 key 确保切换页面时重新创建组件，触发动画
                androidx.compose.runtime.key(page) {
                    when (page) {
                        0 -> PresetGrid(
                            presets = allPresets,
                            tabIndex = 0,
                            onNavigateToDetail = onNavigateToDetail,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDeletePreset = {
                                presetToDelete = it
                                showDeleteConfirm = true
                            },
                            onScrollStateChanged = onScrollStateChanged,
                            onRefresh = { onComplete -> viewModel.refresh(onComplete) }
                        )
                        1 -> PresetGrid(
                            presets = favorites,
                            tabIndex = 1,
                            onNavigateToDetail = onNavigateToDetail,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDeletePreset = {
                                presetToDelete = it
                                showDeleteConfirm = true
                            },
                            onScrollStateChanged = onScrollStateChanged,
                            showLoadingTip = false,
                            onRefresh = { onComplete -> viewModel.refresh(onComplete) }
                        )
                        2 -> PresetGrid(
                            presets = customPresets,
                            tabIndex = 2,
                            onNavigateToDetail = onNavigateToDetail,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDeletePreset = {
                                presetToDelete = it
                                showDeleteConfirm = true
                            },
                            showLoadingTip = false,
                            showTopHint = false,
                            onScrollStateChanged = onScrollStateChanged,
                            onRefresh = { onComplete -> viewModel.refresh(onComplete) }
                        )
                    }
                }
            }
        }

        // 悬浮添加按钮（只在"我的"Tab显示）
        if (selectedTab == 2) {
            FloatingActionButton(
                onClick = {
                    haptic.perform(HapticFeedbackType.Confirm)
                    onNavigateToCreate()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 100.dp)
                    .size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.create_preset),
                modifier = Modifier.size(32.dp)
            )
        }
    }

    // 删除确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                presetToDelete = null
            },
            title = { Text(stringResource(R.string.delete_preset_title)) },
            text = { Text(stringResource(R.string.delete_preset_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.perform(HapticFeedbackType.Confirm)
                        val id = presetToDelete
                        if (id != null) {
                            viewModel.deleteCustomPreset(id)
                        }
                        showDeleteConfirm = false
                        presetToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        haptic.perform(HapticFeedbackType.TextHandleMove)
                        showDeleteConfirm = false
                        presetToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PresetGrid(
    presets: List<MasterPreset>,
    tabIndex: Int,
    onNavigateToDetail: (MasterPreset) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDeletePreset: (String) -> Unit,
    onScrollStateChanged: (Boolean) -> Unit = {},
    showLoadingTip: Boolean = true,
    showTopHint: Boolean = false,
    onRefresh: (onComplete: () -> Unit) -> Unit = {}
) {
    val listState = rememberLazyStaggeredGridState()
    val haptic = LocalHapticFeedback.current

    // Pull-to-refresh state
    var refreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh = {
        refreshing = true
        onRefresh { refreshing = false }
    })

    // Remove the problematic LaunchedEffect
    // LaunchedEffect(presets) {
    //    if (refreshing) refreshing = false
    // }

    // 修复：使用 snapshotFlow 安全地检测滚动方向
    // 避免在 derivedStateOf 中修改外部状态
    var isScrollingUp by remember { mutableStateOf(false) }
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    var hasHapticAtTop by remember { mutableStateOf(false) }
    var hasHapticAtBottom by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (currentIndex, currentOffset) ->
            val isUp = currentIndex < previousIndex ||
                       (currentIndex == previousIndex && currentOffset <= previousScrollOffset)
            isScrollingUp = isUp
            previousIndex = currentIndex
            previousScrollOffset = currentOffset
            onScrollStateChanged(isUp)

            // 滚动到顶部或底部时触发震感
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            if (currentIndex == 0 && !hasHapticAtTop) {
                // 滚动到顶部
                haptic.perform(HapticFeedbackType.TextHandleMove)
                hasHapticAtTop = true
                hasHapticAtBottom = false
            } else if (lastVisibleItem >= totalItems - 1 && totalItems > 0 && !hasHapticAtBottom) {
                // 滚动到底部（最后一个可见 item 是最后一个 item）
                haptic.perform(HapticFeedbackType.TextHandleMove)
                hasHapticAtBottom = true
                hasHapticAtTop = false
            } else if (currentIndex > 0 && lastVisibleItem < totalItems - 1) {
                // 在中间位置，重置状态
                hasHapticAtTop = false
                hasHapticAtBottom = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        if (presets.isEmpty()) {
            // 使用 LazyColumn 确保即使为空也能触发下拉刷新
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    EmptyState(tabIndex)
                }
            }
        } else {
            // 缓存可见区域起始索引，避免每次重组都计算
            val visibleStartIndex by remember {
                derivedStateOf {
                    listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                }
            }

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 100.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),  // 优化：水平间距从 12dp 增加到 16dp
                verticalItemSpacing = 16.dp,  // 优化：垂直间距从 12dp 增加到 16dp
                modifier = Modifier.fillMaxSize()
            ) {
                if (showTopHint) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.feature_coming_soon),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                itemsIndexed(
                    items = presets,
                    key = { index, preset -> preset.id?.let { "${it}_$index" } ?: "preset_$index" }
                ) { index, preset ->
                    val imageHeight = remember(index) {
                        when (index % 3) {
                            0 -> 220
                            1 -> 180
                            else -> 260
                        }
                    }

                    if (preset.id != null) {
                        // 使用缓存的 visibleStartIndex 计算延迟
                        // 优化：只有在列表顶部时才应用交错延迟，滚动时立即显示，避免卡顿感
                        val delayMillis = if (visibleStartIndex == 0) {
                            calculateStaggerDelay(index, visibleStartIndex)
                        } else {
                            0
                        }

                        PresetCardItem(
                            preset = preset,
                            index = index,
                            tabIndex = tabIndex,
                            imageHeight = imageHeight,
                            delayMillis = delayMillis,
                            onNavigateToDetail = onNavigateToDetail,
                            onToggleFavorite = onToggleFavorite,
                            onDeletePreset = onDeletePreset,
                            modifier = Modifier.animateItem(
                                fadeInSpec = ListItemFadeInSpec,
                                placementSpec = ListItemPlacementSpec
                            )
                        )
                    }
                }

                // 底部提示（仅在全部预设页面显示）
                if (showLoadingTip) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        LoadingMoreTip()
                    }
                }
            }
        }

        // Pull refresh indicator overlay
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PresetCardItem(
    preset: MasterPreset,
    index: Int,
    tabIndex: Int,
    imageHeight: Int,
    delayMillis: Int,
    onNavigateToDetail: (MasterPreset) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDeletePreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 使用统一的动画状态管理，减少 Animatable 实例
    val animatedProgress = remember(preset.id, tabIndex) { Animatable(0f) }

    LaunchedEffect(preset.id, tabIndex) {
        // 添加延迟，实现错开动画效果
        if (delayMillis > 0) {
            delay(delayMillis.toLong())
        }
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = AnimationSpecs.CardSpring
        )
    }

    // 使用 graphicsLayer 进行硬件加速友好的变换
    val alpha = animatedProgress.value
    val scale = 0.85f + (0.15f * animatedProgress.value)
    val translationY = (1f - animatedProgress.value) * 30f

    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationY = translationY
                // 启用硬件加速
                this.shadowElevation = if (alpha > 0.9f) 4f else 0f
            }
    ) {
        PresetCard(
            preset = preset,
            onClick = { onNavigateToDetail(preset) },
            onFavoriteClick = { onToggleFavorite(preset.id!!) },
            onDeleteClick = { onDeletePreset(preset.id!!) },
            showFavoriteButton = true,
            showDeleteButton = tabIndex == 2,
            imageHeight = imageHeight
        )
    }
}

@Composable
private fun EmptyState(tabIndex: Int) {
    val message = when (tabIndex) {
        0 -> stringResource(R.string.empty_no_presets)
        1 -> stringResource(R.string.empty_no_favorites)
        2 -> stringResource(R.string.empty_no_custom)
        else -> stringResource(R.string.empty_no_data)
    }

    val subMessage = when (tabIndex) {
        0 -> stringResource(R.string.empty_hint_add_presets)
        1 -> stringResource(R.string.empty_hint_favorite)
        2 -> stringResource(R.string.empty_hint_create)
        else -> ""
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            if (subMessage.isNotEmpty()) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 底部加载更多提示 - 持续更新 敬请期待
 */
@Composable
private fun LoadingMoreTip() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 装饰线条
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(2.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 主文字
            Text(
                text = stringResource(R.string.load_more_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )

            // 装饰线条
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(2.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

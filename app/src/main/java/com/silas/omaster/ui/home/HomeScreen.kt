package com.silas.omaster.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.silas.omaster.data.repository.PresetRepository
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.ui.animation.AnimationSpecs
import com.silas.omaster.ui.animation.ListItemFadeInSpec
import com.silas.omaster.ui.animation.ListItemPlacementSpec
import com.silas.omaster.ui.animation.calculateStaggerDelay
import com.silas.omaster.ui.components.PresetCard
import com.silas.omaster.ui.theme.HasselbladOrange
import com.silas.omaster.ui.theme.PureBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (MasterPreset) -> Unit,
    onNavigateToCreate: () -> Unit,
    onScrollStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { PresetRepository.getInstance(context) }
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repository)
    )

    val allPresets by viewModel.allPresets.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val customPresets by viewModel.customPresets.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

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
                    text = "OMaster",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Tab 切换栏 - 带动画指示器
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureBlack,
                contentColor = HasselbladOrange,
                edgePadding = 16.dp,
                modifier = Modifier.height(44.dp),
                indicator = { tabPositions ->
                    // 自定义指示器动画
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp),
                        color = HasselbladOrange
                    )
                },
                divider = {} // 移除默认分割线
            ) {
                val tabs = listOf(
                    "全部" to allPresets.size,
                    "收藏" to favorites.size,
                    "我的" to customPresets.size
                )
                
                tabs.forEachIndexed { index, (title, count) ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = {
                            // 直接滚动到目标页面，不经过中间页面
                            scope.launch {
                                pagerState.scrollToPage(index)
                            }
                            viewModel.selectTab(index)
                        },
                        text = {
                            Text(
                                text = "$title($count)",
                                color = if (isSelected) HasselbladOrange else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
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
                            onDeletePreset = { viewModel.deleteCustomPreset(it) },
                            onScrollStateChanged = onScrollStateChanged
                        )
                        1 -> PresetGrid(
                            presets = favorites,
                            tabIndex = 1,
                            onNavigateToDetail = onNavigateToDetail,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDeletePreset = { viewModel.deleteCustomPreset(it) },
                            onScrollStateChanged = onScrollStateChanged
                        )
                        2 -> PresetGrid(
                            presets = customPresets,
                            tabIndex = 2,
                            onNavigateToDetail = onNavigateToDetail,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onDeletePreset = { viewModel.deleteCustomPreset(it) },
                            onScrollStateChanged = onScrollStateChanged
                        )
                    }
                }
            }
        }

        // 悬浮添加按钮（只在"我的"Tab显示）
        if (selectedTab == 2) {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = HasselbladOrange,
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
                    contentDescription = "新建预设",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun PresetGrid(
    presets: List<MasterPreset>,
    tabIndex: Int,
    onNavigateToDetail: (MasterPreset) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDeletePreset: (String) -> Unit,
    onScrollStateChanged: (Boolean) -> Unit = {}
) {
    val listState = rememberLazyStaggeredGridState()
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    // 检测滚动方向，上滑返回 true，下滑返回 false
    val isScrollingUp by remember {
        derivedStateOf {
            val currentIndex = listState.firstVisibleItemIndex
            val currentOffset = listState.firstVisibleItemScrollOffset
            val isUp = currentIndex < previousIndex ||
                       (currentIndex == previousIndex && currentOffset <= previousScrollOffset)
            previousIndex = currentIndex
            previousScrollOffset = currentOffset
            isUp
        }
    }

    LaunchedEffect(isScrollingUp) {
        onScrollStateChanged(isScrollingUp)
    }

    if (presets.isEmpty()) {
        EmptyState(tabIndex)
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            modifier = Modifier.fillMaxSize()
        ) {
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
                    val delayMillis = calculateStaggerDelay(index, visibleStartIndex)

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
        }
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
        0 -> "暂无预设数据"
        1 -> "暂无收藏预设"
        2 -> "暂无自定义预设"
        else -> "暂无数据"
    }

    val subMessage = when (tabIndex) {
        0 -> "请在 assets/presets.json 中添加数据"
        1 -> "点击心形图标收藏预设"
        2 -> "点击右下角按钮创建预设"
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
                    color = HasselbladOrange.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

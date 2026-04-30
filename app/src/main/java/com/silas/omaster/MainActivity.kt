package com.silas.omaster

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.silas.omaster.model.MasterPreset
import com.silas.omaster.ui.components.PillNavBar
import com.silas.omaster.ui.components.WelcomeDialog
import com.silas.omaster.ui.create.PresetSelectionScreen
import com.silas.omaster.ui.create.UniversalCreatePresetScreen
import com.silas.omaster.ui.create.UniversalCreatePresetViewModel
import com.silas.omaster.ui.create.UniversalCreatePresetViewModelFactory
import com.silas.omaster.ui.discover.DiscoverScreen
import com.silas.omaster.ui.discover.ColorWalkScreen
import com.silas.omaster.ui.frame.PhotoFrameScreen
import com.silas.omaster.ui.detail.ProfileScreen
import com.silas.omaster.ui.detail.DetailScreen
import com.silas.omaster.ui.detail.OpenSourceLicenseScreen
import com.silas.omaster.ui.detail.PrivacyPolicyScreen
import com.silas.omaster.ui.home.HomeScreen
import com.silas.omaster.ui.service.FloatingWindowController
import com.silas.omaster.ui.theme.OMasterTheme
import com.silas.omaster.util.JsonUtil
import com.silas.omaster.util.LanguageUtil
import com.silas.omaster.util.VersionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.silas.omaster.data.repository.PresetRepository

import androidx.compose.runtime.collectAsState
import com.silas.omaster.data.config.ConfigCenter
import com.silas.omaster.ui.settings.SettingsScreen
import com.silas.omaster.util.UpdateChecker
import kotlinx.coroutines.delay


val LocalActivity = compositionLocalOf<Activity> { error("No Activity provided") }

sealed class Screen {
    @Serializable
    data object Home : Screen()

    @Serializable
    data class Detail(val presetId: String) : Screen()

    @Serializable
    data object PresetSelection : Screen()

    @Serializable
    data class CreatePreset(val templateId: String? = null) : Screen()

    @Serializable
    data class EditPreset(val presetId: String) : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object Profile : Screen()

    @Serializable
    data object About : Screen()

    @Serializable
    data object Discover : Screen()

    @Serializable
    data object ColorWalk : Screen()

    @Serializable
    data object Subscription : Screen()

    @Serializable
    data object PrivacyPolicy : Screen()

    @Serializable
    data object OpenSourceLicense : Screen()

    @Serializable
    data object PhotoFrame : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var floatingWindowController: FloatingWindowController

    override fun attachBaseContext(newBase: Context?) {
        // 在 Activity 创建前应用语言设置
        val context = newBase?.let { LanguageUtil.applyLanguage(it) }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 初始化并注册全局悬浮窗控制器
        floatingWindowController = FloatingWindowController.getInstance(this)
        floatingWindowController.register()

        setContent {
            CompositionLocalProvider(LocalActivity provides this) {
                val config = remember { ConfigCenter.getInstance(applicationContext) }
                val currentTheme by config.themeFlow.collectAsState()
                val darkMode by config.darkModeFlow.collectAsState()
                
                // 根据深色模式设置计算实际主题
                val isDarkTheme = when (darkMode) {
                    com.silas.omaster.data.local.DarkMode.SYSTEM -> {
                        val uiMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                        uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
                    }
                    com.silas.omaster.data.local.DarkMode.LIGHT -> false
                    com.silas.omaster.data.local.DarkMode.DARK -> true
                }

                OMasterTheme(
                    darkTheme = isDarkTheme,
                    brandTheme = currentTheme
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        var showWelcomeFlow by remember { mutableStateOf(!OMasterApplication.getInstance().hasUserAgreed()) }

                        if (showWelcomeFlow) {
                            WelcomeFlow(
                                navController = navController,
                                onAgree = {
                                    OMasterApplication.getInstance().setUserAgreed(true)
                                    OMasterApplication.getInstance().initUMeng()
                                    showWelcomeFlow = false
                                },
                                onDisagree = {
                                    finish()
                                }
                            )
                        } else {
                            MainApp(
                                navController = navController,
                                config = config
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 注销悬浮窗控制器
        floatingWindowController.unregister()
    }
}

@Composable
fun WelcomeFlow(
    navController: NavHostController,
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showOpenSourceLicense by remember { mutableStateOf(false) }

    // 处理系统返回键
    androidx.activity.compose.BackHandler(enabled = showPrivacyPolicy || showOpenSourceLicense) {
        showPrivacyPolicy = false
        showOpenSourceLicense = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            showPrivacyPolicy -> {
                PrivacyPolicyScreen(
                    onBack = {
                        showPrivacyPolicy = false
                    }
                )
            }
            showOpenSourceLicense -> {
                OpenSourceLicenseScreen(
                    onBack = {
                        showOpenSourceLicense = false
                    }
                )
            }
            else -> {
                WelcomeDialog(
                    onAgree = onAgree,
                    onDisagree = onDisagree,
                    onViewPrivacyPolicy = {
                        showPrivacyPolicy = true
                    }
                )
            }
        }
    }
}

@Composable
fun MainApp(
    navController: NavHostController,
    config: ConfigCenter
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { PresetRepository.getInstance(context) }
    var showMigrationDialog by remember { mutableStateOf(false) }

    // 检查是否需要数据迁移
    LaunchedEffect(Unit) {
        // 先触发 loadPresets 来正确检测版本
        // 使用 IO 线程避免阻塞 UI
        withContext(Dispatchers.IO) {
            JsonUtil.loadPresets(context)
        }
        
        // 现在 currentPresetsVersion 已经被正确设置
        // 支持 version 2 和 3，只有旧版本 (version 1) 才需要迁移
        if (JsonUtil.currentPresetsVersion == 1) {
            showMigrationDialog = true
        }
    }

    if (showMigrationDialog) {
        AlertDialog(
            onDismissRequest = { /* Force user to decide */ },
            title = { Text("数据结构更新") },
            text = { Text("检测到预设数据版本过旧，需要迁移数据以支持新功能。\n\n点击“迁移数据”将重置内置预设（您的自定义预设和收藏不会丢失）。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        JsonUtil.deleteRemotePresets(context)
                        repository.reloadDefaultPresets()
                        showMigrationDialog = false
                    }
                ) {
                    Text("迁移数据")
                }
            },
            dismissButton = {
                // Optional: Allow user to cancel and exit app?
                // Or maybe just hide dialog and let them use potentially broken app?
                // Given the request "check if version field exists and value is 2, otherwise pop up prompt",
                // usually implies mandatory action.
                // But for safety/UX, maybe allow cancel?
                // If cancel, showMigrationDialog = false, but app might crash later if structure mismatch.
                // Let's stick to mandatory for now or just allow dismiss.
                // I'll leave dismissButton empty to force "Migrate" or back button (which onDismissRequest handles if we implemented logic).
                // Actually, onDismissRequest handles back button.
            }
        )
    }

    val showBottomNav = currentRoute?.contains("Home") == true || 
                        currentRoute?.contains("Discover") == true || 
                        currentRoute?.contains("About") == true

    var isHomeScrollingUp by remember { mutableStateOf(true) }
    
    // 用于触发 HomeScreen 刷新的状态
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // 读取高级 Glass 质感配置
    val usePremiumGlass by config.premiumGlassFlow.collectAsState()

    // 自动检查更新相关状态
    val autoCheckUpdate by config.autoCheckUpdateFlow.collectAsState()
    val updateChannel by config.updateChannelFlow.collectAsState()
    var showUpdateDialog by remember { mutableStateOf<UpdateChecker.UpdateInfo?>(null) }

    // 冷启动时自动检查更新
    LaunchedEffect(Unit) {
        if (autoCheckUpdate) {
            // 延迟2秒，避免启动时立即弹窗影响用户体验
            delay(2000)
            try {
                val result = UpdateChecker.checkUpdate(context, VersionInfo.VERSION_CODE, updateChannel)
                if (result != null && result.isNewer) {
                    showUpdateDialog = result
                }
            } catch (e: Exception) {
                // 静默失败，不打扰用户
            }
        }
    }

    // 底部导航栏页面顺序，用于决定切换动画方向
    val mainRouteList = remember { listOf("Home", "Discover", "About") }
    fun getNavIndex(route: String?): Int {
        return mainRouteList.indexOfFirst { route?.contains(it) == true }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                val initialIndex = getNavIndex(initialState.destination.route)
                val targetIndex = getNavIndex(targetState.destination.route)
                
                val direction = if (initialIndex != -1 && targetIndex != -1) {
                    // 底部导航栏页面之间的切换
                    if (targetIndex > initialIndex) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }
                } else {
                    // 默认的前进导航（如 Home -> Detail）
                    AnimatedContentTransitionScope.SlideDirection.Left
                }
                
                slideIntoContainer(
                    towards = direction,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                val initialIndex = getNavIndex(initialState.destination.route)
                val targetIndex = getNavIndex(targetState.destination.route)
                
                val direction = if (initialIndex != -1 && targetIndex != -1) {
                    // 底部导航栏页面之间的切换
                    if (targetIndex > initialIndex) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }
                } else {
                    // 默认的前进导航（如 Home -> Detail）
                    AnimatedContentTransitionScope.SlideDirection.Left
                }

                slideOutOfContainer(
                    towards = direction,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                val initialIndex = getNavIndex(initialState.destination.route)
                val targetIndex = getNavIndex(targetState.destination.route)
                
                val direction = if (initialIndex != -1 && targetIndex != -1) {
                    // 底部导航栏页面之间的切换（通过 popBackStack 触发，如回到 Home）
                    if (targetIndex > initialIndex) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }
                } else {
                    // 默认的回退导航（如 Detail -> Home）
                    AnimatedContentTransitionScope.SlideDirection.Right
                }

                slideIntoContainer(
                    towards = direction,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                val initialIndex = getNavIndex(initialState.destination.route)
                val targetIndex = getNavIndex(targetState.destination.route)
                
                val direction = if (initialIndex != -1 && targetIndex != -1) {
                    // 底部导航栏页面之间的切换（通过 popBackStack 触发，如从 Subscription 回到 Home）
                    if (targetIndex > initialIndex) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }
                } else {
                    // 默认的回退导航（如 Detail -> Home）
                    AnimatedContentTransitionScope.SlideDirection.Right
                }

                slideOutOfContainer(
                    towards = direction,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable<Screen.Home> {
                HomeScreen(
                    onNavigateToDetail = { preset: MasterPreset ->
                        preset.id?.let { id ->
                            navController.navigate(Screen.Detail(id))
                        }
                    },
                    onNavigateToCreate = {
                        navController.navigate(Screen.PresetSelection)
                    },
                    onScrollStateChanged = { isScrollingUp ->
                        isHomeScrollingUp = isScrollingUp
                    },
                    refreshTrigger = refreshTrigger,
                    usePremiumGlass = usePremiumGlass
                )
            }

            composable<Screen.PresetSelection> {
                PresetSelectionScreen(
                    onPresetSelected = { templateId ->
                        navController.navigate(Screen.CreatePreset(templateId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.Detail> { backStackEntry ->
                val detail = backStackEntry.toRoute<Screen.Detail>()
                val localContext = androidx.compose.ui.platform.LocalContext.current
                val repository = PresetRepository.getInstance(localContext)
                DetailScreen(
                    presetId = detail.presetId,
                    onBack = {
                        navController.popBackStack()
                    },
                    onEdit = { presetId ->
                        navController.navigate(Screen.EditPreset(presetId))
                    },
                    refreshTrigger = refreshTrigger
                )
            }

            composable<Screen.CreatePreset> { backStackEntry ->
                val createPreset = backStackEntry.toRoute<Screen.CreatePreset>()
                val localContext = androidx.compose.ui.platform.LocalContext.current
                val repository = PresetRepository.getInstance(localContext)
                
                val viewModel: UniversalCreatePresetViewModel = viewModel(
                    factory = UniversalCreatePresetViewModelFactory(localContext, repository)
                )
                
                // Load template if not already loaded (to avoid reloading on recomposition)
                // However, viewModel survives configuration changes, but if we navigate back and forth, 
                // we might want to ensure we don't overwrite if user is editing.
                // For simplicity, we can load it once. 
                // But since we create a new screen instance on navigation, 
                // the viewModel store owner is the backStackEntry, so it's a new ViewModel instance.
                LaunchedEffect(createPreset.templateId) {
                    viewModel.loadTemplate(createPreset.templateId)
                }

                UniversalCreatePresetScreen(
                    onSave = {
                        refreshTrigger++ // 触发刷新
                        // Navigate back to Home, popping the selection screen as well
                        navController.popBackStack(Screen.Home, false)
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    viewModel = viewModel
                )
            }

            composable<Screen.EditPreset> { backStackEntry ->
                val editPreset = backStackEntry.toRoute<Screen.EditPreset>()
                val localContext = androidx.compose.ui.platform.LocalContext.current
                val repository = PresetRepository.getInstance(localContext)
                
                val viewModel: UniversalCreatePresetViewModel = viewModel(
                    factory = UniversalCreatePresetViewModelFactory(localContext, repository)
                )

                LaunchedEffect(editPreset.presetId) {
                    viewModel.loadPresetForEdit(editPreset.presetId)
                }

                UniversalCreatePresetScreen(
                    onSave = {
                        refreshTrigger++ // 触发刷新
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    viewModel = viewModel
                )
            }

            composable<Screen.Settings> {
                SettingsScreen()
            }

            composable<Screen.About> {
                com.silas.omaster.ui.detail.ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings)
                    },
                    onNavigateToSubscription = {
                        navController.navigate(Screen.Subscription)
                    },
                    onScrollStateChanged = { isScrollingUp ->
                        isHomeScrollingUp = isScrollingUp
                    },
                    onNavigateToPrivacyPolicy = {
                        navController.navigate(Screen.PrivacyPolicy)
                    },
                    onNavigateToOpenSourceLicense = {
                        navController.navigate(Screen.OpenSourceLicense)
                    }
                )
            }

            composable<Screen.Discover> {
                DiscoverScreen(
                    onNavigateToColorWalk = {
                        navController.navigate(Screen.ColorWalk)
                    },
                    onNavigateToPhotoFrame = {
                        navController.navigate(Screen.PhotoFrame)
                    },
                    onScrollStateChanged = { isScrollingUp ->
                        isHomeScrollingUp = isScrollingUp
                    }
                )
            }

            composable<Screen.ColorWalk> {
                ColorWalkScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.PhotoFrame> {
                PhotoFrameScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.Subscription> {
                com.silas.omaster.ui.subscription.SubscriptionScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onScrollStateChanged = { isScrollingUp ->
                        isHomeScrollingUp = isScrollingUp
                    }
                )
            }

            composable<Screen.PrivacyPolicy> {
                PrivacyPolicyScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Screen.OpenSourceLicense> {
                OpenSourceLicenseScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        if (showBottomNav) {
            PillNavBar(
                visible = isHomeScrollingUp,
                currentRoute = when {
                    currentRoute?.contains("Home") == true -> "home"
                    currentRoute?.contains("Discover") == true -> "discover"
                    currentRoute?.contains("About") == true -> "about"
                    else -> "home"
                },
                onNavigate = { route ->
                    when (route) {
                        "home" -> {
                            if (currentRoute?.contains("Home") != true) {
                                navController.popBackStack(Screen.Home, false)
                            }
                        }
                        "discover" -> {
                            if (currentRoute?.contains("Discover") != true) {
                                navController.navigate(Screen.Discover) {
                                    popUpTo(Screen.Home) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        "about" -> {
                            if (currentRoute?.contains("About") != true) {
                                navController.navigate(Screen.About) {
                                    popUpTo(Screen.Home) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
                usePremiumGlass = usePremiumGlass
            )
        }

        // 自动检查更新弹窗
        showUpdateDialog?.let { updateInfo ->
            AlertDialog(
                onDismissRequest = { showUpdateDialog = null },
                title = { 
                    Text(
                        text = "发现新版本",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Column {
                        // 版本信息卡片
                        androidx.compose.material3.Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "当前版本",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "v${VersionInfo.VERSION_NAME}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "最新版本",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "v${updateInfo.versionName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        // 更新内容
                        if (updateInfo.releaseNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "更新内容",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.heightIn(max = 150.dp)
                            ) {
                                items(updateInfo.releaseNotes.lines().filter { it.isNotBlank() }) { line ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = line.trim().removePrefix("-").trim(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            UpdateChecker.downloadAndInstall(context, updateInfo.downloadUrl, updateInfo.versionName)
                            showUpdateDialog = null
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                    ) {
                        Text("立即更新")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showUpdateDialog = null }
                    ) {
                        Text(
                            "稍后",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

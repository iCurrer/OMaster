package com.example.omaster

import android.app.Activity
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.omaster.model.MasterPreset
import com.example.omaster.ui.components.PillNavBar
import com.example.omaster.ui.components.WelcomeDialog
import com.example.omaster.ui.create.CreatePresetScreen
import com.example.omaster.ui.create.CreatePresetViewModelFactory
import com.example.omaster.ui.detail.AboutScreen
import com.example.omaster.ui.detail.DetailScreen
import com.example.omaster.ui.detail.DetailViewModelFactory
import com.example.omaster.ui.detail.PrivacyPolicyScreen
import com.example.omaster.ui.home.HomeScreen
import com.example.omaster.ui.theme.OMasterTheme
import kotlinx.serialization.Serializable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omaster.data.repository.PresetRepository

val LocalActivity = compositionLocalOf<Activity> { error("No Activity provided") }

sealed class Screen {
    @Serializable
    data object Home : Screen()

    @Serializable
    data class Detail(val presetId: String) : Screen()

    @Serializable
    data object CreatePreset : Screen()

    @Serializable
    data object About : Screen()

    @Serializable
    data object PrivacyPolicy : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CompositionLocalProvider(LocalActivity provides this) {
                OMasterTheme {
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
                            MainApp(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeFlow(
    navController: NavHostController,
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    // 处理系统返回键
    androidx.activity.compose.BackHandler(enabled = showPrivacyPolicy) {
        showPrivacyPolicy = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showPrivacyPolicy) {
            PrivacyPolicyScreen(
                onBack = {
                    showPrivacyPolicy = false
                }
            )
        } else {
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

@Composable
fun MainApp(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomNav = currentRoute?.contains("Home") == true || currentRoute?.contains("About") == true

    var isHomeScrollingUp by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
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
                        navController.navigate(Screen.CreatePreset)
                    },
                    onScrollStateChanged = { isScrollingUp ->
                        isHomeScrollingUp = isScrollingUp
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
                    }
                )
            }

            composable<Screen.CreatePreset> {
                val localContext = androidx.compose.ui.platform.LocalContext.current
                val repository = PresetRepository.getInstance(localContext)
                CreatePresetScreen(
                    onSave = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    viewModel = viewModel(
                        factory = CreatePresetViewModelFactory(localContext, repository)
                    )
                )
            }

            composable<Screen.About> {
                AboutScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onScrollStateChanged = { isScrollingUp ->
                        isHomeScrollingUp = isScrollingUp
                    }
                )
            }
        }

        if (showBottomNav) {
            PillNavBar(
                visible = isHomeScrollingUp,
                currentRoute = when {
                    currentRoute?.contains("Home") == true -> "home"
                    currentRoute?.contains("About") == true -> "about"
                    else -> "home"
                },
                onNavigate = { route ->
                    when (route) {
                        "home" -> {
                            if (currentRoute?.contains("About") == true) {
                                navController.popBackStack()
                            }
                        }
                        "about" -> {
                            if (currentRoute?.contains("Home") == true) {
                                navController.navigate(Screen.About)
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

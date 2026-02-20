package com.example.receptionkiosk.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pointerinput.pointerInput
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.receptionkiosk.data.prefs.SettingsPreferences
import com.example.receptionkiosk.feature.admin.AdminPinScreen
import com.example.receptionkiosk.feature.form.FormScreen
import com.example.receptionkiosk.feature.home.HomeScreen
import com.example.receptionkiosk.feature.settings.SettingsScreen
import com.example.receptionkiosk.feature.status.StatusScreen
import com.example.receptionkiosk.feature.success.SuccessScreen
import kotlinx.coroutines.delay

@Composable
fun AppNavHost() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val prefs = remember { SettingsPreferences(context) }
    val idleTimeoutSeconds by prefs.idleTimeoutSeconds.collectAsStateWithLifecycle(initialValue = 60)
    var interactionTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    LaunchedEffect(idleTimeoutSeconds, interactionTick, currentRoute) {
        if (idleTimeoutSeconds <= 0) return@LaunchedEffect
        delay(idleTimeoutSeconds * 1000L)
        if (currentRoute != Routes.Home) {
            navController.navigate(Routes.Home) {
                popUpTo(Routes.Home) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        interactionTick = System.currentTimeMillis()
                    }
                }
            }
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.Home
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    onOpenForm = { purposeId -> navController.navigate(Routes.form(purposeId)) },
                    onOpenAdmin = { navController.navigate(Routes.AdminPin) }
                )
            }

            composable(
                route = Routes.Form,
                arguments = listOf(navArgument("purposeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val purposeId = backStackEntry.arguments?.getLong("purposeId") ?: 0L
                FormScreen(
                    purposeId = purposeId,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.navigate(Routes.Success) }
                )
            }

            composable(Routes.Success) {
                SuccessScreen(onDone = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = false }
                        launchSingleTop = true
                    }
                })
            }

            composable(Routes.AdminPin) {
                AdminPinScreen(
                    onBack = { navController.popBackStack() },
                    onPinSuccess = { navController.navigate(Routes.Settings) }
                )
            }

            composable(Routes.Settings) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenStatus = { navController.navigate(Routes.Status) }
                )
            }

            composable(Routes.Status) {
                StatusScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

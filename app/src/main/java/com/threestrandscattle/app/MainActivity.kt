package com.threestrandscattle.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.threestrandscattle.app.services.SaleStore
import com.threestrandscattle.app.ui.navigation.AppNavigation
import com.threestrandscattle.app.ui.screens.LaunchScreen
import com.threestrandscattle.app.ui.screens.OnboardingScreen
import com.threestrandscattle.app.ui.theme.ThemeColors
import com.threestrandscattle.app.ui.theme.ThreeStrandsTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ThreeStrandsTheme {
                val store: SaleStore = viewModel(factory = SaleStore.Factory(applicationContext))
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

                var isLaunching by remember { mutableStateOf(true) }
                var hasCompletedOnboarding by remember {
                    mutableStateOf(prefs.getBoolean("has_completed_onboarding", false))
                }

                LaunchedEffect(Unit) {
                    delay(3000)
                    isLaunching = false
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ThemeColors.Background
                ) {
                    when {
                        isLaunching -> LaunchScreen()
                        !hasCompletedOnboarding -> OnboardingScreen(
                            onComplete = {
                                hasCompletedOnboarding = true
                                prefs.edit().putBoolean("has_completed_onboarding", true).apply()
                            }
                        )
                        else -> AppNavigation(store = store)
                    }
                }
            }
        }
    }
}

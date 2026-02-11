package com.threestrandscattle.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threestrandscattle.app.R
import com.threestrandscattle.app.services.LocationService
import com.threestrandscattle.app.services.NotificationService
import com.threestrandscattle.app.ui.theme.ThemeColors
import com.threestrandscattle.app.ui.theme.ThemeDimens
import com.threestrandscattle.app.ui.theme.ThemeTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    // Chain permissions: notification first, then location in its callback
    var pendingLocationRequest by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        LocationService.getInstance(context).setAuthorized(granted)
        onComplete()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        NotificationService.getInstance(context).setAuthorized(granted)
        // Now request location permission
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> FlashSalesPage()
                    2 -> PermissionsPage(
                        onEnableAll = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Request notification first; its callback chains to location
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                // Pre-Tiramisu: no notification permission needed, go straight to location
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        onSkip = onComplete
                    )
                }
            }

            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) ThemeColors.Primary
                                else ThemeColors.TextSecondary.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Next button (pages 0 and 1 only)
            if (pagerState.currentPage < 2) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ThemeDimens.ScreenPadding)
                        .padding(bottom = 40.dp),
                    shape = RoundedCornerShape(ThemeDimens.CornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThemeColors.Primary
                    )
                ) {
                    Text(
                        "Next",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = "3 Strands",
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to\n3 Strands Cattle Co.",
            style = ThemeTypography.HeroFont,
            color = ThemeColors.Primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Veteran owned. Faith driven.\nFlorida sourced.",
            style = ThemeTypography.SubheadingFont,
            color = ThemeColors.Primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Premium beef delivered straight from Florida ranches to your door.",
            style = ThemeTypography.BodyFont,
            color = ThemeColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(2f))
    }
}

@Composable
private fun FlashSalesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(ThemeColors.BronzeGold.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = ThemeColors.BronzeGold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Flash Sales",
            style = ThemeTypography.HeroFont,
            color = ThemeColors.Primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Deals that don't last long.",
            style = ThemeTypography.SubheadingFont,
            color = ThemeColors.Primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Get exclusive access to limited-time offers on premium cuts, bundles, and seasonal specials.",
            style = ThemeTypography.BodyFont,
            color = ThemeColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(2f))
    }
}

@Composable
private fun PermissionsPage(onEnableAll: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(ThemeColors.Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = (-12).dp, y = (-8).dp),
                tint = ThemeColors.Primary
            )
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .offset(x = 22.dp, y = 18.dp),
                tint = ThemeColors.BronzeGold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Stay in the Loop",
            style = ThemeTypography.HeroFont,
            color = ThemeColors.Primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This is how we reach you.",
            style = ThemeTypography.SubheadingFont,
            color = ThemeColors.Primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enable notifications for flash sales, pop-up locations, and announcements. Allow location access so we can alert you when we're selling near you.",
            style = ThemeTypography.BodyFont,
            color = ThemeColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onEnableAll,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeDimens.ScreenPadding),
            shape = RoundedCornerShape(ThemeDimens.CornerRadius),
            colors = ButtonDefaults.buttonColors(containerColor = ThemeColors.Primary)
        ) {
            Text(
                "Enable All",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ThemeDimens.ScreenPadding),
            shape = RoundedCornerShape(ThemeDimens.CornerRadius),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ThemeColors.TextSecondary),
            border = androidx.compose.foundation.BorderStroke(2.dp, ThemeColors.TextSecondary)
        ) {
            Text(
                "Maybe Later",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

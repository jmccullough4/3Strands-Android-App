package com.threestrandscattle.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threestrandscattle.app.R
import com.threestrandscattle.app.ui.theme.ThemeColors
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LaunchScreen() {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current

    // Logo animation states
    var logoLanded by remember { mutableStateOf(false) }
    val logoOffsetY by animateFloatAsState(
        targetValue = if (logoLanded) 0f else -800f,
        animationSpec = tween(350, easing = FastOutLinearInEasing),
        label = "logoOffset"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (logoLanded) 1f else 1.8f,
        animationSpec = if (logoLanded) spring(dampingRatio = 0.5f, stiffness = 300f)
        else tween(350),
        label = "logoScale"
    )

    // Flash effect
    var showFlash by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(
        targetValue = if (showFlash) 0.3f else 0f,
        animationSpec = tween(80),
        label = "flash"
    )

    // Ripple states
    var rippleTriggered by remember { mutableStateOf(false) }

    // Text reveal
    var showText by remember { mutableStateOf(false) }
    val textAlpha by animateFloatAsState(
        targetValue = if (showText) 1f else 0f,
        animationSpec = tween(600),
        label = "textAlpha"
    )

    // Droplets
    var showDroplets by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Phase 1: Logo slams down
        delay(100)
        logoLanded = true

        // Phase 2: Impact effects
        delay(350)
        showFlash = true
        showDroplets = true
        rippleTriggered = true
        delay(80)
        showFlash = false

        // Phase 3: Text reveal
        delay(400)
        showText = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Ripple rings
        if (rippleTriggered) {
            RippleRings()
        }

        // Flash overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = flashAlpha))
        )

        // Splash droplets
        if (showDroplets) {
            SplashDroplets()
        }

        // Main content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo
            Box(
                modifier = Modifier
                    .offset(y = with(density) { logoOffsetY.toDp() })
                    .scale(logoScale)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.copper_logo),
                    contentDescription = "3 Strands Cattle Co.",
                    modifier = Modifier.size(188.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Scripture quote
            Column(
                modifier = Modifier
                    .alpha(textAlpha)
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\"A cord of three strands is\nnot quickly broken.\"",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp,
                    color = ThemeColors.BronzeGold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ecclesiastes 4:12",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RippleRings() {
    data class RippleState(
        val targetScale: Float,
        val color: Color,
        val strokeWidth: Float,
        val delayMs: Int,
        val durationMs: Int
    )

    val ripples = listOf(
        RippleState(4f, ThemeColors.Copper.copy(alpha = 0.5f), 2.5f, 0, 1200),
        RippleState(4.5f, ThemeColors.Bronze.copy(alpha = 0.4f), 2f, 100, 1300),
        RippleState(5f, ThemeColors.BronzeGold.copy(alpha = 0.35f), 1.5f, 200, 1400),
        RippleState(5.5f, ThemeColors.Copper.copy(alpha = 0.25f), 1.2f, 350, 1500),
        RippleState(6f, ThemeColors.Bronze.copy(alpha = 0.15f), 1f, 500, 1600)
    )

    ripples.forEachIndexed { index, ripple ->
        var triggered by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (triggered) ripple.targetScale else 0.1f,
            animationSpec = tween(ripple.durationMs, easing = FastOutSlowInEasing),
            label = "rippleScale$index"
        )
        val alpha by animateFloatAsState(
            targetValue = if (triggered) 0f else 0f,
            animationSpec = tween(ripple.durationMs, easing = LinearEasing),
            label = "rippleAlpha$index"
        )

        var currentAlpha by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(Unit) {
            delay(ripple.delayMs.toLong())
            triggered = true
            // Fade in then out
            val fadeInDuration = 300L
            val holdDuration = 100L
            val steps = 30
            for (i in 0..steps) {
                currentAlpha = (i.toFloat() / steps) * 0.7f
                delay(fadeInDuration / steps)
            }
            delay(holdDuration)
            val fadeOutSteps = 40
            for (i in fadeOutSteps downTo 0) {
                currentAlpha = (i.toFloat() / fadeOutSteps) * 0.7f
                delay(ripple.durationMs.toLong() / fadeOutSteps)
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = ripple.color.copy(alpha = currentAlpha),
                radius = 100.dp.toPx() * scale,
                center = Offset(size.width / 2, size.height / 2 - 40.dp.toPx()),
                style = Stroke(width = ripple.strokeWidth.dp.toPx())
            )
        }
    }
}

@Composable
private fun SplashDroplets() {
    val numDroplets = 12
    val colors = listOf(ThemeColors.Copper, ThemeColors.BronzeGold)

    for (i in 0 until numDroplets) {
        val angle = remember { i * (360.0 / numDroplets) * Math.PI / 180.0 }
        val distance = remember { Random.nextFloat() * 100f + 80f }
        val dropletSize = remember { Random.nextFloat() * 4f + 3f }
        val burstDuration = remember { (Random.nextFloat() * 300 + 400).toInt() }
        val fallDelay = remember { (Random.nextFloat() * 200 + 300).toInt() }

        var burst by remember { mutableStateOf(false) }
        var falling by remember { mutableStateOf(false) }

        val offsetX by animateFloatAsState(
            targetValue = if (burst) (cos(angle) * distance).toFloat() else 0f,
            animationSpec = tween(burstDuration, easing = FastOutSlowInEasing),
            label = "dropX$i"
        )
        val offsetY by animateFloatAsState(
            targetValue = if (falling) (sin(angle) * distance).toFloat() + 50f
            else if (burst) (sin(angle) * distance).toFloat() - 40f
            else 0f,
            animationSpec = if (falling) tween(400, easing = FastOutLinearInEasing)
            else tween(burstDuration, easing = FastOutSlowInEasing),
            label = "dropY$i"
        )
        val dropAlpha by animateFloatAsState(
            targetValue = if (falling) 0f else if (burst) 0.8f else 0f,
            animationSpec = tween(if (falling) 400 else burstDuration),
            label = "dropAlpha$i"
        )

        LaunchedEffect(Unit) {
            burst = true
            delay(fallDelay.toLong())
            falling = true
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = colors[i % 2].copy(alpha = dropAlpha),
                radius = dropletSize.dp.toPx(),
                center = Offset(
                    size.width / 2 + offsetX.dp.toPx(),
                    size.height / 2 - 40.dp.toPx() + offsetY.dp.toPx()
                )
            )
        }
    }
}

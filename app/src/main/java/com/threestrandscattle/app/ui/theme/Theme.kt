package com.threestrandscattle.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Brand Colors matching iOS Theme.swift
object ThemeColors {
    val Copper = Color(0xFFC07A3E)
    val Bronze = Color(0xFF9F714A)
    val BronzeGold = Color(0xFFD4A054)
    val Background = Color(0xFF0D0D0D)
    val CardBackground = Color(0xFF1C1C1E)
    val TextPrimary = Color(0xFFF2EDE8)
    val TextSecondary = Color(0xFF9A9088)

    // Semantic aliases
    val Primary = Copper
    val Secondary = Bronze
    val Accent = BronzeGold
}

// Typography matching iOS Theme.swift
object ThemeTypography {
    val HeroFont = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    )
    val HeadingFont = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    )
    val SubheadingFont = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp
    )
    val BodyFont = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    )
    val CaptionFont = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    )
}

// Dimensions matching iOS Theme.swift
object ThemeDimens {
    val CornerRadius = 14.dp
    val CardPadding = 16.dp
    val ScreenPadding = 20.dp
}

private val DarkColorScheme = darkColorScheme(
    primary = ThemeColors.Copper,
    secondary = ThemeColors.Bronze,
    tertiary = ThemeColors.BronzeGold,
    background = ThemeColors.Background,
    surface = ThemeColors.CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = ThemeColors.TextPrimary,
    onSurface = ThemeColors.TextPrimary,
    surfaceVariant = ThemeColors.CardBackground,
    onSurfaceVariant = ThemeColors.TextSecondary
)

@Composable
fun ThreeStrandsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

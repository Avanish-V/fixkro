package com.iotabuild.zippyhand.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MainBlue,
    secondary = LightBlue,
    tertiary = StarGold,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = MainBlue,
    secondary = LightBlue,
    tertiary = StarGold,
    background = BackgroundColor,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = MainBlue,
    onBackground = DarkText,
    onSurface = DarkText,
    outline = BorderColor
)

@Composable
fun ZippyhandTheme(
    darkTheme: Boolean = false, // Forced to false to disable dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Always use LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

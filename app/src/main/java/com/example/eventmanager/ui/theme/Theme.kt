package com.example.eventmanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF9D351C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBD0),
    onPrimaryContainer = Color(0xFF3B0901),
    secondary = Color(0xFF77574E),
    secondaryContainer = Color(0xFFFFDBD0),
    tertiary = Color(0xFF6C5D2F),
    background = Color(0xFFFFF8F6),
    surface = Color(0xFFFFF8F6),
    surfaceVariant = Color(0xFFF5DED7)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB5A0),
    onPrimary = Color(0xFF5F1606),
    primaryContainer = Color(0xFF7E2812),
    secondary = Color(0xFFE7BDB1),
    tertiary = Color(0xFFD8C679)
)

@Composable
fun EventManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}

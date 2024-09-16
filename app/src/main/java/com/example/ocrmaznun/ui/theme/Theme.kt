package com.example.ocrmaznun.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    secondary = DarkRed,
    tertiary = DarkBlue,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = LightBackground,
    onSecondary = WhitePrimary,
    onTertiary = LightBackground,
    onBackground = Gray,
    onSurface = Gray
)

private val LightColorScheme = lightColorScheme(
    primary = RedPrimary,
    secondary = DarkRed,
    tertiary = DarkBlue,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Black,
    onSecondary = WhitePrimary,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black
)

@Composable
fun OcrMaznunTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

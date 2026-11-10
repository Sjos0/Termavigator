package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Termavigator Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = TermavigatorYellow,
    onPrimary = TermavigatorBlack,
    primaryContainer = TermavigatorSurfaceLight,
    onPrimaryContainer = TermavigatorBlueDark,
    secondary = TermavigatorBlueLight,
    onSecondary = TermavigatorBlueDark,
    background = TermavigatorWhite,
    onBackground = TermavigatorBlack,
    surface = TermavigatorSurfaceLight,
    onSurface = TermavigatorBlack,
    surfaceVariant = TermavigatorWhite,
    onSurfaceVariant = TermavigatorTextSecondaryLight,
    outline = TermavigatorCardBorderLight
)

// Termavigator Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = TermavigatorYellow,
    onPrimary = TermavigatorBlack,
    primaryContainer = TermavigatorSurfaceDark,
    onPrimaryContainer = TermavigatorWhite,
    secondary = TermavigatorBlueLight,
    onSecondary = TermavigatorWhite,
    background = TermavigatorBlack,
    onBackground = TermavigatorWhite,
    surface = TermavigatorSurfaceDark,
    onSurface = TermavigatorWhite,
    surfaceVariant = Color(0xFF19253C),
    onSurfaceVariant = TermavigatorTextSecondaryDark,
    outline = TermavigatorCardBorderDark
)

val TermavigatorShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun TermavigatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: Color = TermavigatorYellow,
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = baseScheme.copy(
        primary = accentColor,
        onPrimary = if (accentColor == TermavigatorBlueDark || accentColor == TermavigatorBlack) TermavigatorWhite else TermavigatorBlack
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = TermavigatorShapes,
        content = content
    )
}

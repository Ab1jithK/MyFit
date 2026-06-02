package com.myfit.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MyFitColorScheme = darkColorScheme(
    background = Black,
    surface = Surface1,
    surfaceVariant = Surface2,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    primary = Performance,
    onPrimary = Black,
    secondary = Active,
    onSecondary = Black,
    tertiary = Caution,
    onTertiary = Black,
    error = Warning,
    onError = Black,
    outline = Border,
    outlineVariant = Surface2
)

@Composable
fun MyFitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MyFitColorScheme,
        typography = MyFitTypography,
        content = content
    )
}

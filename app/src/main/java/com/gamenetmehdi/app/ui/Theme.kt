package com.gamenetmehdi.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object VenueColors {
    val Slate950 = Color(0xFF020617)
    val Slate900 = Color(0xFF0F172A)
    val Slate800 = Color(0xFF1E293B)
    val Slate700 = Color(0xFF334155)
    val Slate400 = Color(0xFF94A3B8)
    val Slate300 = Color(0xFFCBD5E1)
    val Emerald500 = Color(0xFF10B981)
    val Emerald400 = Color(0xFF34D399)
    val Emerald300 = Color(0xFF6EE7B7)
    val Emerald200 = Color(0xFFA7F3D0)
    val Emerald100 = Color(0xFFD1FAE5)
    val Red500 = Color(0xFFEF4444)
    val Red300 = Color(0xFFFCA5A5)
    val Red100 = Color(0xFFFEE2E2)
    val Cyan400 = Color(0xFF22D3EE)
}

private val VenueColorScheme = darkColorScheme(
    primary = VenueColors.Emerald500,
    onPrimary = VenueColors.Slate950,
    secondary = VenueColors.Cyan400,
    onSecondary = VenueColors.Slate950,
    background = VenueColors.Slate950,
    onBackground = Color.White,
    surface = VenueColors.Slate900,
    onSurface = Color.White,
    surfaceVariant = VenueColors.Slate800,
    onSurfaceVariant = VenueColors.Slate300,
    outline = VenueColors.Slate700,
    outlineVariant = VenueColors.Slate800,
    error = VenueColors.Red500,
)

@Composable
fun VenueTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = VenueColorScheme, content = content)
}

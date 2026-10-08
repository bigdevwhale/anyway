package io.cyberdise.anyway.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF0E0D0C)
val Coal = Color(0xFF1A1816)
val Smoke = Color(0xFF2A2724)
val Bone = Color(0xFFF2EDE4)
val Ash = Color(0xFF8C857B)
val Ember = Color(0xFFFF7A3D)

/** Always dark: tea on the balcony at the end of the world. */
@Composable
fun AnywayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Ember,
            onPrimary = Ink,
            secondary = Ember,
            onSecondary = Ink,
            background = Ink,
            onBackground = Bone,
            surface = Ink,
            onSurface = Bone,
            surfaceVariant = Coal,
            onSurfaceVariant = Ash,
            surfaceContainer = Coal,
            surfaceContainerHigh = Coal,
            surfaceContainerHighest = Smoke,
            surfaceContainerLow = Coal,
            outline = Smoke,
            outlineVariant = Smoke,
        ),
        content = content,
    )
}

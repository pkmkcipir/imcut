package com.editor.design.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * imcut Design Tokens (© iman.mn_)
 * Warm obsidian studio canvas + coral-tangerine accent (#FF8552).
 */
@Immutable
data class StudioColors(
    val bg: Color = Color(0xFF0E0D0C),
    val surface: Color = Color(0xFF171614),
    val surfaceElevated: Color = Color(0xFF23211E),
    val border: Color = Color(0xFF33302B),
    val borderInput: Color = Color(0xFF7C756B),
    val text: Color = Color(0xFFF7F5F2),
    val textMuted: Color = Color(0xFFB0A89F),
    val accent: Color = Color(0xFFFF8552),
    val onAccent: Color = Color(0xFF0E0D0C),
    val danger: Color = Color(0xFFF87171),
    val onDanger: Color = Color(0xFF0E0D0C),
    val success: Color = Color(0xFF4ADE80),
    val warning: Color = Color(0xFFFBBF24),
    val onWarning: Color = Color(0xFF0E0D0C),
    val trackAudio: Color = Color(0xFF14532D),
    val trackText: Color = Color(0xFF7C2D12),
    val trackEffect: Color = Color(0xFF3B2F63),
    val playhead: Color = Color(0xFFF7F5F2),
)

object StudioRadius {
    val sm = 6.dp
    val md = 10.dp
    val lg = 16.dp
    val sheet = 20.dp
    val pill = 999.dp
}

object StudioSpace {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object StudioLayout {
    val minTouchTarget = 48.dp
    val topBarHeight = 52.dp
    val bottomToolbarHeight = 64.dp
    val bottomNavHeight = 64.dp
    val mainTrackHeight = 48.dp
    val secondaryTrackHeight = 32.dp
}

val LocalStudioColors = staticCompositionLocalOf { StudioColors() }

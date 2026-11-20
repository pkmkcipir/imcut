package com.editor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editor.design.tokens.LocalStudioColors
import com.editor.design.tokens.StudioRadius
import com.editor.design.tokens.StudioSpace
import com.editor.model.AspectRatio

data class TransitionPreset(
    val id: String,
    val name: String,
    val isPro: Boolean = false,
)

enum class CanvasBackdrop(val label: String) {
    SOLID_DARK("Obsidian"),
    SOLID_WARM("Abu Hangat"),
    BLUR("Kabur (Blur)"),
}

/**
 * S11 — Aspect Ratio & Canvas Backdrop Sheet
 */
@Composable
fun RatioCanvasSheet(
    currentRatio: AspectRatio,
    currentBackdrop: CanvasBackdrop,
    onSelectRatio: (AspectRatio) -> Unit,
    onSelectBackdrop: (CanvasBackdrop) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalStudioColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = StudioRadius.sheet, topEnd = StudioRadius.sheet))
            .background(colors.surface)
            .padding(StudioSpace.lg),
        verticalArrangement = Arrangement.spacedBy(StudioSpace.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Rasio & Kanvas (S11)", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onDismiss) { Text("Selesai", color = colors.accent) }
        }

        Text("Rasio Aspek", color = colors.textMuted, fontSize = 12.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(StudioSpace.sm)) {
            items(AspectRatio.entries) { ratio ->
                FilterChip(
                    selected = currentRatio == ratio,
                    onClick = { onSelectRatio(ratio) },
                    label = { Text(ratio.label, fontSize = 13.sp) },
                )
            }
        }

        Text("Latar Kanvas", color = colors.textMuted, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(StudioSpace.sm)) {
            CanvasBackdrop.entries.forEach { bg ->
                FilterChip(
                    selected = currentBackdrop == bg,
                    onClick = { onSelectBackdrop(bg) },
                    label = { Text(bg.label, fontSize = 12.sp) },
                )
            }
        }
    }
}

/**
 * S05 — Transition Picker Sheet between two adjacent clips
 */
@Composable
fun TransitionSheet(
    currentPresetId: String?,
    currentDurationSec: Float,
    onApplyTransition: (presetId: String?, durationSec: Float, applyToAll: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalStudioColors.current
    val presets = remember {
        listOf(
            TransitionPreset("none", "Tanpa Transisi", false),
            TransitionPreset("dissolve", "Cross Dissolve", false),
            TransitionPreset("slide_left", "Slide Kiri", false),
            TransitionPreset("fade_black", "Fade Hitam", false),
            TransitionPreset("zoom_blur", "Zoom Blur", true),
        )
    }
    var selectedId by remember { mutableStateOf(currentPresetId ?: "none") }
    var durationSec by remember { mutableFloatStateOf(currentDurationSec.coerceIn(0.1f, 2.0f)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = StudioRadius.sheet, topEnd = StudioRadius.sheet))
            .background(colors.surface)
            .padding(StudioSpace.lg),
        verticalArrangement = Arrangement.spacedBy(StudioSpace.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Transisi Klip (S05)", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onDismiss) { Text("Tutup", color = colors.textMuted) }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(StudioSpace.sm)) {
            items(presets, key = { it.id }) { preset ->
                val isSelected = selectedId == preset.id
                Column(
                    modifier = Modifier
                        .width(96.dp)
                        .clip(RoundedCornerShape(StudioRadius.md))
                        .background(colors.surfaceElevated)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) colors.accent else colors.border,
                            shape = RoundedCornerShape(StudioRadius.md),
                        )
                        .clickable { selectedId = preset.id }
                        .padding(StudioSpace.sm),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (preset.isPro) {
                        Text(
                            "PRO",
                            color = colors.onWarning,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(StudioRadius.pill))
                                .background(colors.warning)
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                        )
                    }
                    Text(preset.name, color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        if (selectedId != "none") {
            Text("Durasi: ${"%.1f".format(durationSec)} detik", color = colors.textMuted, fontSize = 12.sp)
            Slider(
                value = durationSec,
                onValueChange = { durationSec = it },
                valueRange = 0.1f..2.0f,
                modifier = Modifier.semantics { contentDescription = "Durasi transisi" },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(StudioSpace.sm),
        ) {
            OutlinedButton(
                onClick = {
                    onApplyTransition(if (selectedId == "none") null else selectedId, durationSec, true)
                },
                modifier = Modifier.weight(1f).height(48.dp),
            ) {
                Text("Terapkan Semua", color = colors.text, fontSize = 13.sp)
            }
            Button(
                onClick = {
                    onApplyTransition(if (selectedId == "none") null else selectedId, durationSec, false)
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
            ) {
                Text("Terapkan", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * S06 — Clip Speed Sheet (Normal 0.1x–100x + Keep Pitch toggle)
 */
@Composable
fun SpeedSheet(
    clipLabel: String,
    currentSpeed: Float,
    keepPitch: Boolean,
    onApplySpeed: (speed: Float, keepPitch: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalStudioColors.current
    var speed by remember { mutableFloatStateOf(currentSpeed.coerceIn(0.1f, 10.0f)) }
    var preservePitch by remember { mutableStateOf(keepPitch) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = StudioRadius.sheet, topEnd = StudioRadius.sheet))
            .background(colors.surface)
            .padding(StudioSpace.lg),
        verticalArrangement = Arrangement.spacedBy(StudioSpace.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Kecepatan • $clipLabel (S06)", color = colors.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onDismiss) { Text("Batal", color = colors.textMuted) }
        }

        Text("Kecepatan: ${"%.2f".format(speed)}x", color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Slider(
            value = speed,
            onValueChange = { speed = it },
            valueRange = 0.2f..4.0f,
            modifier = Modifier.semantics { contentDescription = "Pengatur kecepatan klip" },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(StudioSpace.sm)) {
            listOf(0.5f, 1.0f, 1.5f, 2.0f, 4.0f).forEach { preset ->
                FilterChip(
                    selected = kotlin.math.abs(speed - preset) < 0.05f,
                    onClick = { speed = preset },
                    label = { Text("${preset}x", fontSize = 12.sp) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(checked = preservePitch, onCheckedChange = { preservePitch = it })
                Text("Pertahankan pitch audio", color = colors.text, fontSize = 13.sp)
            }
            Button(
                onClick = { onApplySpeed(speed, preservePitch) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
            ) {
                Text("Terapkan", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

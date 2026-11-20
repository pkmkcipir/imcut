package com.editor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.delay

enum class AudioSourceKind(val label: String) {
    LOCAL("Audio Lokal"),
    EXTRACTED("Ekstrak Video"),
    VOICEOVER("Rekam Suara"),
}

data class AudioTrackItem(
    val id: String,
    val title: String,
    val kind: AudioSourceKind,
    val startMs: Long,
    val durationMs: Long,
    val volume: Float = 1.0f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
)

data class TextTrackItem(
    val id: String,
    val text: String,
    val startMs: Long,
    val durationMs: Long = 3000L,
    val fontName: String = "Inter",
    val strokeEnabled: Boolean = true,
    val isAutoCaption: Boolean = false,
)

/**
 * S07 — Audio Drawer Sheet (Local Audio, Extract Audio, Voiceover 3-2-1, Volume & Fade)
 */
@Composable
fun AudioDrawerSheet(
    selectedAudio: AudioTrackItem?,
    micPermissionDenied: Boolean = false,
    onAddLocalAudio: (title: String, durationMs: Long) -> Unit,
    onExtractAudioFromVideo: () -> Unit,
    onSaveVoiceover: (durationMs: Long) -> Unit,
    onUpdateAudio: (AudioTrackItem) -> Unit,
    onDeleteAudio: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalStudioColors.current
    var activeTab by remember { mutableStateOf(AudioSourceKind.LOCAL) }
    var countdown by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var recordedMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            if (countdown == 1) {
                countdown = 0
                isRecording = true
                recordedMs = 0L
            } else {
                countdown -= 1
            }
        }
    }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(100L)
            recordedMs += 100L
        }
    }

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
            Text("Audio (S07)", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onDismiss) { Text("Selesai", color = colors.accent) }
        }

        if (selectedAudio != null) {
            Text(selectedAudio.title, color = colors.text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text("Volume: ${(selectedAudio.volume * 100).toInt()}%", color = colors.textMuted, fontSize = 12.sp)
            Slider(
                value = selectedAudio.volume,
                onValueChange = { onUpdateAudio(selectedAudio.copy(volume = it)) },
                valueRange = 0f..2f,
                modifier = Modifier.semantics { contentDescription = "Pengatur volume audio" },
            )
            Text(
                "Fade In: ${selectedAudio.fadeInMs / 1000f}s • Fade Out: ${selectedAudio.fadeOutMs / 1000f}s",
                color = colors.textMuted,
                fontSize = 12.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(StudioSpace.sm)) {
                FilterChip(
                    selected = selectedAudio.fadeInMs > 0L,
                    onClick = { onUpdateAudio(selectedAudio.copy(fadeInMs = if (selectedAudio.fadeInMs > 0L) 0L else 1000L)) },
                    label = { Text("Fade In 1s") },
                )
                FilterChip(
                    selected = selectedAudio.fadeOutMs > 0L,
                    onClick = { onUpdateAudio(selectedAudio.copy(fadeOutMs = if (selectedAudio.fadeOutMs > 0L) 0L else 1000L)) },
                    label = { Text("Fade Out 1s") },
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { onDeleteAudio(selectedAudio.id) }) {
                    Text("Hapus", color = colors.danger)
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(StudioSpace.sm)) {
                AudioSourceKind.entries.forEach { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        label = { Text(tab.label, fontSize = 12.sp) },
                    )
                }
            }

            when (activeTab) {
                AudioSourceKind.LOCAL -> {
                    listOf("Lo-Fi Pagi (Bebas Royalti)" to 12000L, "Beat Produk UMKM" to 9500L).forEach { (title, dur) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(StudioRadius.md))
                                .background(colors.surfaceElevated)
                                .clickable { onAddLocalAudio(title, dur) }
                                .padding(StudioSpace.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(title, color = colors.text, fontSize = 14.sp)
                            Text("+ Pakai", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                AudioSourceKind.EXTRACTED -> {
                    Button(
                        onClick = onExtractAudioFromVideo,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                    ) {
                        Text("Ekstrak Audio dari Video Galeri")
                    }
                }
                AudioSourceKind.VOICEOVER -> {
                    if (micPermissionDenied) {
                        Text("Izin mikrofon diperlukan untuk merekam suara di imcut.", color = colors.danger, fontSize = 13.sp)
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(StudioSpace.sm),
                        ) {
                            val statusLabel = when {
                                countdown > 0 -> "Mulai dalam $countdown..."
                                isRecording -> "Merekam ${recordedMs / 1000}.${(recordedMs % 1000) / 100} detik"
                                else -> "Ketuk tombol di bawah untuk mulai rekam suara"
                            }
                            Text(statusLabel, color = colors.textMuted, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(if (isRecording) colors.danger else colors.accent)
                                    .clickable {
                                        if (isRecording) {
                                            isRecording = false
                                            if (recordedMs >= 500L) onSaveVoiceover(recordedMs)
                                        } else if (countdown == 0) {
                                            countdown = 3
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    if (isRecording) "Stop" else "Rekam",
                                    color = colors.onAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * S08 — Text & Captions Sheet (Manual Styled Text + Auto Captions Credit Preview)
 */
@Composable
fun TextOverlaySheet(
    initialText: String = "",
    onSaveText: (text: String, fontName: String, strokeEnabled: Boolean) -> Unit,
    onRequestAutoCaptions: (languageCode: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalStudioColors.current
    var textValue by remember { mutableStateOf(initialText) }
    var selectedFont by remember { mutableStateOf("Inter") }
    var strokeEnabled by remember { mutableStateOf(true) }
    var showAutoCaptions by remember { mutableStateOf(false) }

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
            Text("Teks & Subtitle (S08)", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onDismiss) { Text("Tutup", color = colors.textMuted) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(StudioSpace.sm)) {
            FilterChip(selected = !showAutoCaptions, onClick = { showAutoCaptions = false }, label = { Text("Teks Manual") })
            FilterChip(selected = showAutoCaptions, onClick = { showAutoCaptions = true }, label = { Text("Auto Captions") })
        }

        if (!showAutoCaptions) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { if (it.length <= 120) textValue = it },
                label = { Text("Tulis teks di video") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(StudioSpace.sm)) {
                items(listOf("Inter", "JetBrains Mono", "Serif")) { font ->
                    FilterChip(
                        selected = selectedFont == font,
                        onClick = { selectedFont = font },
                        label = { Text(font, fontSize = 12.sp) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(checked = strokeEnabled, onCheckedChange = { strokeEnabled = it })
                    Text("Garis tepi (Stroke)", color = colors.text, fontSize = 13.sp)
                }
                Button(
                    enabled = textValue.isNotBlank(),
                    onClick = { onSaveText(textValue.trim(), selectedFont, strokeEnabled) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                ) {
                    Text("Tambah ke Timeline")
                }
            }
        } else {
            Text(
                "Transkripsi otomatis memotong 1 kredit/menit audio. Kredit otomatis kembali jika proses gagal.",
                color = colors.textMuted,
                fontSize = 12.sp,
            )
            Button(
                onClick = { onRequestAutoCaptions("id") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
            ) {
                Text("Buat Auto Captions (Bahasa Indonesia)")
            }
        }
    }
}

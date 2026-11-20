package com.editor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editor.design.tokens.LocalStudioColors
import com.editor.design.tokens.StudioLayout
import com.editor.design.tokens.StudioRadius
import com.editor.design.tokens.StudioSpace
import com.editor.model.*
import kotlinx.coroutines.delay

/**
 * S01 — Home / Draft List Screen (empty, filled, loading, storage warning)
 */
@Composable
fun HomeScreen(
    projects: List<VideoProject>,
    isLoading: Boolean = false,
    onNewProject: () -> Unit,
    onOpenProject: (String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = LocalStudioColors.current
    Scaffold(
        containerColor = colors.bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(StudioLayout.topBarHeight)
                    .padding(horizontal = StudioSpace.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("imcut", color = colors.text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onOpenSettings) {
                    Text("Pengaturan", color = colors.textMuted)
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = StudioSpace.lg),
            verticalArrangement = Arrangement.spacedBy(StudioSpace.lg),
        ) {
            Button(
                onClick = onNewProject,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(StudioRadius.lg),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
            ) {
                Text("+ Proyek Baru", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Text("Draf Proyek", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

            when {
                isLoading -> {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .clip(RoundedCornerShape(StudioRadius.md))
                                .background(colors.surface)
                        )
                    }
                }
                projects.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(StudioRadius.lg))
                            .background(colors.surface)
                            .padding(StudioSpace.xl),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Belum ada proyek. Ketuk Proyek Baru untuk mulai menyunting video.",
                            color = colors.textMuted,
                            fontSize = 14.sp,
                        )
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(StudioSpace.sm)) {
                        items(projects, key = { it.id }) { project ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(StudioRadius.md))
                                    .background(colors.surface)
                                    .border(1.dp, colors.border, RoundedCornerShape(StudioRadius.md))
                                    .clickable { onOpenProject(project.id) }
                                    .padding(StudioSpace.md),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        project.title,
                                        color = colors.text,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        "${formatMs(project.totalDurationMs)} • ${project.storageBytes / 1_000_000} MB",
                                        color = colors.textMuted,
                                        fontSize = 12.sp,
                                    )
                                }
                                TextButton(onClick = { onDeleteProject(project.id) }) {
                                    Text("Hapus", color = colors.danger, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * S02 — Media Picker Screen (multi-select order, permission denied, empty)
 */
@Composable
fun MediaPickerScreen(
    mediaItems: List<MediaAssetItem>,
    permissionDenied: Boolean = false,
    onBack: () -> Unit,
    onConfirmSelection: (List<MediaAssetItem>) -> Unit,
) {
    val colors = LocalStudioColors.current
    var selectedIds by remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(StudioLayout.topBarHeight)
                    .padding(horizontal = StudioSpace.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onBack) { Text("Batal", color = colors.textMuted) }
                Text("Pilih Media", color = colors.text, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(48.dp))
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(StudioSpace.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${selectedIds.size} klip dipilih", color = colors.textMuted, fontSize = 14.sp)
                Button(
                    enabled = selectedIds.isNotEmpty(),
                    onClick = {
                        val ordered = selectedIds.mapNotNull { id -> mediaItems.find { it.id == id } }
                        onConfirmSelection(ordered)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                ) {
                    Text("Tambah (${selectedIds.size})")
                }
            }
        },
    ) { padding ->
        if (permissionDenied) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Izin galeri diperlukan untuk memuat video Anda.", color = colors.textMuted)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(StudioSpace.sm),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(padding),
            ) {
                items(mediaItems, key = { it.id }) { item ->
                    val orderIdx = selectedIds.indexOf(item.id)
                    val isSelected = orderIdx >= 0
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(StudioRadius.sm))
                            .background(colors.surfaceElevated)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) colors.accent else colors.border,
                                shape = RoundedCornerShape(StudioRadius.sm),
                            )
                            .clickable {
                                selectedIds = if (isSelected) selectedIds - item.id else selectedIds + item.id
                            }
                            .padding(StudioSpace.sm),
                    ) {
                        Text(item.title, color = colors.text, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("${orderIdx + 1}", color = colors.onAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            formatMs(item.durationMs),
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.BottomEnd),
                        )
                    }
                }
            }
        }
    }
}

/**
 * S03 + S04 + S12 — Timeline Editor Workspace, Contextual Clip Edit, and Export Popover
 */
@Composable
fun TimelineEditorScreen(
    project: VideoProject,
    canUndo: Boolean,
    canRedo: Boolean,
    onClose: () -> Unit,
    onSplitClip: (clipId: String, playheadMs: Long) -> Unit,
    onDeleteClip: (clipId: String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onStartExport: (ExportConfig) -> Unit,
) {
    val colors = LocalStudioColors.current
    var selectedClipId by remember { mutableStateOf<String?>(null) }
    var playheadMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var showExportPopover by remember { mutableStateOf(false) }
    var exportConfig by remember { mutableStateOf(ExportConfig()) }

    LaunchedEffect(isPlaying, project.totalDurationMs) {
        while (isPlaying && project.totalDurationMs > 0L) {
            delay(100L)
            playheadMs = if (playheadMs + 100L >= project.totalDurationMs) {
                isPlaying = false
                0L
            } else {
                playheadMs + 100L
            }
        }
    }

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(StudioLayout.topBarHeight)
                    .padding(horizontal = StudioSpace.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onClose) { Text("Tutup", color = colors.textMuted) }
                Row(horizontalArrangement = Arrangement.spacedBy(StudioSpace.sm), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showExportPopover = !showExportPopover }) {
                        Text("${exportConfig.resolution.label} ${exportConfig.fps}fps", color = colors.text)
                    }
                    Button(
                        enabled = project.clips.isNotEmpty(),
                        onClick = { onStartExport(exportConfig) },
                        shape = RoundedCornerShape(StudioRadius.pill),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                    ) {
                        Text("Ekspor", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(StudioLayout.bottomToolbarHeight)
                    .background(colors.surface)
                    .padding(horizontal = StudioSpace.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StudioSpace.md),
            ) {
                if (selectedClipId != null) {
                    TextButton(onClick = { selectedClipId = null }) { Text("< Kembali", color = colors.textMuted) }
                    TextButton(onClick = { selectedClipId?.let { onSplitClip(it, playheadMs) } }) {
                        Text("Bagi", color = colors.accent, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = {
                        selectedClipId?.let { onDeleteClip(it) }
                        selectedClipId = null
                    }) {
                        Text("Hapus", color = colors.danger)
                    }
                } else {
                    listOf("Edit", "Audio", "Teks", "Overlay", "Efek", "Rasio").forEach { tool ->
                        Text(tool, color = colors.textMuted, fontSize = 12.sp)
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(StudioSpace.md),
            verticalArrangement = Arrangement.spacedBy(StudioSpace.md),
        ) {
            if (showExportPopover) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(StudioRadius.md))
                        .background(colors.surfaceElevated)
                        .padding(StudioSpace.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Resolusi Ekspor", color = colors.text, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(ExportResolution.P720, ExportResolution.P1080, ExportResolution.UHD_4K).forEach { res ->
                            FilterChip(
                                selected = exportConfig.resolution == res,
                                onClick = { exportConfig = exportConfig.copy(resolution = res) },
                                label = { Text(res.label) },
                            )
                        }
                    }
                }
            }

            // Video Preview Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(StudioRadius.md))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(StudioRadius.md)),
                contentAlignment = Alignment.Center,
            ) {
                val activeClip = project.clips.find { playheadMs in it.timelineStartMs..it.timelineEndMs }
                Text(
                    activeClip?.label ?: "Tidak ada klip di playhead",
                    color = colors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            // Playback + Undo/Redo Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${formatMs(playheadMs)} / ${formatMs(project.totalDurationMs)}", color = colors.textMuted, fontSize = 12.sp)
                Button(
                    onClick = { isPlaying = !isPlaying },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceElevated, contentColor = colors.text),
                ) {
                    Text(if (isPlaying) "Jeda" else "Putar")
                }
                Row {
                    TextButton(enabled = canUndo, onClick = onUndo) { Text("Urungkan", color = if (canUndo) colors.text else colors.border) }
                    TextButton(enabled = canRedo, onClick = onRedo) { Text("Ulangi", color = if (canRedo) colors.text else colors.border) }
                }
            }

            // Scrubber Slider
            LaunchedEffect(project.totalDurationMs) {
                playheadMs = playheadMs.coerceIn(0L, project.totalDurationMs)
            }
            if (project.totalDurationMs > 0L) {
                Slider(
                    value = playheadMs.coerceIn(0L, project.totalDurationMs).toFloat(),
                    onValueChange = { playheadMs = it.toLong() },
                    valueRange = 0f..project.totalDurationMs.toFloat(),
                    modifier = Modifier.semantics { contentDescription = "Posisi playhead timeline" },
                )
            }

            // Main Video Track
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth().height(StudioLayout.mainTrackHeight),
            ) {
                items(project.clips, key = { it.id }) { clip ->
                    val selected = clip.id == selectedClipId
                    Box(
                        modifier = Modifier
                            .width((clip.durationMs / 40L).coerceIn(72L, 260L).dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(StudioRadius.sm))
                            .background(colors.surfaceElevated)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) colors.accent else colors.border,
                                shape = RoundedCornerShape(StudioRadius.sm),
                            )
                            .clickable { selectedClipId = clip.id }
                            .padding(StudioSpace.sm),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(clip.label, color = colors.text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

/**
 * S13 — Export Progress Screen (ForegroundService render contract)
 */
@Composable
fun ExportProgressScreen(
    config: ExportConfig,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
) {
    val colors = LocalStudioColors.current
    var progress by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (progress < 100) {
            delay(60L)
            progress = (progress + 5).coerceAtMost(100)
        }
        onComplete()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.bg).padding(StudioSpace.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Mengekspor ${config.resolution.label} (${config.fps} fps)", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(StudioSpace.lg))
        LinearProgressIndicator(progress = { progress / 100f }, color = colors.accent, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(StudioSpace.md))
        Text("$progress% • Tetap buka aplikasi hingga selesai", color = colors.textMuted, fontSize = 13.sp)
        Spacer(Modifier.height(StudioSpace.xl))
        TextButton(onClick = onCancel) { Text("Batalkan Ekspor", color = colors.danger) }
    }
}

/**
 * S14 — Export Complete & Share Screen
 */
@Composable
fun ExportCompleteScreen(
    onShare: () -> Unit,
    onDone: () -> Unit,
) {
    val colors = LocalStudioColors.current
    Column(
        modifier = Modifier.fillMaxSize().background(colors.bg).padding(StudioSpace.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StudioSpace.md, Alignment.CenterVertically),
    ) {
        Text("Video Tersimpan di Galeri", color = colors.success, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("File MP4 siap dibagikan ke Reels, Shorts, atau TikTok.", color = colors.textMuted, fontSize = 14.sp)
        Spacer(Modifier.height(StudioSpace.md))
        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
        ) {
            Text("Bagikan Video")
        }
        TextButton(onClick = onDone) { Text("Selesai", color = colors.text) }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val mins = totalSec / 60
    val secs = totalSec % 60
    val tenths = (ms % 1000) / 100
    return "%02d:%02d.%d".format(mins, secs, tenths)
}

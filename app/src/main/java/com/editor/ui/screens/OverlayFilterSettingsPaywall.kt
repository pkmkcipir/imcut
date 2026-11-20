package com.editor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editor.design.tokens.LocalStudioColors
import com.editor.design.tokens.StudioLayout
import com.editor.design.tokens.StudioRadius
import com.editor.design.tokens.StudioSpace

data class OverlayPipItem(
    val id: String,
    val label: String,
    val opacity: Float = 1.0f,
    val scale: Float = 0.4f,
)

data class FilterColorState(
    val presetId: String = "none",
    val isProPreset: Boolean = false,
    val brightness: Float = 0f, // -50..50
    val contrast: Float = 0f,   // -50..50
    val saturation: Float = 0f, // -50..50
)

/**
 * S09 — Overlay (Picture-in-Picture) Sheet
 */
@Composable
fun OverlayPipSheet(
    overlays: List<OverlayPipItem>,
    onAddOverlay: (String) -> Unit,
    onUpdateOverlay: (OverlayPipItem) -> Unit,
    onDeleteOverlay: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalStudioColors.current
    val active = overlays.firstOrNull()
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
            Text("Overlay PIP (S09)", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onDismiss) { Text("Selesai", color = colors.accent) }
        }

        if (active == null) {
            Button(
                onClick = { onAddOverlay("Logo / B-Roll PIP") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
            ) {
                Text("+ Tambah Overlay PIP")
            }
        } else {
            Text(active.label, color = colors.text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text("Opasitas: ${(active.opacity * 100).toInt()}%", color = colors.textMuted, fontSize = 12.sp)
            Slider(
                value = active.opacity,
                onValueChange = { onUpdateOverlay(active.copy(opacity = it)) },
                valueRange = 0.1f..1.0f,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = { onAddOverlay("Overlay #${overlays.size + 1}") }) {
                    Text("+ Tambah Lagi", color = colors.accent)
                }
                TextButton(onClick = { onDeleteOverlay(active.id) }) {
                    Text("Hapus Overlay", color = colors.danger)
                }
            }
        }
    }
}

/**
 * S10 — Filter LUT & Basic Color Adjustments Sheet
 */
@Composable
fun FilterColorSheet(
    colorState: FilterColorState,
    onChange: (FilterColorState) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalStudioColors.current
    val presets = remember {
        listOf(
            Triple("none", "Asli", false),
            Triple("warm_film", "Warm Film", false),
            Triple("clean_daylight", "Clean Daylight", false),
            Triple("cinema_teal", "Cinema Teal", true),
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = StudioRadius.sheet, topEnd = StudioRadius.sheet))
            .background(colors.surface)
            .padding(StudioSpace.lg),
        verticalArrangement = Arrangement.spacedBy(StudioSpace.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Filter & Warna (S10)", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onDismiss) { Text("Selesai", color = colors.accent) }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(StudioSpace.sm)) {
            items(presets) { (id, name, isPro) ->
                val selected = colorState.presetId == id
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(StudioRadius.md))
                        .background(colors.surfaceElevated)
                        .border(if (selected) 2.dp else 1.dp, if (selected) colors.accent else colors.border, RoundedCornerShape(StudioRadius.md))
                        .clickable { onChange(colorState.copy(presetId = id, isProPreset = isPro)) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    if (isPro) {
                        Text("PRO", color = colors.warning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(name, color = colors.text, fontSize = 12.sp)
                }
            }
        }

        Text("Kecerahan: ${colorState.brightness.toInt()}", color = colors.textMuted, fontSize = 12.sp)
        Slider(value = colorState.brightness, onValueChange = { onChange(colorState.copy(brightness = it)) }, valueRange = -50f..50f)
        Text("Kontras: ${colorState.contrast.toInt()}", color = colors.textMuted, fontSize = 12.sp)
        Slider(value = colorState.contrast, onValueChange = { onChange(colorState.copy(contrast = it)) }, valueRange = -50f..50f)
        Text("Saturasi: ${colorState.saturation.toInt()}", color = colors.textMuted, fontSize = 12.sp)
        Slider(value = colorState.saturation, onValueChange = { onChange(colorState.copy(saturation = it)) }, valueRange = -50f..50f)
    }
}

/**
 * S15 — Settings Screen (Language, Default Ending toggle OFF by default, Cache, Subscription, Delete Account)
 */
@Composable
fun SettingsScreen(
    appLanguage: String,
    defaultEndingEnabled: Boolean,
    cacheMb: Int,
    onToggleLanguage: () -> Unit,
    onToggleDefaultEnding: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onOpenPaywall: () -> Unit,
    onRequestDeleteAccount: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = LocalStudioColors.current
    Scaffold(
        containerColor = colors.bg,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().height(StudioLayout.topBarHeight).padding(horizontal = StudioSpace.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onBack) { Text("Kembali", color = colors.textMuted) }
                Text("Pengaturan (S15)", color = colors.text, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(56.dp))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(StudioSpace.lg),
            verticalArrangement = Arrangement.spacedBy(StudioSpace.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(StudioRadius.md)).background(colors.surface).padding(StudioSpace.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Tambahkan akhiran default", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Nonaktif bawaan — ekspor tetap bersih tanpa watermark", color = colors.textMuted, fontSize = 12.sp)
                }
                Switch(checked = defaultEndingEnabled, onCheckedChange = onToggleDefaultEnding)
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(StudioRadius.md)).background(colors.surface).clickable { onToggleLanguage() }.padding(StudioSpace.md),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Bahasa Aplikasi", color = colors.text, fontSize = 14.sp)
                Text(if (appLanguage == "id") "Bahasa Indonesia" else "English", color = colors.accent, fontSize = 14.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(StudioRadius.md)).background(colors.surface).clickable { onClearCache() }.padding(StudioSpace.md),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Bersihkan Cache Lokal", color = colors.text, fontSize = 14.sp)
                Text("$cacheMb MB", color = colors.textMuted, fontSize = 13.sp)
            }

            Button(
                onClick = onOpenPaywall,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
            ) {
                Text("Kelola Langganan & imcut Pro", fontWeight = FontWeight.SemiBold)
            }

            TextButton(onClick = onRequestDeleteAccount, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Hapus Akun & Data Saya", color = colors.danger, fontSize = 13.sp)
            }

            Text(
                "imcut • © iman.mn_",
                color = colors.textMuted,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = StudioSpace.md),
            )
        }
    }
}

/**
 * S16 — Paywall & Subscription Screen (with 1-tap "Strip Pro Assets & Export Free")
 */
@Composable
fun PaywallScreen(
    detectedProAssets: List<String>,
    onSubscribeMonthly: () -> Unit,
    onBuyLifetime: () -> Unit,
    onStripProAndExportFree: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = LocalStudioColors.current
    Scaffold(
        containerColor = colors.bg,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().height(StudioLayout.topBarHeight).padding(horizontal = StudioSpace.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("imcut Pro (S16)", color = colors.text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onClose) { Text("Tutup", color = colors.textMuted) }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(StudioSpace.lg),
            verticalArrangement = Arrangement.spacedBy(StudioSpace.md),
        ) {
            if (detectedProAssets.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(StudioRadius.md))
                        .background(colors.surfaceElevated)
                        .border(1.dp, colors.warning, RoundedCornerShape(StudioRadius.md))
                        .padding(StudioSpace.md),
                    verticalArrangement = Arrangement.spacedBy(StudioSpace.sm),
                ) {
                    Text(
                        "Proyek memakai ${detectedProAssets.size} aset Pro: ${detectedProAssets.joinToString(", ")}",
                        color = colors.text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    OutlinedButton(
                        onClick = onStripProAndExportFree,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Text("Hapus Aset Pro & Ekspor Gratis 1080p", color = colors.accent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(StudioRadius.lg))
                    .background(colors.surfaceElevated)
                    .border(2.dp, colors.accent, RoundedCornerShape(StudioRadius.lg))
                    .padding(StudioSpace.lg),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("imcut Pro", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Rp49.000/bulan atau Rp490.000/tahun", color = colors.accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Ekspor 4K/HEVC, paket LUT & transisi orisinal, 120 kredit Auto Captions/bln + refund otomatis jika gagal. Batal 1-klik lewat Google Play.", color = colors.textMuted, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onSubscribeMonthly,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                ) {
                    Text("Langganan via Google Play", fontWeight = FontWeight.SemiBold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(StudioRadius.lg))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(StudioRadius.lg))
                    .padding(StudioSpace.lg),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("imcut Lifetime Pass", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Rp399.000 sekali bayar", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Buka semua alat editing, aset orisinal, & ekspor 4K selamanya tanpa langganan bulanan.", color = colors.textMuted, fontSize = 13.sp)
                OutlinedButton(onClick = onBuyLifetime, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text("Beli Lifetime Sekali Bayar", color = colors.text)
                }
            }
        }
    }
}

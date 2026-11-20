package com.editor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.editor.design.tokens.LocalStudioColors
import com.editor.design.tokens.StudioColors
import com.editor.model.ExportConfig
import com.editor.model.FakeProjectRepository
import com.editor.navigation.AppRoutes
import com.editor.ui.screens.*
import kotlinx.coroutines.launch

/**
 * imcut Android Entry Point (© iman.mn_)
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = FakeProjectRepository()

        setContent {
            val studioColors = StudioColors()
            CompositionLocalProvider(LocalStudioColors provides studioColors) {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        primary = studioColors.accent,
                        onPrimary = studioColors.onAccent,
                        background = studioColors.bg,
                        surface = studioColors.surface,
                        onBackground = studioColors.text,
                        onSurface = studioColors.text,
                        error = studioColors.danger,
                    )
                ) {
                    ImcutApp(repository = repository)
                }
            }
        }
    }
}

@Composable
private fun ImcutApp(repository: FakeProjectRepository) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val projects by repository.observeProjects().collectAsState()
    val gallery by repository.observeGalleryMedia().collectAsState()
    var lastExportConfig by remember { mutableStateOf(ExportConfig()) }
    var appLanguage by remember { mutableStateOf("id") }
    var defaultEnding by remember { mutableStateOf(false) }
    var cacheMb by remember { mutableIntStateOf(64) }

    NavHost(navController = navController, startDestination = AppRoutes.S01_HOME) {
        composable(AppRoutes.S01_HOME) {
            HomeScreen(
                projects = projects,
                onNewProject = { navController.navigate(AppRoutes.S02_MEDIA_PICKER) },
                onOpenProject = { id -> navController.navigate(AppRoutes.editor(id)) },
                onDeleteProject = { id -> scope.launch { repository.deleteProject(id) } },
                onOpenSettings = { navController.navigate(AppRoutes.S15_SETTINGS) },
            )
        }
        composable(AppRoutes.S02_MEDIA_PICKER) {
            MediaPickerScreen(
                mediaItems = gallery,
                onBack = { navController.popBackStack() },
                onConfirmSelection = { selected ->
                    scope.launch {
                        val proj = repository.createProjectFromMedia(selected)
                        navController.navigate(AppRoutes.editor(proj.id)) {
                            popUpTo(AppRoutes.S01_HOME)
                        }
                    }
                },
            )
        }
        composable(AppRoutes.S03_TIMELINE_EDITOR) { entry ->
            val projectId = entry.arguments?.getString("projectId") ?: return@composable
            val project = projects.find { it.id == projectId } ?: return@composable
            TimelineEditorScreen(
                project = project,
                canUndo = repository.canUndo(projectId),
                canRedo = repository.canRedo(projectId),
                onClose = { navController.popBackStack(AppRoutes.S01_HOME, inclusive = false) },
                onSplitClip = { clipId, playheadMs -> scope.launch { repository.splitClipAt(projectId, clipId, playheadMs) } },
                onDeleteClip = { clipId -> scope.launch { repository.deleteClip(projectId, clipId) } },
                onUndo = { scope.launch { repository.undo(projectId) } },
                onRedo = { scope.launch { repository.redo(projectId) } },
                onStartExport = { cfg ->
                    lastExportConfig = cfg
                    navController.navigate(AppRoutes.exportProgress(projectId))
                },
            )
        }
        composable(AppRoutes.S13_EXPORT_PROGRESS) { entry ->
            val projectId = entry.arguments?.getString("projectId") ?: return@composable
            ExportProgressScreen(
                config = lastExportConfig,
                onCancel = { navController.popBackStack() },
                onComplete = {
                    navController.navigate(AppRoutes.exportComplete(projectId)) {
                        popUpTo(AppRoutes.editor(projectId))
                    }
                },
            )
        }
        composable(AppRoutes.S14_EXPORT_COMPLETE) {
            ExportCompleteScreen(
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Diedit dengan imcut (© iman.mn_)")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Bagikan Video"))
                },
                onDone = { navController.popBackStack(AppRoutes.S01_HOME, inclusive = false) },
            )
        }
        composable(AppRoutes.S15_SETTINGS) {
            SettingsScreen(
                appLanguage = appLanguage,
                defaultEndingEnabled = defaultEnding,
                cacheMb = cacheMb,
                onToggleLanguage = { appLanguage = if (appLanguage == "id") "en" else "id" },
                onToggleDefaultEnding = { defaultEnding = it },
                onClearCache = { cacheMb = 0 },
                onOpenPaywall = { navController.navigate(AppRoutes.S16_PAYWALL) },
                onRequestDeleteAccount = { /* Trigger DELETE /api/account/delete */ },
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppRoutes.S16_PAYWALL) {
            PaywallScreen(
                detectedProAssets = emptyList(),
                onSubscribeMonthly = { navController.popBackStack() },
                onBuyLifetime = { navController.popBackStack() },
                onStripProAndExportFree = { navController.popBackStack() },
                onClose = { navController.popBackStack() },
            )
        }
    }
}

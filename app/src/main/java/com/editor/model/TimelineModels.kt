package com.editor.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

enum class AspectRatio(val label: String) {
    FIT("Fit"),
    PORTRAIT_9_16("9:16"),
    LANDSCAPE_16_9("16:9"),
    SQUARE_1_1("1:1"),
    CLASSIC_4_3("4:3"),
}

enum class ExportResolution(val label: String, val heightPx: Int) {
    P480("480p", 480),
    P720("720p", 720),
    P1080("1080p", 1080),
    UHD_4K("4K", 2160),
}

data class MediaAssetItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val localUri: String,
    val mimeType: String = "video/mp4",
    val durationMs: Long,
    val width: Int = 1080,
    val height: Int = 1920,
    val fps: Int = 30,
)

data class TimelineClip(
    val id: String = UUID.randomUUID().toString(),
    val mediaAssetId: String,
    val label: String,
    val timelineStartMs: Long,
    val timelineEndMs: Long,
    val sourceInMs: Long,
    val sourceOutMs: Long,
    val speedValue: Float = 1.0f,
    val volume: Float = 1.0f,
) {
    val durationMs: Long get() = (timelineEndMs - timelineStartMs).coerceAtLeast(0L)
}

data class VideoProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val aspectRatio: AspectRatio = AspectRatio.PORTRAIT_9_16,
    val targetResolution: ExportResolution = ExportResolution.P1080,
    val targetFps: Int = 30,
    val clips: List<TimelineClip> = emptyList(),
    val updatedAtEpochMs: Long = System.currentTimeMillis(),
    val storageBytes: Long = 0L,
    val hasMissingMedia: Boolean = false,
) {
    val totalDurationMs: Long get() = clips.maxOfOrNull { it.timelineEndMs } ?: 0L
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.P1080,
    val fps: Int = 30,
    val codec: String = "h264",
    val bitrateTier: String = "recommended",
)

interface ProjectRepository {
    fun observeProjects(): StateFlow<List<VideoProject>>
    fun observeGalleryMedia(): StateFlow<List<MediaAssetItem>>
    suspend fun createProjectFromMedia(selectedAssets: List<MediaAssetItem>): VideoProject
    suspend fun getProject(projectId: String): VideoProject?
    suspend fun splitClipAt(projectId: String, clipId: String, playheadMs: Long): VideoProject?
    suspend fun deleteClip(projectId: String, clipId: String): VideoProject?
    suspend fun undo(projectId: String): VideoProject?
    suspend fun redo(projectId: String): VideoProject?
    suspend fun renameProject(projectId: String, newTitle: String)
    suspend fun deleteProject(projectId: String)
    fun canUndo(projectId: String): Boolean
    fun canRedo(projectId: String): Boolean
}

/**
 * Fake offline-first repository with identical signatures to the future Room + Supabase layer.
 * Swapping to RoomProjectRepository in /replica-backend requires zero UI changes.
 */
class FakeProjectRepository : ProjectRepository {
    private val undoStacks = mutableMapOf<String, ArrayDeque<List<TimelineClip>>>()
    private val redoStacks = mutableMapOf<String, ArrayDeque<List<TimelineClip>>>()

    private val _gallery = MutableStateFlow(
        listOf(
            MediaAssetItem("m1", "Kopi Pagi.mp4", "content://media/external/video/101", durationMs = 6400L),
            MediaAssetItem("m2", "B-Roll Kemasan.mp4", "content://media/external/video/102", durationMs = 8200L),
            MediaAssetItem("m3", "Review Pelanggan.mp4", "content://media/external/video/103", durationMs = 11500L),
            MediaAssetItem("m4", "Outro Promo.mp4", "content://media/external/video/104", durationMs = 4500L),
        )
    )

    private val _projects = MutableStateFlow(
        listOf(
            VideoProject(
                id = "proj-seed-1",
                title = "Reels Promo Kopi Susu",
                clips = listOf(
                    TimelineClip("c1", "m1", "Kopi Pagi", 0L, 6400L, 0L, 6400L),
                    TimelineClip("c2", "m2", "B-Roll Kemasan", 6400L, 14600L, 0L, 8200L),
                ),
                storageBytes = 42_500_000L,
            )
        )
    )

    override fun observeProjects(): StateFlow<List<VideoProject>> = _projects.asStateFlow()
    override fun observeGalleryMedia(): StateFlow<List<MediaAssetItem>> = _gallery.asStateFlow()

    override suspend fun createProjectFromMedia(selectedAssets: List<MediaAssetItem>): VideoProject {
        var cursorMs = 0L
        val clips = selectedAssets.map { asset ->
            val clip = TimelineClip(
                mediaAssetId = asset.id,
                label = asset.title.substringBeforeLast('.'),
                timelineStartMs = cursorMs,
                timelineEndMs = cursorMs + asset.durationMs,
                sourceInMs = 0L,
                sourceOutMs = asset.durationMs,
            )
            cursorMs += asset.durationMs
            clip
        }
        val project = VideoProject(
            title = "Proyek ${_projects.value.size + 1}",
            clips = clips,
            storageBytes = selectedAssets.size * 18_000_000L,
        )
        _projects.update { listOf(project) + it }
        return project
    }

    override suspend fun getProject(projectId: String): VideoProject? =
        _projects.value.find { it.id == projectId }

    override suspend fun splitClipAt(projectId: String, clipId: String, playheadMs: Long): VideoProject? {
        val current = getProject(projectId) ?: return null
        val target = current.clips.find { it.id == clipId } ?: return current
        if (playheadMs <= target.timelineStartMs + 100L || playheadMs >= target.timelineEndMs - 100L) {
            return current
        }
        pushUndo(projectId, current.clips)
        val splitOffset = playheadMs - target.timelineStartMs
        val left = target.copy(
            timelineEndMs = playheadMs,
            sourceOutMs = target.sourceInMs + splitOffset,
        )
        val right = target.copy(
            id = UUID.randomUUID().toString(),
            timelineStartMs = playheadMs,
            sourceInMs = target.sourceInMs + splitOffset,
        )
        val updatedClips = current.clips.flatMap { if (it.id == clipId) listOf(left, right) else listOf(it) }
        return replaceClips(projectId, updatedClips)
    }

    override suspend fun deleteClip(projectId: String, clipId: String): VideoProject? {
        val current = getProject(projectId) ?: return null
        pushUndo(projectId, current.clips)
        var cursorMs = 0L
        val compacted = current.clips
            .filterNot { it.id == clipId }
            .map { clip ->
                val dur = clip.durationMs
                val moved = clip.copy(timelineStartMs = cursorMs, timelineEndMs = cursorMs + dur)
                cursorMs += dur
                moved
            }
        return replaceClips(projectId, compacted)
    }

    override suspend fun undo(projectId: String): VideoProject? {
        val current = getProject(projectId) ?: return null
        val prev = undoStacks[projectId]?.removeLastOrNull() ?: return current
        redoStacks.getOrPut(projectId) { ArrayDeque() }.addLast(current.clips)
        return replaceClips(projectId, prev)
    }

    override suspend fun redo(projectId: String): VideoProject? {
        val current = getProject(projectId) ?: return null
        val next = redoStacks[projectId]?.removeLastOrNull() ?: return current
        undoStacks.getOrPut(projectId) { ArrayDeque() }.addLast(current.clips)
        return replaceClips(projectId, next)
    }

    override suspend fun renameProject(projectId: String, newTitle: String) {
        _projects.update { list ->
            list.map { if (it.id == projectId) it.copy(title = newTitle.trim()) else it }
        }
    }

    override suspend fun deleteProject(projectId: String) {
        _projects.update { list -> list.filterNot { it.id == projectId } }
    }

    override fun canUndo(projectId: String): Boolean = !undoStacks[projectId].isNullOrEmpty()
    override fun canRedo(projectId: String): Boolean = !redoStacks[projectId].isNullOrEmpty()

    private fun pushUndo(projectId: String, snapshot: List<TimelineClip>) {
        undoStacks.getOrPut(projectId) { ArrayDeque() }.addLast(snapshot)
        redoStacks[projectId]?.clear()
    }

    private fun replaceClips(projectId: String, clips: List<TimelineClip>): VideoProject? {
        var result: VideoProject? = null
        _projects.update { list ->
            list.map {
                if (it.id == projectId) {
                    val updated = it.copy(clips = clips, updatedAtEpochMs = System.currentTimeMillis())
                    result = updated
                    updated
                } else it
            }
        }
        return result
    }
}

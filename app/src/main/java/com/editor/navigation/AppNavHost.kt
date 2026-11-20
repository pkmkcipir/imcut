package com.editor.navigation

/**
 * Route registry for all 16 screens from replica/recon.md.
 * S01, S02, S03, S04, S12, S13, S14 are implemented in the vertical slice;
 * S05-S11, S15, S16 are registered stubs ready for the must-have sprint.
 */
object AppRoutes {
    const val S01_HOME = "home"
    const val S02_MEDIA_PICKER = "media-picker"
    const val S03_TIMELINE_EDITOR = "editor/{projectId}"
    const val S04_CLIP_CONTEXT = "editor/{projectId}/clip/{clipId}"
    const val S05_TRANSITIONS = "editor/{projectId}/transitions"
    const val S06_SPEED_CURVE = "editor/{projectId}/speed"
    const val S07_AUDIO_DRAWER = "editor/{projectId}/audio"
    const val S08_TEXT_CAPTIONS = "editor/{projectId}/text"
    const val S09_OVERLAY_MASK = "editor/{projectId}/overlay"
    const val S10_EFFECTS_COLOR = "editor/{projectId}/effects"
    const val S11_RATIO_CANVAS = "editor/{projectId}/ratio"
    const val S12_EXPORT_SETTINGS = "editor/{projectId}/export-settings"
    const val S13_EXPORT_PROGRESS = "editor/{projectId}/export-progress"
    const val S14_EXPORT_COMPLETE = "editor/{projectId}/export-complete"
    const val S15_SETTINGS = "settings"
    const val S16_PAYWALL = "paywall"

    fun editor(projectId: String) = "editor/$projectId"
    fun exportProgress(projectId: String) = "editor/$projectId/export-progress"
    fun exportComplete(projectId: String) = "editor/$projectId/export-complete"
}

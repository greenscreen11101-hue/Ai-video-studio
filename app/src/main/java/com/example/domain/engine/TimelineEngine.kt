package com.example.domain.engine

import com.example.domain.model.SceneItem

data class VideoFrameTransform(
    val scale: Float,
    val translationX: Float,
    val translationY: Float,
    val alpha: Float = 1.0f
)

data class ActivePlaybackState(
    val currentMs: Long,
    val totalDurationMs: Long,
    val currentSceneIndex: Int,
    val currentScene: SceneItem?,
    val transitionProgress: Float, // 0.0 to 1.0 during transition between current and next scene
    val frameTransform: VideoFrameTransform,
    val isDuckingMusic: Boolean
)

class TimelineEngine {

    fun calculateTotalDurationMs(scenes: List<SceneItem>): Long {
        return scenes.sumOf { (it.durationSeconds * 1000).toLong() }
    }

    /**
     * Computes the current scene, transition, and Ken Burns cinematic camera movement (zoom/pan)
     * at any arbitrary millisecond position.
     */
    fun evaluatePlaybackAt(scenes: List<SceneItem>, currentMs: Long): ActivePlaybackState {
        val totalMs = calculateTotalDurationMs(scenes)
        if (scenes.isEmpty() || totalMs == 0L) {
            return ActivePlaybackState(
                currentMs = 0L,
                totalDurationMs = 0L,
                currentSceneIndex = 0,
                currentScene = null,
                transitionProgress = 0f,
                frameTransform = VideoFrameTransform(1f, 0f, 0f, 1f),
                isDuckingMusic = false
            )
        }

        val clampedMs = currentMs.coerceIn(0L, totalMs)
        var accumulatedMs = 0L
        var activeSceneIndex = 0
        var sceneElapsedMs = 0L
        var sceneDurationMs = 0L

        for (i in scenes.indices) {
            val dMs = (scenes[i].durationSeconds * 1000).toLong()
            if (clampedMs in accumulatedMs until (accumulatedMs + dMs) || i == scenes.lastIndex) {
                activeSceneIndex = i
                sceneElapsedMs = clampedMs - accumulatedMs
                sceneDurationMs = dMs
                break
            }
            accumulatedMs += dMs
        }

        val activeScene = scenes.getOrNull(activeSceneIndex)
        val progressInScene = if (sceneDurationMs > 0) (sceneElapsedMs.toFloat() / sceneDurationMs).coerceIn(0f, 1f) else 0f

        // Transition detection near end of scene (last 700ms)
        val transitionDurationMs = 700L
        val remainingInScene = sceneDurationMs - sceneElapsedMs
        val transitionProgress = if (remainingInScene < transitionDurationMs && activeSceneIndex < scenes.lastIndex) {
            (1.0f - (remainingInScene.toFloat() / transitionDurationMs)).coerceIn(0f, 1f)
        } else {
            0.0f
        }

        // Ken Burns Motion Calculation based on transitionType
        val transform = when (activeScene?.transitionType) {
            "ZOOM_IN" -> {
                val scale = 1.0f + (progressInScene * 0.18f)
                VideoFrameTransform(scale, 0f, 0f, 1f)
            }
            "ZOOM_OUT" -> {
                val scale = 1.20f - (progressInScene * 0.18f)
                VideoFrameTransform(scale, 0f, 0f, 1f)
            }
            "PAN" -> {
                val scale = 1.12f
                val transX = -40f + (progressInScene * 80f)
                VideoFrameTransform(scale, transX, 0f, 1f)
            }
            "CROSSFADE" -> {
                val scale = 1.05f + (progressInScene * 0.08f)
                val alpha = if (transitionProgress > 0) 1.0f - (transitionProgress * 0.4f) else 1.0f
                VideoFrameTransform(scale, 0f, 0f, alpha)
            }
            else -> {
                VideoFrameTransform(1.04f, 0f, 0f, 1f)
            }
        }

        return ActivePlaybackState(
            currentMs = clampedMs,
            totalDurationMs = totalMs,
            currentSceneIndex = activeSceneIndex,
            currentScene = activeScene,
            transitionProgress = transitionProgress,
            frameTransform = transform,
            isDuckingMusic = true // voiceover is active throughout narration
        )
    }
}

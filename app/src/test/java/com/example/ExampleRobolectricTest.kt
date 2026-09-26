package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.network.AIProviderClient
import com.example.domain.engine.SubtitleEngine
import com.example.domain.engine.TimelineEngine
import com.example.domain.model.SceneItem
import com.example.domain.model.VideoStyle
import com.example.domain.model.VisualSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Video Studio", appName)
    }

    @Test
    fun `test script analyzer heuristic breakdown`() {
        val aiClient = AIProviderClient()
        val script = "Artificial intelligence is changing the world. Modern neural systems can see and listen. The future of robotics is happening today."
        val scenes = aiClient.localHeuristicScriptBreakdown(
            script = script,
            style = VideoStyle.TECH,
            targetDurationMinutes = 1
        )
        assertTrue(scenes.isNotEmpty())
        assertEquals(3, scenes.size)
        assertTrue(scenes[0].keywords.isNotEmpty())
    }

    @Test
    fun `test subtitle engine timing cues`() {
        val subtitleEngine = SubtitleEngine()
        val scenes = listOf(
            SceneItem(
                id = "s1",
                projectId = "p1",
                sceneIndex = 0,
                narrationText = "Welcome to AI Video Studio",
                durationSeconds = 5.0f,
                visualType = VisualSource.HYBRID,
                visualPrompt = "Cinematic studio",
                searchKeywords = listOf("studio", "ai"),
                mediaUrl = "https://example.com/img.jpg"
            )
        )
        val cues = subtitleEngine.generateTimedCues(scenes)
        assertEquals(1, cues.size)
        assertEquals(5000L, cues[0].endMs)
        assertEquals(5, cues[0].words.size)
    }

    @Test
    fun `test timeline engine playback evaluation`() {
        val timelineEngine = TimelineEngine()
        val scenes = listOf(
            SceneItem(
                id = "s1",
                projectId = "p1",
                sceneIndex = 0,
                narrationText = "Scene 1",
                durationSeconds = 6.0f,
                visualType = VisualSource.HYBRID,
                visualPrompt = "Prompt 1",
                searchKeywords = listOf("key1"),
                mediaUrl = "https://example.com/1.jpg",
                transitionType = "ZOOM_IN"
            ),
            SceneItem(
                id = "s2",
                projectId = "p1",
                sceneIndex = 1,
                narrationText = "Scene 2",
                durationSeconds = 6.0f,
                visualType = VisualSource.HYBRID,
                visualPrompt = "Prompt 2",
                searchKeywords = listOf("key2"),
                mediaUrl = "https://example.com/2.jpg",
                transitionType = "CROSSFADE"
            )
        )

        val totalMs = timelineEngine.calculateTotalDurationMs(scenes)
        assertEquals(12000L, totalMs)

        val state = timelineEngine.evaluatePlaybackAt(scenes, 3000L)
        assertEquals(0, state.currentSceneIndex)
        assertNotNull(state.currentScene)
        assertTrue(state.frameTransform.scale > 1.0f)
    }
}

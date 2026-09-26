package com.example.domain.engine

import com.example.domain.model.SceneItem
import com.example.domain.model.SubtitleConfig

data class TimedSubtitleWord(
    val word: String,
    val startMs: Long,
    val endMs: Long
)

data class TimedSubtitleCue(
    val sceneIndex: Int,
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val words: List<TimedSubtitleWord>
)

class SubtitleEngine {

    fun generateTimedCues(scenes: List<SceneItem>): List<TimedSubtitleCue> {
        val cues = mutableListOf<TimedSubtitleCue>()
        var cumulativeTimeMs = 0L

        scenes.forEach { scene ->
            val sceneDurationMs = (scene.durationSeconds * 1000).toLong()
            val text = scene.narrationText.trim()
            val rawWords = text.split("\\s+".toRegex()).filter { it.isNotBlank() }

            val wordCount = maxOf(1, rawWords.size)
            val timePerWord = sceneDurationMs / wordCount

            val timedWords = rawWords.mapIndexed { idx, w ->
                val wStart = cumulativeTimeMs + (idx * timePerWord)
                val wEnd = wStart + timePerWord
                TimedSubtitleWord(w, wStart, wEnd)
            }

            cues.add(
                TimedSubtitleCue(
                    sceneIndex = scene.sceneIndex,
                    text = text,
                    startMs = cumulativeTimeMs,
                    endMs = cumulativeTimeMs + sceneDurationMs,
                    words = timedWords
                )
            )

            cumulativeTimeMs += sceneDurationMs
        }

        return cues
    }

    /**
     * Determines active subtitle cue and active highlighted word at a given playback timestamp
     */
    fun getSubtitleAtTimestamp(
        cues: List<TimedSubtitleCue>,
        currentMs: Long
    ): Pair<TimedSubtitleCue?, String?> {
        val activeCue = cues.firstOrNull { currentMs in it.startMs until it.endMs }
        if (activeCue == null) return null to null

        val activeWord = activeCue.words.firstOrNull { currentMs in it.startMs until it.endMs }?.word
        return activeCue to activeWord
    }

    fun exportToSrt(cues: List<TimedSubtitleCue>): String {
        val builder = StringBuilder()
        cues.forEachIndexed { index, cue ->
            builder.append("${index + 1}\n")
            builder.append("${formatSrtTime(cue.startMs)} --> ${formatSrtTime(cue.endMs)}\n")
            builder.append("${cue.text}\n\n")
        }
        return builder.toString()
    }

    private fun formatSrtTime(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis)
    }
}

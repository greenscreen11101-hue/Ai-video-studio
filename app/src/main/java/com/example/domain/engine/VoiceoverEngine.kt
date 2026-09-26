package com.example.domain.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.domain.model.VoiceProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class VoiceoverEngine(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val availableVoices = listOf(
        VoiceProfile("en_us_adam", "Adam (Deep & Authoritative)", "en-US", "Male", 1.0f, 0.95f),
        VoiceProfile("en_us_emma", "Emma (Clear & Cinematic)", "en-US", "Female", 1.05f, 1.05f),
        VoiceProfile("en_gb_arthur", "Arthur (British Documentary)", "en-GB", "Male", 0.95f, 1.0f),
        VoiceProfile("en_us_sarah", "Sarah (Warm Storytelling)", "en-US", "Female", 1.0f, 1.0f),
        VoiceProfile("ur_pk_ali", "Ali (Natural Urdu)", "ur-PK", "Male", 1.0f, 1.0f),
        VoiceProfile("es_es_carlos", "Carlos (Spanish Dynamic)", "es-ES", "Male", 1.0f, 1.0f)
    )

    fun getVoiceProfiles(): List<VoiceProfile> = availableVoices

    suspend fun initialize(): Boolean = withContext(Dispatchers.Main) {
        if (isInitialized && tts != null) return@withContext true
        suspendCancellableCoroutine { continuation ->
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    tts?.language = Locale.US
                    if (continuation.isActive) continuation.resume(true)
                } else {
                    Log.w("VoiceoverEngine", "TTS initialization failed")
                    if (continuation.isActive) continuation.resume(false)
                }
            }
        }
    }

    /**
     * Estimates accurate speech duration in milliseconds based on word count, syllables, and speech rate.
     */
    fun estimateDurationMs(text: String, speed: Float = 1.0f): Long {
        val words = text.trim().split("\\s+".toRegex()).size
        // Average speaking rate: ~140 words per minute = 2.33 words per sec = ~430ms per word + punctuation pauses
        val punctuationCount = text.count { it in listOf('.', '!', '?', ';', ':') }
        val commaCount = text.count { it == ',' }
        val rawMs = (words * 420L) + (punctuationCount * 400L) + (commaCount * 200L)
        val adjustedMs = (rawMs / speed.coerceIn(0.5f, 2.0f)).toLong()
        return maxOf(2500L, adjustedMs)
    }

    suspend fun speakPreview(text: String, voiceProfile: VoiceProfile) = withContext(Dispatchers.Main) {
        initialize()
        tts?.setSpeechRate(voiceProfile.speed)
        tts?.setPitch(voiceProfile.pitch)
        val locale = when (voiceProfile.languageCode) {
            "en-GB" -> Locale.UK
            "ur-PK" -> Locale("ur", "PK")
            "es-ES" -> Locale("es", "ES")
            else -> Locale.US
        }
        try {
            tts?.language = locale
        } catch (e: Exception) {
            tts?.language = Locale.US
        }
        tts?.speak(text.take(120), TextToSpeech.QUEUE_FLUSH, null, "preview_utterance")
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            // Ignore
        }
    }
}

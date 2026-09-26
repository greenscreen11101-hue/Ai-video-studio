package com.example.domain.engine

import com.example.domain.model.MusicTrack

object MusicLibrary {

    val royaltyFreeTracks = listOf(
        MusicTrack("bgm_tech_pulse", "Cyber Tech Pulse", "Electronic / Sci-Fi", 180, volume = 0.35f),
        MusicTrack("bgm_cinematic_ambient", "Ethereal Cinematic Journey", "Ambient / Drone", 240, volume = 0.30f),
        MusicTrack("bgm_epic_orchestral", "Triumphant Horizon", "Orchestral / Epic", 210, volume = 0.32f),
        MusicTrack("bgm_lofi_chill", "Midnight Lofi Coffee", "Lofi / Relaxed", 160, volume = 0.28f),
        MusicTrack("bgm_corporate_inspire", "Upbeat Innovation Summit", "Corporate / Inspiring", 195, volume = 0.30f),
        MusicTrack("bgm_dramatic_suspense", "Subtle Noir Mystery", "Suspense / Thriller", 175, volume = 0.25f),
        MusicTrack("bgm_none", "None (Voiceover Only)", "Silent", 0, volume = 0.0f)
    )

    fun getTrackById(id: String): MusicTrack {
        return royaltyFreeTracks.find { it.id == id } ?: royaltyFreeTracks[0]
    }
}

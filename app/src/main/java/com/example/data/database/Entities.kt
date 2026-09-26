package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val script: String,
    val topic: String,
    val targetDurationMinutes: Int,
    val aspectRatioName: String,
    val videoStyleName: String,
    val visualSourceName: String,
    val voiceId: String,
    val musicId: String,
    val preferredProviderId: String,
    val statusName: String,
    val progressPercent: Int,
    val currentStepTitle: String,
    val scenesCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val outputPath: String?,
    val subtitleFontSize: Int = 20,
    val subtitleTextColor: String = "#FFFFFF",
    val subtitleBgColor: String = "#80000000",
    val subtitlePosition: String = "BOTTOM"
)

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val sceneIndex: Int,
    val narrationText: String,
    val durationSeconds: Float,
    val visualTypeName: String,
    val visualPrompt: String,
    val searchKeywordsCsv: String,
    val mediaUrl: String,
    val localMediaPath: String,
    val transitionType: String,
    val isStock: Boolean,
    val audioDurationMs: Long,
    val status: String
)

@Entity(tableName = "ai_providers")
data class AIProviderEntity(
    @PrimaryKey val id: String,
    val providerTypeName: String,
    val name: String,
    val apiKey: String,
    val baseUrl: String,
    val preferredModel: String,
    val priority: Int,
    val isEnabled: Boolean,
    val isDefault: Boolean,
    val timeoutSeconds: Int,
    val maxRetries: Int,
    val lastTestedSuccess: Boolean?
)

@Entity(tableName = "media_assets")
data class MediaAssetEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val assetType: String, // VIDEO, IMAGE, AUDIO
    val sourceName: String, // PEXELS, PIXABAY, AI_IMAGE, LOCAL
    val title: String,
    val previewUrl: String,
    val localPath: String,
    val durationSeconds: Int,
    val licenseNotice: String,
    val createdAt: Long
)

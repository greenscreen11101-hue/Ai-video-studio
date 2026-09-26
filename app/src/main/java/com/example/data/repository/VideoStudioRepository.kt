package com.example.data.repository

import android.content.Context
import com.example.data.database.AIProviderEntity
import com.example.data.database.AppDatabase
import com.example.data.database.ProjectEntity
import com.example.data.database.SceneEntity
import com.example.data.network.AIProviderClient
import com.example.data.network.StockMediaClient
import com.example.domain.engine.LongVideoPipeline
import com.example.domain.engine.MusicLibrary
import com.example.domain.engine.PipelineExecutionState
import com.example.domain.engine.SubtitleEngine
import com.example.domain.engine.TimelineEngine
import com.example.domain.engine.VoiceoverEngine
import com.example.domain.model.AIProviderConfig
import com.example.domain.model.AIProviderType
import com.example.domain.model.AspectRatio
import com.example.domain.model.GenerationStatus
import com.example.domain.model.MusicTrack
import com.example.domain.model.Project
import com.example.domain.model.SceneItem
import com.example.domain.model.SubtitleConfig
import com.example.domain.model.VideoStyle
import com.example.domain.model.VisualSource
import com.example.domain.model.VoiceProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class VideoStudioRepository(private val context: Context) {

    val database: AppDatabase = AppDatabase.getInstance(context)
    val aiClient = AIProviderClient()
    val stockClient = StockMediaClient()
    val voiceoverEngine = VoiceoverEngine(context)
    val subtitleEngine = SubtitleEngine()
    val timelineEngine = TimelineEngine()
    val longVideoPipeline = LongVideoPipeline(context, database, aiClient, stockClient, voiceoverEngine)

    val pipelineState: StateFlow<PipelineExecutionState?> = longVideoPipeline.pipelineState

    fun getAllProjectsFlow(): Flow<List<Project>> {
        return database.projectDao().getAllProjectsFlow().map { list ->
            list.map { mapToDomainProject(it) }
        }
    }

    suspend fun getProjectById(id: String): Project? = withContext(Dispatchers.IO) {
        database.projectDao().getProjectById(id)?.let { mapToDomainProject(it) }
    }

    fun getProjectFlowById(id: String): Flow<Project?> {
        return database.projectDao().getProjectFlowById(id).map { it?.let { mapToDomainProject(it) } }
    }

    fun getScenesForProjectFlow(projectId: String): Flow<List<SceneItem>> {
        return database.sceneDao().getScenesForProjectFlow(projectId).map { list ->
            list.map { mapToDomainScene(it) }
        }
    }

    suspend fun getScenesForProject(projectId: String): List<SceneItem> = withContext(Dispatchers.IO) {
        database.sceneDao().getScenesForProject(projectId).map { mapToDomainScene(it) }
    }

    fun getAllProvidersFlow(): Flow<List<AIProviderConfig>> {
        return database.aiProviderDao().getAllProvidersFlow().map { list ->
            list.map { mapToDomainProvider(it) }
        }
    }

    suspend fun saveProvider(provider: AIProviderConfig) = withContext(Dispatchers.IO) {
        database.aiProviderDao().insertProvider(
            AIProviderEntity(
                id = provider.id.ifBlank { "prov_${UUID.randomUUID().toString().take(8)}" },
                providerTypeName = provider.type.name,
                name = provider.name,
                apiKey = provider.apiKey,
                baseUrl = provider.baseUrl,
                preferredModel = provider.preferredModel,
                priority = provider.priority,
                isEnabled = provider.isEnabled,
                isDefault = provider.isDefault,
                timeoutSeconds = provider.timeoutSeconds,
                maxRetries = provider.maxRetries,
                lastTestedSuccess = provider.lastTestedSuccess
            )
        )
    }

    suspend fun testProvider(provider: AIProviderConfig): Result<String> {
        return aiClient.testProviderConnection(provider)
    }

    suspend fun createProject(
        title: String,
        script: String,
        topic: String,
        targetDurationMinutes: Int,
        aspectRatio: AspectRatio,
        videoStyle: VideoStyle,
        visualSource: VisualSource,
        voiceId: String,
        musicId: String,
        preferredProviderId: String
    ): String = withContext(Dispatchers.IO) {
        val projectId = "proj_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val entity = ProjectEntity(
            id = projectId,
            title = title.ifBlank { if (topic.isNotBlank()) topic else "Untitled AI Video" },
            script = script,
            topic = topic,
            targetDurationMinutes = targetDurationMinutes,
            aspectRatioName = aspectRatio.name,
            videoStyleName = videoStyle.name,
            visualSourceName = visualSource.name,
            voiceId = voiceId,
            musicId = musicId,
            preferredProviderId = preferredProviderId,
            statusName = GenerationStatus.IDLE.name,
            progressPercent = 0,
            currentStepTitle = "Created - Ready to Generate",
            scenesCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            outputPath = null
        )
        database.projectDao().insertProject(entity)
        return@withContext projectId
    }

    suspend fun updateProject(project: Project) = withContext(Dispatchers.IO) {
        database.projectDao().updateProject(mapToEntityProject(project))
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        database.projectDao().deleteProjectById(id)
        database.sceneDao().deleteScenesForProject(id)
    }

    suspend fun updateScene(scene: SceneItem) = withContext(Dispatchers.IO) {
        database.sceneDao().updateScene(mapToEntityScene(scene))
    }

    fun startGeneration(projectId: String, scope: CoroutineScope) {
        longVideoPipeline.startOrResumeGeneration(projectId, scope)
    }

    fun pauseGeneration() {
        longVideoPipeline.pauseGeneration()
    }

    fun cancelGeneration(projectId: String) {
        longVideoPipeline.cancelGeneration(projectId)
    }

    fun getVoiceProfiles(): List<VoiceProfile> = voiceoverEngine.getVoiceProfiles()

    fun getMusicTracks(): List<MusicTrack> = MusicLibrary.royaltyFreeTracks

    private fun mapToDomainProject(e: ProjectEntity): Project {
        return Project(
            id = e.id,
            title = e.title,
            script = e.script,
            topic = e.topic,
            targetDurationMinutes = e.targetDurationMinutes,
            aspectRatio = try { AspectRatio.valueOf(e.aspectRatioName) } catch (_: Exception) { AspectRatio.RATIO_16_9 },
            videoStyle = try { VideoStyle.valueOf(e.videoStyleName) } catch (_: Exception) { VideoStyle.CINEMATIC },
            visualSource = try { VisualSource.valueOf(e.visualSourceName) } catch (_: Exception) { VisualSource.HYBRID },
            voiceId = e.voiceId,
            musicId = e.musicId,
            preferredProviderId = e.preferredProviderId,
            status = try { GenerationStatus.valueOf(e.statusName) } catch (_: Exception) { GenerationStatus.IDLE },
            progressPercent = e.progressPercent,
            currentStepTitle = e.currentStepTitle,
            scenesCount = e.scenesCount,
            createdAt = e.createdAt,
            updatedAt = e.updatedAt,
            outputPath = e.outputPath,
            subtitleConfig = SubtitleConfig(
                fontSizeSp = e.subtitleFontSize,
                textColorHex = e.subtitleTextColor,
                backgroundColorHex = e.subtitleBgColor,
                position = e.subtitlePosition
            )
        )
    }

    private fun mapToEntityProject(d: Project): ProjectEntity {
        return ProjectEntity(
            id = d.id,
            title = d.title,
            script = d.script,
            topic = d.topic,
            targetDurationMinutes = d.targetDurationMinutes,
            aspectRatioName = d.aspectRatio.name,
            videoStyleName = d.videoStyle.name,
            visualSourceName = d.visualSource.name,
            voiceId = d.voiceId,
            musicId = d.musicId,
            preferredProviderId = d.preferredProviderId,
            statusName = d.status.name,
            progressPercent = d.progressPercent,
            currentStepTitle = d.currentStepTitle,
            scenesCount = d.scenesCount,
            createdAt = d.createdAt,
            updatedAt = d.updatedAt,
            outputPath = d.outputPath,
            subtitleFontSize = d.subtitleConfig.fontSizeSp,
            subtitleTextColor = d.subtitleConfig.textColorHex,
            subtitleBgColor = d.subtitleConfig.backgroundColorHex,
            subtitlePosition = d.subtitleConfig.position
        )
    }

    private fun mapToDomainScene(e: SceneEntity): SceneItem {
        return SceneItem(
            id = e.id,
            projectId = e.projectId,
            sceneIndex = e.sceneIndex,
            narrationText = e.narrationText,
            durationSeconds = e.durationSeconds,
            visualType = try { VisualSource.valueOf(e.visualTypeName) } catch (_: Exception) { VisualSource.HYBRID },
            visualPrompt = e.visualPrompt,
            searchKeywords = e.searchKeywordsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            mediaUrl = e.mediaUrl,
            localMediaPath = e.localMediaPath,
            transitionType = e.transitionType,
            isStock = e.isStock,
            audioDurationMs = e.audioDurationMs,
            status = e.status
        )
    }

    private fun mapToEntityScene(d: SceneItem): SceneEntity {
        return SceneEntity(
            id = d.id,
            projectId = d.projectId,
            sceneIndex = d.sceneIndex,
            narrationText = d.narrationText,
            durationSeconds = d.durationSeconds,
            visualTypeName = d.visualType.name,
            visualPrompt = d.visualPrompt,
            searchKeywordsCsv = d.searchKeywords.joinToString(", "),
            mediaUrl = d.mediaUrl,
            localMediaPath = d.localMediaPath,
            transitionType = d.transitionType,
            isStock = d.isStock,
            audioDurationMs = d.audioDurationMs,
            status = d.status
        )
    }

    private fun mapToDomainProvider(e: AIProviderEntity): AIProviderConfig {
        return AIProviderConfig(
            id = e.id,
            type = try { AIProviderType.valueOf(e.providerTypeName) } catch (_: Exception) { AIProviderType.GEMINI },
            name = e.name,
            apiKey = e.apiKey,
            baseUrl = e.baseUrl,
            preferredModel = e.preferredModel,
            priority = e.priority,
            isEnabled = e.isEnabled,
            isDefault = e.isDefault,
            timeoutSeconds = e.timeoutSeconds,
            maxRetries = e.maxRetries,
            lastTestedSuccess = e.lastTestedSuccess
        )
    }
}

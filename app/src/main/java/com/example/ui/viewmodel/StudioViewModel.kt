package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.VideoStudioRepository
import com.example.domain.engine.ActivePlaybackState
import com.example.domain.engine.TimedSubtitleCue
import com.example.domain.model.AIProviderConfig
import com.example.domain.model.AIProviderType
import com.example.domain.model.AspectRatio
import com.example.domain.model.GenerationStatus
import com.example.domain.model.MusicTrack
import com.example.domain.model.Project
import com.example.domain.model.SceneItem
import com.example.domain.model.StockMediaItem
import com.example.domain.model.SubtitleConfig
import com.example.domain.model.VideoStyle
import com.example.domain.model.VisualSource
import com.example.domain.model.VoiceProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    val repository = VideoStudioRepository(application)

    // Data streams
    val allProjects: StateFlow<List<Project>> = repository.getAllProjectsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiProviders: StateFlow<List<AIProviderConfig>> = repository.getAllProvidersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pipelineState = repository.pipelineState

    // Active project & editor state
    private val _selectedProjectId = MutableStateFlow<String?>(null)
    val selectedProjectId: StateFlow<String?> = _selectedProjectId.asStateFlow()

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    private val _projectScenes = MutableStateFlow<List<SceneItem>>(emptyList())
    val projectScenes: StateFlow<List<SceneItem>> = _projectScenes.asStateFlow()

    private val _timedSubtitleCues = MutableStateFlow<List<TimedSubtitleCue>>(emptyList())
    val timedSubtitleCues: StateFlow<List<TimedSubtitleCue>> = _timedSubtitleCues.asStateFlow()

    // Playback state in Video Editor
    private val _playbackMs = MutableStateFlow(0L)
    val playbackMs: StateFlow<Long> = _playbackMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _activePlaybackState = MutableStateFlow<ActivePlaybackState?>(null)
    val activePlaybackState: StateFlow<ActivePlaybackState?> = _activePlaybackState.asStateFlow()

    private val _currentSubtitleWord = MutableStateFlow<String?>(null)
    val currentSubtitleWord: StateFlow<String?> = _currentSubtitleWord.asStateFlow()

    private var playbackJob: Job? = null

    // Stock & Assets Search Tab
    private val _stockSearchResults = MutableStateFlow<List<StockMediaItem>>(emptyList())
    val stockSearchResults: StateFlow<List<StockMediaItem>> = _stockSearchResults.asStateFlow()

    private val _isSearchingStock = MutableStateFlow(false)
    val isSearchingStock: StateFlow<Boolean> = _isSearchingStock.asStateFlow()

    // Script Wizard Form State
    var wizardTitle = MutableStateFlow("")
    var wizardScript = MutableStateFlow("")
    var wizardTopic = MutableStateFlow("")
    var wizardDurationMinutes = MutableStateFlow(1)
    var wizardAspectRatio = MutableStateFlow(AspectRatio.RATIO_16_9)
    var wizardVideoStyle = MutableStateFlow(VideoStyle.CINEMATIC)
    var wizardVisualSource = MutableStateFlow(VisualSource.HYBRID)
    var wizardVoiceId = MutableStateFlow("en_us_adam")
    var wizardMusicId = MutableStateFlow("bgm_tech_pulse")
    var wizardProviderId = MutableStateFlow("prov_gemini")

    // UI feedback toast / snackbar
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Pre-select first project when projects load if none selected
        viewModelScope.launch {
            allProjects.collect { projects ->
                if (_selectedProjectId.value == null && projects.isNotEmpty()) {
                    selectProject(projects.first().id)
                }
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun selectProject(projectId: String) {
        _selectedProjectId.value = projectId
        pausePlayback()
        _playbackMs.value = 0L

        viewModelScope.launch {
            val proj = repository.getProjectById(projectId)
            _selectedProject.value = proj

            val scenes = repository.getScenesForProject(projectId)
            _projectScenes.value = scenes
            _timedSubtitleCues.value = repository.subtitleEngine.generateTimedCues(scenes)
            updatePlaybackEvaluation(0L)
        }
    }

    // Playback Engine Controls
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        val scenes = _projectScenes.value
        val totalMs = repository.timelineEngine.calculateTotalDurationMs(scenes)
        if (totalMs <= 0) return

        if (_playbackMs.value >= totalMs) {
            _playbackMs.value = 0L
        }

        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val frameRateMs = 33L // ~30 FPS smooth animation
            while (_isPlaying.value && _playbackMs.value < totalMs) {
                delay(frameRateMs)
                val nextMs = _playbackMs.value + frameRateMs
                _playbackMs.value = nextMs
                updatePlaybackEvaluation(nextMs)
            }
            if (_playbackMs.value >= totalMs) {
                _isPlaying.value = false
            }
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun seekTo(ms: Long) {
        val scenes = _projectScenes.value
        val totalMs = repository.timelineEngine.calculateTotalDurationMs(scenes)
        val target = ms.coerceIn(0L, totalMs)
        _playbackMs.value = target
        updatePlaybackEvaluation(target)
    }

    private fun updatePlaybackEvaluation(currentMs: Long) {
        val scenes = _projectScenes.value
        val state = repository.timelineEngine.evaluatePlaybackAt(scenes, currentMs)
        _activePlaybackState.value = state

        val (cue, activeWord) = repository.subtitleEngine.getSubtitleAtTimestamp(_timedSubtitleCues.value, currentMs)
        _currentSubtitleWord.value = activeWord
    }

    // Quick Script Templates for instant generation
    fun applyPresetTopic(topic: String, durationMin: Int, style: VideoStyle) {
        wizardTopic.value = topic
        wizardDurationMinutes.value = durationMin
        wizardVideoStyle.value = style

        when (topic) {
            "Space & Mars Rovers" -> {
                wizardTitle.value = "Journey to the Red Planet"
                wizardScript.value = "Humanity stands on the precipice of becoming a multi-planetary species. Across the desolate crimson deserts of Mars, autonomous robotic rovers navigate jagged craters and analyze ancient Martian lakebeds. Powered by next-generation neural computers and solar arrays, these metal explorers beam high-resolution panoramas back to Earth. Each scientific discovery brings civilization closer to human footprints on the Martian regolith."
            }
            "AI Revolution & Robotics" -> {
                wizardTitle.value = "The Rise of Autonomous AI Systems"
                wizardScript.value = "Artificial intelligence is undergoing a massive transformation across every industry. Autonomous robots and neural vision models are collaborating inside modern smart factories. In hospitals, deep learning algorithms detect cellular abnormalities with superhuman precision. The future is no longer a distant imagination; humanity is actively engineering it today."
            }
            "Deep Ocean Mysteries" -> {
                wizardTitle.value = "Secrets of the Mariana Trench"
                wizardScript.value = "Beneath thousands of meters of crushing saltwater lies Earth's most alien frontier. The abyssal plains and hydrothermal vents host bioluminescent creatures that thrive in complete darkness. Robotic submersibles equipped with pressure-resistant cameras capture glowing jellyfish, giant squids, and volcanic vents powering surreal ecosystems."
            }
            "Cybersecurity & Digital World" -> {
                wizardTitle.value = "Guarding the Global Grid"
                wizardScript.value = "Every millisecond, petabytes of encrypted transactions flow across transcontinental undersea fiber cables. Autonomous defense systems analyze billions of packets to neutralize cyber breaches before human operators can blink. The digital world is the spine of modern civilization."
            }
            else -> {
                wizardTitle.value = topic
                wizardScript.value = "In this compelling exploration of $topic, we analyze how cutting-edge ideas and historical developments have shaped our modern understanding."
            }
        }
    }

    fun createAndStartProject(onNavigateToDashboard: (String) -> Unit) {
        val script = wizardScript.value.trim()
        if (script.isBlank()) {
            _toastMessage.value = "Please write or generate a script first!"
            return
        }

        viewModelScope.launch {
            val projectId = repository.createProject(
                title = wizardTitle.value.trim().ifBlank { wizardTopic.value.ifBlank { "AI Video Project" } },
                script = script,
                topic = wizardTopic.value.trim(),
                targetDurationMinutes = wizardDurationMinutes.value,
                aspectRatio = wizardAspectRatio.value,
                videoStyle = wizardVideoStyle.value,
                visualSource = wizardVisualSource.value,
                voiceId = wizardVoiceId.value,
                musicId = wizardMusicId.value,
                preferredProviderId = wizardProviderId.value
            )

            selectProject(projectId)
            repository.startGeneration(projectId, viewModelScope)
            onNavigateToDashboard(projectId)
        }
    }

    fun restartGeneration(projectId: String) {
        repository.startGeneration(projectId, viewModelScope)
    }

    fun pauseGeneration() {
        repository.pauseGeneration()
    }

    fun cancelGeneration(projectId: String) {
        repository.cancelGeneration(projectId)
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            _toastMessage.value = "Project deleted"
            if (_selectedProjectId.value == projectId) {
                _selectedProjectId.value = null
                _selectedProject.value = null
                _projectScenes.value = emptyList()
            }
        }
    }

    // Editor Adjustments
    fun updateSceneNarration(sceneId: String, newText: String) {
        viewModelScope.launch {
            val scene = _projectScenes.value.find { it.id == sceneId } ?: return@launch
            val exactMs = repository.voiceoverEngine.estimateDurationMs(newText)
            val updated = scene.copy(
                narrationText = newText,
                audioDurationMs = exactMs,
                durationSeconds = (exactMs / 1000f) + 0.3f
            )
            repository.updateScene(updated)
            val updatedList = _projectScenes.value.map { if (it.id == sceneId) updated else it }
            _projectScenes.value = updatedList
            _timedSubtitleCues.value = repository.subtitleEngine.generateTimedCues(updatedList)
            updatePlaybackEvaluation(_playbackMs.value)
            _toastMessage.value = "Scene updated"
        }
    }

    fun updateSceneTransition(sceneId: String, transitionType: String) {
        viewModelScope.launch {
            val scene = _projectScenes.value.find { it.id == sceneId } ?: return@launch
            val updated = scene.copy(transitionType = transitionType)
            repository.updateScene(updated)
            _projectScenes.value = _projectScenes.value.map { if (it.id == sceneId) updated else it }
            updatePlaybackEvaluation(_playbackMs.value)
        }
    }

    fun replaceSceneMedia(sceneId: String, newMediaUrl: String) {
        viewModelScope.launch {
            val scene = _projectScenes.value.find { it.id == sceneId } ?: return@launch
            val updated = scene.copy(mediaUrl = newMediaUrl)
            repository.updateScene(updated)
            _projectScenes.value = _projectScenes.value.map { if (it.id == sceneId) updated else it }
            updatePlaybackEvaluation(_playbackMs.value)
            _toastMessage.value = "Scene visual updated"
        }
    }

    fun updateSubtitleConfig(config: SubtitleConfig) {
        val proj = _selectedProject.value ?: return
        viewModelScope.launch {
            val updated = proj.copy(subtitleConfig = config)
            _selectedProject.value = updated
            repository.updateProject(updated)
        }
    }

    // Stock & AI Assets Search
    fun searchStock(query: String) {
        viewModelScope.launch {
            _isSearchingStock.value = true
            val results = repository.stockClient.searchStockMedia(
                query = query,
                category = "technology",
                aspectRatio = _selectedProject.value?.aspectRatio ?: AspectRatio.RATIO_16_9
            )
            _stockSearchResults.value = results
            _isSearchingStock.value = false
        }
    }

    // AI Provider Management
    fun saveProvider(provider: AIProviderConfig) {
        viewModelScope.launch {
            repository.saveProvider(provider)
            _toastMessage.value = "Provider '${provider.name}' saved"
        }
    }

    fun testProvider(provider: AIProviderConfig, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.testProvider(provider)
            res.onSuccess { msg ->
                repository.saveProvider(provider.copy(lastTestedSuccess = true))
                onResult(true, msg)
            }.onFailure { err ->
                repository.saveProvider(provider.copy(lastTestedSuccess = false))
                onResult(false, err.message ?: "Failed")
            }
        }
    }

    fun previewVoice(voice: VoiceProfile) {
        viewModelScope.launch {
            repository.voiceoverEngine.speakPreview(
                "Artificial intelligence video studio preview voice.",
                voice
            )
        }
    }

    fun getVoiceProfiles(): List<VoiceProfile> = repository.getVoiceProfiles()
    fun getMusicTracks(): List<MusicTrack> = repository.getMusicTracks()

    override fun onCleared() {
        super.onCleared()
        pausePlayback()
        repository.voiceoverEngine.release()
    }
}

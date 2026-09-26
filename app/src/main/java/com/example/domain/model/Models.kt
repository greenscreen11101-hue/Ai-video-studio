package com.example.domain.model

enum class VideoStyle(val displayName: String, val promptModifier: String, val iconName: String) {
    CINEMATIC("Cinematic", "hyper-realistic cinematic lighting, 8k resolution, photorealistic film look, dramatic atmosphere", "Movie"),
    TECH("Futuristic Tech", "cyberpunk neon accents, high-tech interface, holographic glow, cutting-edge technology", "Memory"),
    DOCUMENTARY("Documentary", "authentic BBC National Geographic style, natural ambient lighting, journalistic realism", "Article"),
    EDUCATIONAL("Educational", "crisp clean explanatory visuals, modern infographics, clear focal subject, studio lighting", "School"),
    STORYTELLING("Storytelling", "dramatic emotional narrative, warm cinematic tones, evocative composition", "AutoStories"),
    LUXURY("Luxury & Sleek", "minimalist elegance, gold accents, polished reflective surfaces, premium aesthetic", "Diamond"),
    MOTIVATIONAL("Motivational", "epic sunrise, triumphant athletic perspective, high energy, bold contrast", "FitnessCenter"),
    NEWS("Breaking News", "broadcast TV news room, dynamic broadcast graphics, clean factual visual style", "Newspaper"),
    MINIMAL("Minimal Studio", "clean pastel gradients, flat design elements, modern typography, aesthetic simplicity", "Brush")
}

enum class AspectRatio(val displayName: String, val widthRatio: Int, val heightRatio: Int, val platform: String) {
    RATIO_16_9("16:9 Landscape", 16, 9, "YouTube & TV"),
    RATIO_9_16("9:16 Portrait", 9, 16, "Shorts, Reels, TikTok"),
    RATIO_1_1("1:1 Square", 1, 1, "Instagram & Feeds"),
    RATIO_4_5("4:5 Vertical", 4, 5, "Social Feeds")
}

enum class VisualSource(val displayName: String) {
    STOCK_FOOTAGE("Stock Footage (Pexels / Pixabay)"),
    AI_IMAGES("AI Generated Images"),
    AI_VIDEO("AI Video Generation"),
    HYBRID("Intelligent Hybrid (Stock + AI Fallback)")
}

enum class AIProviderType(val displayName: String, val defaultBaseUrl: String, val defaultModel: String) {
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com", "gemini-1.5-flash"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1", "meta-llama/llama-3.1-8b-instruct:free"),
    OPENAI("OpenAI Compatible", "https://api.openai.com/v1", "gpt-4o-mini"),
    GROQ("Groq Cloud", "https://api.groq.com/openai/v1", "llama-3.1-8b-instant"),
    POLLINATIONS("Pollinations AI (Free Visuals)", "https://image.pollinations.ai", "flux")
}

data class AIProviderConfig(
    val id: String,
    val type: AIProviderType,
    val name: String,
    val apiKey: String,
    val baseUrl: String = type.defaultBaseUrl,
    val preferredModel: String = type.defaultModel,
    val priority: Int = 1,
    val isEnabled: Boolean = true,
    val isDefault: Boolean = false,
    val timeoutSeconds: Int = 30,
    val maxRetries: Int = 3,
    val lastTestedSuccess: Boolean? = null
)

data class VoiceProfile(
    val id: String,
    val name: String,
    val languageCode: String,
    val gender: String,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f
)

data class MusicTrack(
    val id: String,
    val title: String,
    val category: String,
    val durationSeconds: Int,
    val previewUrl: String = "",
    val volume: Float = 0.35f
)

data class SubtitleConfig(
    val fontSizeSp: Int = 20,
    val textColorHex: String = "#FFFFFF",
    val strokeColorHex: String = "#000000",
    val backgroundColorHex: String = "#80000000",
    val isKaraokeHighlighted: Boolean = true,
    val highlightColorHex: String = "#00E5FF",
    val position: String = "BOTTOM" // BOTTOM, CENTER, TOP
)

enum class GenerationStatus {
    IDLE,
    ANALYZING_SCRIPT,
    CREATING_SCENES,
    SEARCHING_STOCK,
    GENERATING_AI_IMAGES,
    SYNTHESIZING_VOICEOVER,
    SYNCING_SUBTITLES,
    ASSEMBLING_TIMELINE,
    RENDERING_VIDEO,
    COMPLETED,
    FAILED,
    PAUSED
}

data class GenerationStepInfo(
    val stepIndex: Int,
    val title: String,
    val description: String,
    val progress: Float,
    val isCompleted: Boolean,
    val isRunning: Boolean,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)

data class SceneItem(
    val id: String,
    val projectId: String,
    val sceneIndex: Int,
    val narrationText: String,
    val durationSeconds: Float,
    val visualType: VisualSource,
    val visualPrompt: String,
    val searchKeywords: List<String>,
    val mediaUrl: String,
    val localMediaPath: String = "",
    val transitionType: String = "CROSSFADE", // CUT, CROSSFADE, ZOOM_IN, ZOOM_OUT, PAN
    val isStock: Boolean = true,
    val audioDurationMs: Long = 0L,
    val status: String = "READY"
)

data class Project(
    val id: String,
    val title: String,
    val script: String,
    val topic: String,
    val targetDurationMinutes: Int, // 1, 2, 5, 10, 15, 20, 30
    val aspectRatio: AspectRatio,
    val videoStyle: VideoStyle,
    val visualSource: VisualSource,
    val voiceId: String,
    val musicId: String,
    val preferredProviderId: String,
    val status: GenerationStatus = GenerationStatus.IDLE,
    val progressPercent: Int = 0,
    val currentStepTitle: String = "Ready to start",
    val scenesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val outputPath: String? = null,
    val subtitleConfig: SubtitleConfig = SubtitleConfig()
)

data class StockMediaItem(
    val id: String,
    val type: String, // "video" or "image"
    val previewUrl: String,
    val downloadUrl: String,
    val photographer: String,
    val width: Int,
    val height: Int,
    val duration: Int = 0,
    val tags: List<String> = emptyList()
)

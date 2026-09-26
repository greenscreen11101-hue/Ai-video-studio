package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        SceneEntity::class,
        AIProviderEntity::class,
        MediaAssetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun sceneDao(): SceneDao
    abstract fun aiProviderDao(): AIProviderDao
    abstract fun mediaAssetDao(): MediaAssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "video_studio_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-seed default AI providers
            CoroutineScope(Dispatchers.IO).launch {
                val providerDao = INSTANCE?.aiProviderDao() ?: return@launch
                val defaultProviders = listOf(
                    AIProviderEntity(
                        id = "prov_gemini",
                        providerTypeName = "GEMINI",
                        name = "Google Gemini Free Tier",
                        apiKey = "",
                        baseUrl = "https://generativelanguage.googleapis.com",
                        preferredModel = "gemini-1.5-flash",
                        priority = 1,
                        isEnabled = true,
                        isDefault = true,
                        timeoutSeconds = 30,
                        maxRetries = 3,
                        lastTestedSuccess = true
                    ),
                    AIProviderEntity(
                        id = "prov_openrouter",
                        providerTypeName = "OPENROUTER",
                        name = "OpenRouter Free Models",
                        apiKey = "",
                        baseUrl = "https://openrouter.ai/api/v1",
                        preferredModel = "meta-llama/llama-3.1-8b-instruct:free",
                        priority = 2,
                        isEnabled = true,
                        isDefault = false,
                        timeoutSeconds = 35,
                        maxRetries = 3,
                        lastTestedSuccess = null
                    ),
                    AIProviderEntity(
                        id = "prov_groq",
                        providerTypeName = "GROQ",
                        name = "Groq Ultra-Fast Free Tier",
                        apiKey = "",
                        baseUrl = "https://api.groq.com/openai/v1",
                        preferredModel = "llama-3.1-8b-instant",
                        priority = 3,
                        isEnabled = true,
                        isDefault = false,
                        timeoutSeconds = 20,
                        maxRetries = 2,
                        lastTestedSuccess = null
                    ),
                    AIProviderEntity(
                        id = "prov_pollinations",
                        providerTypeName = "POLLINATIONS",
                        name = "Pollinations Visuals (No Key Required)",
                        apiKey = "free",
                        baseUrl = "https://image.pollinations.ai",
                        preferredModel = "flux",
                        priority = 4,
                        isEnabled = true,
                        isDefault = false,
                        timeoutSeconds = 30,
                        maxRetries = 2,
                        lastTestedSuccess = true
                    )
                )
                providerDao.insertProviders(defaultProviders)

                // Pre-seed a showcase demo project
                val projectDao = INSTANCE?.projectDao() ?: return@launch
                val sceneDao = INSTANCE?.sceneDao() ?: return@launch

                val demoProjectId = "demo_proj_ai_future"
                val demoProject = ProjectEntity(
                    id = demoProjectId,
                    title = "The Rise of Autonomous AI Systems",
                    script = "Artificial intelligence is undergoing a massive transformation across every industry. Autonomous robots and neural vision models are collaborating inside modern smart factories. In hospitals, deep learning algorithms detect cellular abnormalities with superhuman precision. Meanwhile, exploratory space rovers navigate alien Martian terrain autonomously. The future is no longer a distant imagination; humanity is actively engineering it today.",
                    topic = "Artificial Intelligence & Future Tech",
                    targetDurationMinutes = 1,
                    aspectRatioName = "RATIO_16_9",
                    videoStyleName = "TECH",
                    visualSourceName = "HYBRID",
                    voiceId = "en_us_adam",
                    musicId = "bgm_tech_pulse",
                    preferredProviderId = "prov_gemini",
                    statusName = "COMPLETED",
                    progressPercent = 100,
                    currentStepTitle = "Completed & Ready to Export",
                    scenesCount = 5,
                    createdAt = System.currentTimeMillis() - 3600000,
                    updatedAt = System.currentTimeMillis() - 1800000,
                    outputPath = "/data/user/0/com.aistudio.videostudio.hktvxp/files/renders/ai_future_1080p.mp4"
                )
                projectDao.insertProject(demoProject)

                val demoScenes = listOf(
                    SceneEntity(
                        id = "scene_1_demo",
                        projectId = demoProjectId,
                        sceneIndex = 0,
                        narrationText = "Artificial intelligence is undergoing a massive transformation across every industry.",
                        durationSeconds = 6.0f,
                        visualTypeName = "STOCK_VIDEO",
                        visualPrompt = "Cinematic visualization of glowing neural network data streams in high-tech city",
                        searchKeywordsCsv = "artificial intelligence, neural network, digital data, technology",
                        mediaUrl = "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=1280",
                        localMediaPath = "",
                        transitionType = "CROSSFADE",
                        isStock = true,
                        audioDurationMs = 5800L,
                        status = "READY"
                    ),
                    SceneEntity(
                        id = "scene_2_demo",
                        projectId = demoProjectId,
                        sceneIndex = 1,
                        narrationText = "Autonomous robots and neural vision models are collaborating inside modern smart factories.",
                        durationSeconds = 7.0f,
                        visualTypeName = "STOCK_VIDEO",
                        visualPrompt = "Automated industrial robotic arm assembling electronics in modern futuristic clean factory",
                        searchKeywordsCsv = "industrial robot, factory automation, robotics, smart manufacturing",
                        mediaUrl = "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=1280",
                        localMediaPath = "",
                        transitionType = "ZOOM_IN",
                        isStock = true,
                        audioDurationMs = 6900L,
                        status = "READY"
                    ),
                    SceneEntity(
                        id = "scene_3_demo",
                        projectId = demoProjectId,
                        sceneIndex = 2,
                        narrationText = "In hospitals, deep learning algorithms detect cellular abnormalities with superhuman precision.",
                        durationSeconds = 6.5f,
                        visualTypeName = "AI_IMAGE",
                        visualPrompt = "Futuristic medical laboratory with holographic DNA scan and digital microscopy visualization",
                        searchKeywordsCsv = "medical technology, laboratory, science, healthcare ai",
                        mediaUrl = "https://images.unsplash.com/photo-1532187863486-abf9dbad1b69?w=1280",
                        localMediaPath = "",
                        transitionType = "CROSSFADE",
                        isStock = false,
                        audioDurationMs = 6400L,
                        status = "READY"
                    ),
                    SceneEntity(
                        id = "scene_4_demo",
                        projectId = demoProjectId,
                        sceneIndex = 3,
                        narrationText = "Meanwhile, exploratory space rovers navigate alien Martian terrain autonomously.",
                        durationSeconds = 6.0f,
                        visualTypeName = "STOCK_VIDEO",
                        visualPrompt = "Robotic rover exploring red Martian surface under deep starry cosmic sky",
                        searchKeywordsCsv = "space exploration, mars rover, astronomy, cosmos",
                        mediaUrl = "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=1280",
                        localMediaPath = "",
                        transitionType = "PAN",
                        isStock = true,
                        audioDurationMs = 5900L,
                        status = "READY"
                    ),
                    SceneEntity(
                        id = "scene_5_demo",
                        projectId = demoProjectId,
                        sceneIndex = 4,
                        narrationText = "The future is no longer a distant imagination; humanity is actively engineering it today.",
                        durationSeconds = 6.5f,
                        visualTypeName = "STOCK_VIDEO",
                        visualPrompt = "Golden hour cinematic skyline of futuristic sustainable city with flying transit",
                        searchKeywordsCsv = "futuristic city, skyline, sunrise, technology innovation",
                        mediaUrl = "https://images.unsplash.com/photo-1508873696983-2df5293cb32f?w=1280",
                        localMediaPath = "",
                        transitionType = "ZOOM_OUT",
                        isStock = true,
                        audioDurationMs = 6300L,
                        status = "READY"
                    )
                )
                sceneDao.insertScenes(demoScenes)
            }
        }
    }
}

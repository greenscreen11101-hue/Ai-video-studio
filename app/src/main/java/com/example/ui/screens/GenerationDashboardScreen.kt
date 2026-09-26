package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.GenerationStatus
import com.example.ui.components.Screen
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioPrimaryCyan
import com.example.ui.theme.StudioSecondaryPurple
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun GenerationDashboardScreen(
    viewModel: StudioViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val project by viewModel.selectedProject.collectAsState()
    val scenes by viewModel.projectScenes.collectAsState()
    val pipelineState by viewModel.pipelineState.collectAsState()

    val currentStatus = pipelineState?.status ?: project?.status ?: GenerationStatus.IDLE
    val progress = pipelineState?.progressPercent ?: project?.progressPercent ?: 0
    val currentStep = pipelineState?.currentStepTitle ?: project?.currentStepTitle ?: "Ready"
    val logs = pipelineState?.logs ?: emptyList()

    val stages = listOf(
        "Script Breakdown & AI Analysis" to (progress >= 20),
        "Scene Storyboard Creation" to (progress >= 35),
        "Stock Search & AI Visuals" to (progress >= 60),
        "Voiceover Synthesis (TTS)" to (progress >= 75),
        "Subtitle & Karaoke Sync" to (progress >= 85),
        "Multi-Track Timeline Assembly" to (progress >= 92),
        "Video Composition & MP4 Export" to (progress >= 100)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Generation Pipeline",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
                Text(
                    text = project?.title ?: "AI Video Creation Job",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StudioPrimaryCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Overall Progress Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pipeline_progress_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(StudioPrimaryCyan, StudioSecondaryPurple))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pipeline Status",
                                fontSize = 12.sp,
                                color = StudioTextMuted
                            )
                            Text(
                                text = currentStatus.name.replace("_", " "),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentStatus == GenerationStatus.COMPLETED) StatusSuccess else StudioPrimaryCyan
                            )
                        }

                        Text(
                            text = "$progress%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = StudioPrimaryCyan,
                        trackColor = StudioDarkBg
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentStep,
                        fontSize = 12.sp,
                        color = StudioTextSecondary,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Controls (Pause, Resume, Open in Editor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentStatus == GenerationStatus.COMPLETED) {
                            Button(
                                onClick = { onNavigate(Screen.Editor.route) },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimaryCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dashboard_open_editor_button")
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = StudioDarkBg)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open in Video Editor", color = StudioDarkBg, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            if (pipelineState?.isPaused == true) {
                                Button(
                                    onClick = {
                                        project?.let { viewModel.restartGeneration(it.id) }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimaryCyan),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = StudioDarkBg)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resume", color = StudioDarkBg, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.pauseGeneration() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pause", color = Color.White)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    project?.let { viewModel.cancelGeneration(it.id) }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel", color = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        // Pipeline Stages Checklist
        item {
            Text(
                text = "Pipeline Stages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioBorder, StudioBorder)))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    stages.forEachIndexed { index, (stageTitle, isCompleted) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        if (isCompleted) StatusSuccess.copy(alpha = 0.2f) else StudioSurfaceVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = StatusSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stageTitle,
                                fontSize = 12.sp,
                                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCompleted) StudioTextPrimary else StudioTextMuted
                            )
                        }
                    }
                }
            }
        }

        // Generated Scenes Storyboard
        if (scenes.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Storyboard Scenes (${scenes.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(scenes) { scene ->
                        DashboardSceneCard(scene = scene)
                    }
                }
            }
        }

        // Real-Time Execution Logs Console
        item {
            Text(
                text = "Live Execution Console",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF060910),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioBorder, StudioBorder))),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    if (logs.isEmpty()) {
                        item {
                            Text(
                                text = "Pipeline engine initialized. Waiting for task triggers...",
                                fontSize = 11.sp,
                                color = StudioTextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        items(logs) { log ->
                            Text(
                                text = log,
                                fontSize = 10.sp,
                                color = if (log.contains("Error") || log.contains("failed")) Color.Red else StudioPrimaryCyan,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardSceneCard(scene: com.example.domain.model.SceneItem) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioBorder, StudioBorder)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(StudioDarkBg)
            ) {
                if (scene.mediaUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(scene.mediaUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = scene.narrationText,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = StudioPrimaryCyan,
                            strokeWidth = 2.dp
                        )
                    }
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "Scene ${scene.sceneIndex + 1}",
                        fontSize = 9.sp,
                        color = StudioPrimaryCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "${scene.durationSeconds}s",
                        fontSize = 9.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = scene.narrationText,
                    fontSize = 10.sp,
                    color = StudioTextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "• ${scene.transitionType}",
                    fontSize = 9.sp,
                    color = StudioSecondaryPurple,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

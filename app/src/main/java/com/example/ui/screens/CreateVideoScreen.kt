package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AspectRatio
import com.example.domain.model.VideoStyle
import com.example.domain.model.VisualSource
import com.example.ui.components.Screen
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioPrimaryCyan
import com.example.ui.theme.StudioSecondaryPurple
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.StudioTertiaryAmber
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.viewmodel.StudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVideoScreen(
    viewModel: StudioViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val wizardTitle by viewModel.wizardTitle.collectAsState()
    val wizardScript by viewModel.wizardScript.collectAsState()
    val wizardDuration by viewModel.wizardDurationMinutes.collectAsState()
    val wizardAspectRatio by viewModel.wizardAspectRatio.collectAsState()
    val wizardStyle by viewModel.wizardVideoStyle.collectAsState()
    val wizardSource by viewModel.wizardVisualSource.collectAsState()
    val wizardVoiceId by viewModel.wizardVoiceId.collectAsState()
    val wizardMusicId by viewModel.wizardMusicId.collectAsState()
    val wizardProviderId by viewModel.wizardProviderId.collectAsState()

    val providers by viewModel.aiProviders.collectAsState()
    val voices = viewModel.getVoiceProfiles()
    val musicTracks = viewModel.getMusicTracks()

    val durationOptions = listOf(1, 2, 5, 10, 15, 20, 30)

    val topicPresets = listOf(
        "AI Revolution & Robotics",
        "Space & Mars Rovers",
        "Deep Ocean Mysteries",
        "Cybersecurity & Digital World"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Create AI Video",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
                Text(
                    text = "Configure script, duration (1-30 min), visual style, voice, and AI models",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTextMuted
                )
            }
        }

        // Script Presets / Quick Loaders
        item {
            Text(
                text = "Preset AI Topics (One-Tap Load)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = StudioPrimaryCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(topicPresets) { topic ->
                    Surface(
                        color = StudioSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable {
                                viewModel.applyPresetTopic(
                                    topic = topic,
                                    durationMin = 1,
                                    style = if (topic.contains("AI") || topic.contains("Cyber")) VideoStyle.TECH else VideoStyle.CINEMATIC
                                )
                            }
                            .testTag("preset_topic_${topic.take(6)}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = StudioPrimaryCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = topic,
                                fontSize = 11.sp,
                                color = StudioTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Project Title
        item {
            OutlinedTextField(
                value = wizardTitle,
                onValueChange = { viewModel.wizardTitle.value = it },
                label = { Text("Video Project Title") },
                placeholder = { Text("e.g. Rise of Autonomous AI Systems") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_project_title"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioPrimaryCyan,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary,
                    focusedContainerColor = StudioCardBg,
                    unfocusedContainerColor = StudioCardBg
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
        }

        // Script Input
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Script / Narration Content",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = "${wizardScript.length} chars (~${(wizardScript.split("\\s+".toRegex()).size / 140f * 60).toInt()}s)",
                        fontSize = 11.sp,
                        color = StudioTextMuted
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = wizardScript,
                    onValueChange = { viewModel.wizardScript.value = it },
                    placeholder = { Text("Paste or type your complete video script here. The system will intelligently analyze and segment it into synchronized scenes...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("input_project_script"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioPrimaryCyan,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = StudioTextPrimary,
                        unfocusedTextColor = StudioTextPrimary,
                        focusedContainerColor = StudioCardBg,
                        unfocusedContainerColor = StudioCardBg
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Video Target Duration (1 to 30 minutes)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Duration (Segmented Pipeline)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = "Est. ${wizardDuration * 5} - ${wizardDuration * 8} scenes",
                        fontSize = 11.sp,
                        color = StudioPrimaryCyan
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(durationOptions) { mins ->
                        val isSelected = wizardDuration == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.wizardDurationMinutes.value = mins },
                            label = { Text("${mins}m", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioPrimaryCyan,
                                selectedLabelColor = StudioDarkBg,
                                containerColor = StudioCardBg,
                                labelColor = StudioTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) StudioPrimaryCyan else StudioBorder
                            ),
                            modifier = Modifier.testTag("duration_chip_${mins}m")
                        )
                    }
                }
            }
        }

        // Aspect Ratio
        item {
            Column {
                Text(
                    text = "Aspect Ratio & Platform",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatio.values().forEach { ratio ->
                        val isSelected = wizardAspectRatio == ratio
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.wizardAspectRatio.value = ratio }
                                .testTag("ratio_card_${ratio.name}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StudioSurfaceVariant else StudioCardBg
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    if (isSelected) listOf(StudioPrimaryCyan, StudioSecondaryPurple) else listOf(StudioBorder, StudioBorder)
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = ratio.displayName.split(" ").first(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) StudioPrimaryCyan else StudioTextPrimary
                                )
                                Text(
                                    text = ratio.platform.split(" ").first(),
                                    fontSize = 9.sp,
                                    color = StudioTextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Video Style
        item {
            Column {
                Text(
                    text = "Cinematic Style Preset",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VideoStyle.values()) { style ->
                        val isSelected = wizardStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.wizardVideoStyle.value = style },
                            label = { Text(style.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioSurfaceVariant,
                                selectedLabelColor = StudioPrimaryCyan,
                                containerColor = StudioCardBg,
                                labelColor = StudioTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) StudioPrimaryCyan else StudioBorder
                            ),
                            modifier = Modifier.testTag("style_chip_${style.name}")
                        )
                    }
                }
            }
        }

        // Visual Source
        item {
            Column {
                Text(
                    text = "Visual Source Strategy",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    VisualSource.values().forEach { source ->
                        val isSelected = wizardSource == source
                        Surface(
                            color = if (isSelected) StudioSurfaceVariant else StudioCardBg,
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    if (isSelected) listOf(StudioPrimaryCyan, StudioSecondaryPurple) else listOf(StudioBorder, StudioBorder)
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.wizardVisualSource.value = source }
                                .testTag("source_option_${source.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (isSelected) StudioPrimaryCyan else Color.DarkGray, RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = source.displayName,
                                    fontSize = 12.sp,
                                    color = if (isSelected) StudioTextPrimary else StudioTextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Voiceover Selection & Preview
        item {
            Column {
                Text(
                    text = "Voiceover Narration Profile",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(voices) { voice ->
                        val isSelected = wizardVoiceId == voice.id
                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .clickable { viewModel.wizardVoiceId.value = voice.id },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StudioSurfaceVariant else StudioCardBg
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    if (isSelected) listOf(StudioPrimaryCyan, StudioSecondaryPurple) else listOf(StudioBorder, StudioBorder)
                                )
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = voice.gender,
                                        fontSize = 10.sp,
                                        color = StudioPrimaryCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { viewModel.previewVoice(voice) },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.GraphicEq,
                                            contentDescription = "Preview",
                                            tint = StudioTextPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = voice.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioTextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = voice.languageCode,
                                    fontSize = 9.sp,
                                    color = StudioTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Background Music Track
        item {
            Column {
                Text(
                    text = "Royalty-Free Background Music",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(musicTracks) { track ->
                        val isSelected = wizardMusicId == track.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.wizardMusicId.value = track.id },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = { Text(track.title, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioSurfaceVariant,
                                selectedLabelColor = StudioPrimaryCyan,
                                containerColor = StudioCardBg,
                                labelColor = StudioTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) StudioPrimaryCyan else StudioBorder
                            )
                        )
                    }
                }
            }
        }

        // AI Provider Selection
        item {
            Column {
                Text(
                    text = "AI Engine / Provider",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(providers) { prov ->
                        val isSelected = wizardProviderId == prov.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.wizardProviderId.value = prov.id },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = { Text(prov.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioSurfaceVariant,
                                selectedLabelColor = StudioPrimaryCyan,
                                containerColor = StudioCardBg,
                                labelColor = StudioTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) StudioPrimaryCyan else StudioBorder
                            )
                        )
                    }
                }
            }
        }

        // Generate Action Button
        item {
            val focusManager = LocalFocusManager.current
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.createAndStartProject { projectId ->
                        onNavigate(Screen.Dashboard.route)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_video_generation_button"),
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimaryCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = StudioDarkBg,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Full AI Video",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = StudioDarkBg
                )
            }
        }
    }
}

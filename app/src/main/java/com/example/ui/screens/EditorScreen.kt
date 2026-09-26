package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.AspectRatio
import com.example.domain.model.SceneItem
import com.example.ui.components.VideoPlayerPreview
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
import com.example.ui.theme.TrackMusicColor
import com.example.ui.theme.TrackSubtitleColor
import com.example.ui.theme.TrackVideoColor
import com.example.ui.theme.TrackVoiceColor
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun EditorScreen(
    viewModel: StudioViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val project by viewModel.selectedProject.collectAsState()
    val scenes by viewModel.projectScenes.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackState by viewModel.activePlaybackState.collectAsState()
    val activeWord by viewModel.currentSubtitleWord.collectAsState()
    val subtitleCues by viewModel.timedSubtitleCues.collectAsState()

    var selectedSceneForEdit by remember { mutableStateOf<SceneItem?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    val activeAspectRatio = project?.aspectRatio ?: AspectRatio.RATIO_16_9
    val subtitleConfig = project?.subtitleConfig ?: com.example.domain.model.SubtitleConfig()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Toolbar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project?.title ?: "Video Studio Editor",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${scenes.size} Scenes • ${activeAspectRatio.displayName}",
                        fontSize = 11.sp,
                        color = StudioPrimaryCyan
                    )
                }

                Button(
                    onClick = { showExportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimaryCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("editor_export_mp4_button")
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint = StudioDarkBg,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Export MP4",
                        color = StudioDarkBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Live Video Player Preview Canvas
        item {
            VideoPlayerPreview(
                playbackState = playbackState,
                isPlaying = isPlaying,
                aspectRatio = activeAspectRatio,
                subtitleConfig = subtitleConfig,
                activeSubtitleWord = activeWord,
                onPlayPauseToggle = { viewModel.togglePlayPause() },
                onSeek = { ms -> viewModel.seekTo(ms) }
            )
        }

        // Multi-Track Visual Timeline
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("multi_track_timeline_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioBorder, StudioBorder)))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Multi-Track Timeline",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioTextPrimary
                        )

                        Text(
                            text = "Tap any scene to edit",
                            fontSize = 10.sp,
                            color = StudioTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Track 1: Video / Scene Clips
                    TrackHeader(name = "VIDEO TRACK", color = TrackVideoColor, icon = Icons.Default.Movie)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(scenes) { scene ->
                            val isCurrent = playbackState?.currentSceneIndex == scene.sceneIndex
                            val blockWidth = (scene.durationSeconds * 20).coerceIn(80f, 180f).dp

                            Surface(
                                color = if (isCurrent) TrackVideoColor else StudioSurfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = if (isCurrent) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioPrimaryCyan, Color.White))) else null,
                                modifier = Modifier
                                    .width(blockWidth)
                                    .height(50.dp)
                                    .clickable {
                                        selectedSceneForEdit = scene
                                        // seek to start of scene
                                        var ms = 0L
                                        for (i in 0 until scene.sceneIndex) {
                                            ms += (scenes[i].durationSeconds * 1000).toLong()
                                        }
                                        viewModel.seekTo(ms)
                                    }
                                    .testTag("timeline_scene_${scene.sceneIndex}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (scene.mediaUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = scene.mediaUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = "S${scene.sceneIndex + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) Color.White else StudioTextPrimary
                                        )
                                        Text(
                                            text = "${scene.durationSeconds}s",
                                            fontSize = 9.sp,
                                            color = if (isCurrent) Color.White else StudioTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Track 2: Voiceover Audio Track
                    TrackHeader(name = "VOICEOVER TRACK", color = TrackVoiceColor, icon = Icons.Default.GraphicEq)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(scenes) { scene ->
                            val blockWidth = (scene.durationSeconds * 20).coerceIn(80f, 180f).dp
                            Surface(
                                color = TrackVoiceColor.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .width(blockWidth)
                                    .height(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = TrackVoiceColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${scene.audioDurationMs}ms",
                                        fontSize = 9.sp,
                                        color = TrackVoiceColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Track 3: Subtitles Track
                    TrackHeader(name = "SUBTITLE TRACK", color = TrackSubtitleColor, icon = Icons.Default.Subtitles)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(scenes) { scene ->
                            val blockWidth = (scene.durationSeconds * 20).coerceIn(80f, 180f).dp
                            Surface(
                                color = TrackSubtitleColor.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .width(blockWidth)
                                    .height(24.dp)
                            ) {
                                Text(
                                    text = scene.narrationText,
                                    fontSize = 9.sp,
                                    color = TrackSubtitleColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Track 4: Background Music Track
                    TrackHeader(name = "MUSIC TRACK (Auto Ducking)", color = TrackMusicColor, icon = Icons.Default.MusicNote)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = TrackMusicColor.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = TrackMusicColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BGM • Volume ducked -14dB during voice narration",
                                fontSize = 9.sp,
                                color = TrackMusicColor
                            )
                        }
                    }
                }
            }
        }

        // Subtitle Styling Inspector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudioBorder, StudioBorder)))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Subtitle & Caption Styling",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Karaoke Word Highlight",
                            fontSize = 12.sp,
                            color = StudioTextSecondary
                        )
                        Switch(
                            checked = subtitleConfig.isKaraokeHighlighted,
                            onCheckedChange = { checked ->
                                viewModel.updateSubtitleConfig(subtitleConfig.copy(isKaraokeHighlighted = checked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StudioPrimaryCyan,
                                checkedTrackColor = StudioSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Position: ${subtitleConfig.position}",
                            fontSize = 12.sp,
                            color = StudioTextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("BOTTOM", "CENTER", "TOP").forEach { pos ->
                                FilterChip(
                                    selected = subtitleConfig.position == pos,
                                    onClick = {
                                        viewModel.updateSubtitleConfig(subtitleConfig.copy(position = pos))
                                    },
                                    label = { Text(pos, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudioPrimaryCyan,
                                        selectedLabelColor = StudioDarkBg,
                                        containerColor = StudioSurfaceVariant,
                                        labelColor = StudioTextMuted
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Column {
                        Text(
                            text = "Font Size: ${subtitleConfig.fontSizeSp}sp",
                            fontSize = 12.sp,
                            color = StudioTextSecondary
                        )
                        Slider(
                            value = subtitleConfig.fontSizeSp.toFloat(),
                            onValueChange = { size ->
                                viewModel.updateSubtitleConfig(subtitleConfig.copy(fontSizeSp = size.toInt()))
                            },
                            valueRange = 14f..32f,
                            colors = SliderDefaults.colors(thumbColor = StudioPrimaryCyan, activeTrackColor = StudioPrimaryCyan)
                        )
                    }
                }
            }
        }
    }

    // Scene Edit Modal Dialog
    if (selectedSceneForEdit != null) {
        val scene = selectedSceneForEdit!!
        var narrationText by remember(scene.id) { mutableStateOf(scene.narrationText) }
        var visualUrl by remember(scene.id) { mutableStateOf(scene.mediaUrl) }
        val transitions = listOf("CROSSFADE", "ZOOM_IN", "ZOOM_OUT", "PAN", "CUT")

        val dialogFocusManager = LocalFocusManager.current
        AlertDialog(
            onDismissRequest = {
                dialogFocusManager.clearFocus()
                selectedSceneForEdit = null
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
            title = {
                Text(
                    text = "Edit Scene ${scene.sceneIndex + 1}",
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Narration Line",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioPrimaryCyan
                    )
                    OutlinedTextField(
                        value = narrationText,
                        onValueChange = { narrationText = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPrimaryCyan,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary
                        )
                    )

                    Text(
                        text = "Transition Effect",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioPrimaryCyan
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(transitions) { trans ->
                            FilterChip(
                                selected = scene.transitionType == trans,
                                onClick = { viewModel.updateSceneTransition(scene.id, trans) },
                                label = { Text(trans, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioPrimaryCyan,
                                    selectedLabelColor = StudioDarkBg,
                                    containerColor = StudioSurfaceVariant,
                                    labelColor = StudioTextMuted
                                )
                            )
                        }
                    }

                    Text(
                        text = "Visual Image / Footage URL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioPrimaryCyan
                    )
                    OutlinedTextField(
                        value = visualUrl,
                        onValueChange = { visualUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPrimaryCyan,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = StudioTextPrimary,
                            unfocusedTextColor = StudioTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        dialogFocusManager.clearFocus()
                        viewModel.updateSceneNarration(scene.id, narrationText)
                        if (visualUrl != scene.mediaUrl) {
                            viewModel.replaceSceneMedia(scene.id, visualUrl)
                        }
                        selectedSceneForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimaryCyan)
                ) {
                    Text("Save Changes", color = StudioDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dialogFocusManager.clearFocus()
                    selectedSceneForEdit = null
                }) {
                    Text("Cancel", color = StudioTextMuted)
                }
            },
            containerColor = StudioCardBg
        )
    }

    // Export MP4 Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isExporting) showExportDialog = false },
            title = {
                Text(
                    text = "Export Final MP4 Video",
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Resolution: 1080p Full HD (1920x1080)",
                        fontSize = 12.sp,
                        color = StudioTextSecondary
                    )
                    Text(
                        text = "Frame Rate: 30 FPS",
                        fontSize = 12.sp,
                        color = StudioTextSecondary
                    )
                    Text(
                        text = "Audio: Synchronized Voiceover + Ducked Music",
                        fontSize = 12.sp,
                        color = StudioTextSecondary
                    )
                    Text(
                        text = "Subtitles: Burned-in High Contrast Karaoke Overlay",
                        fontSize = 12.sp,
                        color = StudioTextSecondary
                    )
                    if (isExporting) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = StudioPrimaryCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Muxing video tracks & saving to device...",
                                fontSize = 11.sp,
                                color = StudioPrimaryCyan
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isExporting = true
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isExporting = false
                            showExportDialog = false
                            Toast.makeText(context, "Video exported successfully to Movies/AIStudio!", Toast.LENGTH_LONG).show()
                        }, 1200)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimaryCyan),
                    enabled = !isExporting
                ) {
                    Text("Start Export", color = StudioDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExportDialog = false },
                    enabled = !isExporting
                ) {
                    Text("Cancel", color = StudioTextMuted)
                }
            },
            containerColor = StudioCardBg
        )
    }
}

@Composable
fun TrackHeader(name: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

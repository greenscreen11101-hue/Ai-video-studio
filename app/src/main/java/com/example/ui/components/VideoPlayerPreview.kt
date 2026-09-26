package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.engine.ActivePlaybackState
import com.example.domain.model.AspectRatio
import com.example.domain.model.SubtitleConfig
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioPrimaryCyan
import com.example.ui.theme.StudioSecondaryPurple
import com.example.ui.theme.StudioSurfaceVariant

@Composable
fun VideoPlayerPreview(
    playbackState: ActivePlaybackState?,
    isPlaying: Boolean,
    aspectRatio: AspectRatio,
    subtitleConfig: SubtitleConfig,
    activeSubtitleWord: String?,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showControls by remember { mutableStateOf(true) }

    val ratioFloat = when (aspectRatio) {
        AspectRatio.RATIO_16_9 -> 16f / 9f
        AspectRatio.RATIO_9_16 -> 9f / 16f
        AspectRatio.RATIO_1_1 -> 1f
        AspectRatio.RATIO_4_5 -> 4f / 5f
    }

    val totalMs = playbackState?.totalDurationMs ?: 1L
    val currentMs = playbackState?.currentMs ?: 0L
    val activeScene = playbackState?.currentScene
    val transform = playbackState?.frameTransform

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .clickable { showControls = !showControls }
            .testTag("video_player_preview_box"),
        contentAlignment = Alignment.Center
    ) {
        // Aspect-ratio constrained viewport
        Box(
            modifier = Modifier
                .aspectRatio(ratioFloat, matchHeightConstraintsFirst = false)
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(StudioDarkBg),
            contentAlignment = Alignment.Center
        ) {
            // Scene Visual with Ken Burns Transform Animation
            if (activeScene != null && activeScene.mediaUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(activeScene.mediaUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = activeScene.narrationText,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = transform?.scale ?: 1f
                            scaleY = transform?.scale ?: 1f
                            translationX = transform?.translationX ?: 0f
                            translationY = transform?.translationY ?: 0f
                            alpha = transform?.alpha ?: 1f
                        }
                )
            } else {
                // Empty or loading state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(StudioSurfaceVariant, StudioDarkBg)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "AI Video Studio",
                            style = MaterialTheme.typography.titleMedium,
                            color = StudioPrimaryCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ready to Play",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                }
            }

            // Subtitle Overlay with Karaoke Highlight
            if (activeScene != null && activeScene.narrationText.isNotBlank()) {
                val subtitleAlignment = when (subtitleConfig.position) {
                    "TOP" -> Alignment.TopCenter
                    "CENTER" -> Alignment.Center
                    else -> Alignment.BottomCenter
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = subtitleAlignment
                ) {
                    Surface(
                        color = Color(android.graphics.Color.parseColor(subtitleConfig.backgroundColorHex)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val annotatedText = buildAnnotatedString {
                            val words = activeScene.narrationText.split("\\s+".toRegex())
                            words.forEachIndexed { index, word ->
                                val isHighlighted = word.equals(activeSubtitleWord, ignoreCase = true)
                                if (isHighlighted && subtitleConfig.isKaraokeHighlighted) {
                                    withStyle(
                                        SpanStyle(
                                            color = StudioPrimaryCyan,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = (subtitleConfig.fontSizeSp + 2).sp
                                        )
                                    ) {
                                        append(word)
                                    }
                                } else {
                                    withStyle(
                                        SpanStyle(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = subtitleConfig.fontSizeSp.sp
                                        )
                                    ) {
                                        append(word)
                                    }
                                }
                                if (index < words.size - 1) append(" ")
                            }
                        }

                        Text(
                            text = annotatedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Scene index badge & transition badge
            if (activeScene != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Scene ${activeScene.sceneIndex + 1}",
                                color = StudioPrimaryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${activeScene.transitionType}",
                                color = StudioSecondaryPurple,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Overlay Controls (Play/Pause, Scrub Slider, Timecode)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                // Center Play/Pause button
                IconButton(
                    onClick = onPlayPauseToggle,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .background(StudioPrimaryCyan.copy(alpha = 0.85f), CircleShape)
                        .testTag("player_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = StudioDarkBg,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Bottom scrub controls & timecode
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Slider(
                        value = if (totalMs > 0) currentMs.toFloat() / totalMs else 0f,
                        onValueChange = { frac ->
                            onSeek((frac * totalMs).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = StudioPrimaryCyan,
                            activeTrackColor = StudioPrimaryCyan,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .testTag("player_timeline_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onSeek(maxOf(0L, currentMs - 3000L)) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.FastRewind,
                                    contentDescription = "Rewind 3s",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onSeek(minOf(totalMs, currentMs + 3000L)) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.FastForward,
                                    contentDescription = "Forward 3s",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "${formatTime(currentMs)} / ${formatTime(totalMs)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

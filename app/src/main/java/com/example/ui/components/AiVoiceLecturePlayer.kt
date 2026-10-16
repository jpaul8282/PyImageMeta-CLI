package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learning.model.Lesson
import com.example.learning.voice.VoiceLectureState
import com.example.ui.theme.*

@Composable
fun AiVoiceLecturePlayer(
    lesson: Lesson?,
    voiceState: VoiceLectureState,
    onPlay: (readTakeawaysOnly: Boolean) -> Unit = {},
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onStop: () -> Unit = {},
    onSpeedChange: (Float) -> Unit = {},
    onPitchChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var readTakeawaysOnly by remember { mutableStateOf(false) }
    var showAdvancedSettings by remember { mutableStateOf(false) }

    val isThisLessonPlaying = voiceState.isPlaying && (voiceState.currentLessonId == lesson?.id || lesson == null)
    val isThisLessonPaused = voiceState.isPaused && (voiceState.currentLessonId == lesson?.id || lesson == null)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_voice_lecture_player"),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(Color(0xFF2774AE), Color(0xFFFFD100)))
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Lecturer Persona & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF2774AE), Color(0xFF003B5C))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Voice Lecturer",
                        tint = Color(0xFFFFD100),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = lesson?.voiceLecturerPersona ?: voiceState.currentLecturer,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2774AE).copy(alpha = 0.35f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "UCLA AI",
                                color = Color(0xFFFFD100),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Text(
                        text = if (isThisLessonPlaying) "Narrating: ${voiceState.currentMode}"
                        else if (isThisLessonPaused) "Paused • Ready to resume"
                        else "AI Voice Narration Engine Ready",
                        color = if (isThisLessonPlaying) EmeraldGreen else Slate400,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = { showAdvancedSettings = !showAdvancedSettings },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Audio Pitch & Speed Settings",
                        tint = if (showAdvancedSettings) CyanAccent else Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Audio Waveform Bar (when playing)
            if (isThisLessonPlaying) {
                AudioWaveformVisualizer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(vertical = 4.dp)
                )

                if (voiceState.activeSnippet.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate850)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "\"${voiceState.activeSnippet}\"",
                            color = Slate200,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress indicator
                LinearProgressIndicator(
                    progress = { voiceState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFFFFD100),
                    trackColor = Slate700
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play / Pause / Stop Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isThisLessonPlaying && !isThisLessonPaused) {
                        Button(
                            onClick = {
                                onPlay(readTakeawaysOnly)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2774AE),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("play_voice_lecture_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Lecture", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (readTakeawaysOnly) "Listen to Takeaways" else "Listen to Lecture",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else if (isThisLessonPlaying) {
                        Button(
                            onClick = onPause,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (isThisLessonPaused) {
                        Button(
                            onClick = onResume,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldGreen,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isThisLessonPlaying || isThisLessonPaused) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onStop,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                            border = BorderStroke(1.dp, Slate600),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop", fontSize = 11.sp)
                        }
                    }
                }

                // Mode toggle: Takeaways only vs Full lecture
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = "Takeaways only",
                        color = if (readTakeawaysOnly) Color(0xFFFFD100) else Slate400,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = readTakeawaysOnly,
                        onCheckedChange = { readTakeawaysOnly = it },
                        modifier = Modifier.height(24.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFD100),
                            checkedTrackColor = Color(0xFF2774AE),
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate700
                        )
                    )
                }
            }

            // Advanced Audio Settings (Speed & Pitch)
            AnimatedVisibility(visible = showAdvancedSettings) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate850)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Lecturer Voice Calibration",
                        color = Slate200,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Speed controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Speed: ${voiceState.speed}x", color = Slate400, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
                        speeds.forEach { sp ->
                            FilterChip(
                                selected = kotlin.math.abs(voiceState.speed - sp) < 0.05f,
                                onClick = { onSpeedChange(sp) },
                                label = { Text("${sp}x", fontSize = 10.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Pitch controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Pitch: ${voiceState.pitch}x", color = Slate400, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                        val pitches = listOf(0.9f, 1.0f, 1.15f)
                        val pitchLabels = listOf("Deep", "Standard", "Scholarly")
                        pitches.forEachIndexed { i, p ->
                            FilterChip(
                                selected = kotlin.math.abs(voiceState.pitch - p) < 0.05f,
                                onClick = { onPitchChange(p) },
                                label = { Text(pitchLabels[i], fontSize = 10.sp) },
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 18
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
    val heights = List(barCount) { index ->
        val duration = 350 + (index * 80) % 500
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { animatedHeight ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(animatedHeight.value)
                    .padding(horizontal = 1.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFD100), Color(0xFF2774AE))
                        )
                    )
            )
        }
    }
}

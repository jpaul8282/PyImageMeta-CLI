package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learning.model.Course
import com.example.learning.model.CourseLevel
import com.example.learning.model.Lesson
import com.example.learning.voice.VoiceLectureState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVoiceCourseGeneratorView(
    courses: List<Course>,
    voiceState: VoiceLectureState,
    isGenerating: Boolean,
    generationStatus: String,
    onGenerateUclaMaster: () -> Unit,
    onGenerateCustomCourse: (topic: String, level: CourseLevel, persona: String, apiKey: String?) -> Unit,
    onOpenCourse: (Course) -> Unit,
    onTestVoiceSpeech: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customTopic by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(CourseLevel.GRADUATE_MASTER) }
    var selectedPersona by remember { mutableStateOf("Dr. Bruin — UCLA AI Voice Fellow") }
    var customApiKey by remember { mutableStateOf("") }
    var showApiKeyInput by remember { mutableStateOf(false) }

    val presetTopics = listOf(
        "Quantum Cryptanalysis & Post-Quantum Image Forensics",
        "Autonomous Drone Telemetry & Video GPS Spoofing",
        "Deepfake Facial Inpainting & Diffusion Steganalysis",
        "Hardware Silicon Fault Injection & Side-Channel Audits"
    )

    val uclaBlue = Color(0xFF2774AE)
    val uclaGold = Color(0xFFFFD100)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card: UCLA Master Degree Course
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ucla_master_hero_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(uclaBlue, uclaGold)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // UCLA Seal / Branding Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(uclaBlue, Color(0xFF003B5C)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "UCLA",
                                color = uclaGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "UNIVERSITY OF CALIFORNIA, LOS ANGELES",
                                color = uclaGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Samueli School of Engineering — Master Degree Track",
                                color = Slate300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Master of Science in Cyber Forensics & Neural Steganalysis",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Accredited graduate curriculum powered by AI Voice Lectures. Covers sensor PRNU silicon fingerprints, JPEG DCT coefficients, generative deepfake detection, C2PA cryptographic provenance, and Federal Rules of Evidence 902(14) defense.",
                        color = Slate400,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Curriculum Highlights
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecBadge("6 Modules", "Curriculum")
                        SpecBadge("40 Hours", "Graduate Credit")
                        SpecBadge("SSF-UCLA-MS", "Degree Badge")
                        SpecBadge("Voice AI", "Dr. Bruin Fellow")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onGenerateUclaMaster,
                            enabled = !isGenerating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = uclaBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("generate_ucla_master_btn")
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp), tint = uclaGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate & Enroll",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onTestVoiceSpeech("Greetings from the University of California, Los Angeles. I am Dr. Bruin, your UCLA AI Voice Fellow. Our cyber forensics laboratory investigates silicon sensor noise and neural deepfakes. Welcome to graduate research.")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = uclaGold),
                            border = BorderStroke(1.dp, uclaGold.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Voice Preview", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Voice Demo", fontSize = 12.sp)
                        }
                    }

                    if (isGenerating && generationStatus.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate900)
                                .padding(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = uclaGold,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = generationStatus,
                                color = Slate200,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Custom AI Voice Course Generator Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_ai_course_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Voice Course Synthesizer",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Generate tailored graduate courses with custom topics, code snippets, defense quizzes, and spoken AI lecture scripts.",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Topic Input
                    OutlinedTextField(
                        value = customTopic,
                        onValueChange = { customTopic = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("course_topic_input"),
                        label = { Text("Course Topic or Specialization") },
                        placeholder = { Text("e.g., Post-Quantum Cryptanalysis or Drone Forensics") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = CyanAccent
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Quick Presets:",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Preset Chips
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetTopics.forEach { topic ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (customTopic == topic) Slate700 else Slate800)
                                    .clickable { customTopic = topic }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = topic,
                                        color = if (customTopic == topic) CyanAccent else Slate300,
                                        fontSize = 11.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Select Preset",
                                        tint = if (customTopic == topic) CyanAccent else Slate500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Academic Level Selector
                    Text(
                        text = "Academic Level:",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            CourseLevel.GRADUATE_MASTER to "UCLA Master",
                            CourseLevel.ADVANCED to "Specialist",
                            CourseLevel.INTERMEDIATE to "Intermediate"
                        ).forEach { (lvl, lbl) ->
                            FilterChip(
                                selected = selectedLevel == lvl,
                                onClick = { selectedLevel = lvl },
                                label = { Text(lbl, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = uclaBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Voice Lecturer Persona Selector
                    Text(
                        text = "Voice Lecturer Persona:",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Dr. Bruin — UCLA AI Voice Fellow",
                            "Prof. Thorne — Cryptography Chair",
                            "Dr. Vance — Steganalysis Lead"
                        ).forEach { persona ->
                            FilterChip(
                                selected = selectedPersona == persona,
                                onClick = { selectedPersona = persona },
                                label = { Text(persona.substringBefore("—").trim(), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF003B5C),
                                    selectedLabelColor = uclaGold
                                )
                            )
                        }
                    }

                    // Optional API Key Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showApiKeyInput = !showApiKeyInput }
                            .padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (showApiKeyInput) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Advanced: Custom Gemini API Key (Optional)",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }

                    AnimatedVisibility(visible = showApiKeyInput) {
                        OutlinedTextField(
                            value = customApiKey,
                            onValueChange = { customApiKey = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            label = { Text("Gemini API Key (Leave empty to use built-in engine)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = Slate700,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val topic = customTopic.ifBlank { "Advanced Digital Forensics & Neural Steganalysis" }
                            onGenerateCustomCourse(topic, selectedLevel, selectedPersona, customApiKey.ifBlank { null })
                        },
                        enabled = !isGenerating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_custom_course_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Synthesize Course with AI Voice Lectures",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Master & AI Courses Catalog
        item {
            Text(
                text = "Academic Catalog & Generated Courses",
                color = Slate300,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        val uclaAndAiCourses = courses.filter {
            it.code.startsWith("UCLA") || it.isAiGenerated || it.category == com.example.learning.model.TopicCategory.UCLA_MASTER
        }

        if (uclaAndAiCourses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate850)
                ) {
                    Text(
                        text = "No custom courses generated yet. Tap 'Generate & Enroll' above to synthesize your first UCLA Master Degree Course!",
                        color = Slate400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(uclaAndAiCourses.size) { idx ->
                val c = uclaAndAiCourses[idx]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCourse(c) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    border = BorderStroke(1.dp, if (c.code.startsWith("UCLA")) uclaGold.copy(alpha = 0.6f) else Slate700)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (c.code.startsWith("UCLA")) uclaBlue else Slate800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (c.code.startsWith("UCLA")) Icons.Default.School else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (c.code.startsWith("UCLA")) uclaGold else CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = c.code,
                                    color = if (c.code.startsWith("UCLA")) uclaGold else CyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Slate800)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${c.lessons.size} Modules",
                                        color = Slate300,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Text(
                                text = c.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )

                            Text(
                                text = "${c.institutionName ?: "UCLA"} • Voice Lecturer: ${c.defaultVoiceLecturer.substringBefore("—").trim()}",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Course",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecBadge(title: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Slate800)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFFFFD100),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            color = Slate400,
            fontSize = 10.sp
        )
    }
}

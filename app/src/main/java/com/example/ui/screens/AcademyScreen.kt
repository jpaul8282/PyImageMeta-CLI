package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.learning.model.*
import com.example.learning.voice.VoiceLectureState
import com.example.ui.components.AiVoiceCourseGeneratorView
import com.example.ui.components.AiVoiceLecturePlayer
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyScreen(
    courses: List<Course>,
    courseProgress: Map<String, CourseProgress>,
    userBadges: List<UserBadge>,
    documentations: List<DocSection>,
    voiceState: VoiceLectureState = VoiceLectureState(),
    isGeneratingCourse: Boolean = false,
    generationStatus: String = "",
    onCompleteLesson: (courseId: String, lessonId: String) -> Unit,
    onSubmitQuiz: (courseId: String, scorePercentage: Int) -> Boolean,
    onGenerateUclaMaster: () -> Unit = {},
    onGenerateCustomCourse: (topic: String, level: CourseLevel, persona: String, apiKey: String?) -> Unit = { _, _, _, _ -> },
    onPlayVoiceLesson: (Lesson, readTakeawaysOnly: Boolean) -> Unit = { _, _ -> },
    onPauseVoice: () -> Unit = {},
    onResumeVoice: () -> Unit = {},
    onStopVoice: () -> Unit = {},
    onSetVoiceSpeed: (Float) -> Unit = {},
    onSetVoicePitch: (Float) -> Unit = {},
    onTestVoiceSpeech: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Courses & Topics", "AI Voice Generator", "SSF Badges & Degrees", "Documentation Hub")

    var viewingCourse by remember { mutableStateOf<Course?>(null) }
    var activeQuizCourse by remember { mutableStateOf<Course?>(null) }
    var viewingCertificateBadge by remember { mutableStateOf<UserBadge?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        // Academy Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate850),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Great Learning Academy",
                        tint = CyanAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Great Learning Academy",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF2774AE).copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = "UCLA PARTNER",
                                color = Color(0xFFFFD100),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Digital Forensics, UCLA Master's Degrees & AI Voice Narration",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Sub-tabs Navigation
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Slate850,
            contentColor = CyanAccent,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSubTab == index) (if (index == 1) Color(0xFFFFD100) else CyanAccent) else Slate400
                        )
                    },
                    modifier = Modifier.testTag("academy_subtab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedSubTab) {
                0 -> CoursesListView(
                    courses = courses,
                    progressMap = courseProgress,
                    onOpenCourse = { viewingCourse = it },
                    onStartQuiz = { activeQuizCourse = it }
                )
                1 -> AiVoiceCourseGeneratorView(
                    courses = courses,
                    isGenerating = isGeneratingCourse,
                    generationStatus = generationStatus,
                    voiceState = voiceState,
                    onGenerateUclaMaster = onGenerateUclaMaster,
                    onGenerateCustomCourse = onGenerateCustomCourse,
                    onOpenCourse = { viewingCourse = it },
                    onTestVoiceSpeech = onTestVoiceSpeech
                )
                2 -> SsfBadgesGridView(
                    userBadges = userBadges,
                    onViewCertificate = { viewingCertificateBadge = it }
                )
                3 -> DocumentationHubView(documentations = documentations)
            }
        }
    }

    // Interactive Course Lesson Viewer Dialog
    viewingCourse?.let { course ->
        val progress = courseProgress[course.id] ?: CourseProgress(course.id)
        CourseViewerDialog(
            course = course,
            progress = progress,
            voiceState = voiceState,
            onPlayVoiceLesson = onPlayVoiceLesson,
            onPauseVoice = onPauseVoice,
            onResumeVoice = onResumeVoice,
            onStopVoice = onStopVoice,
            onSetVoiceSpeed = onSetVoiceSpeed,
            onSetVoicePitch = onSetVoicePitch,
            onDismiss = {
                onStopVoice()
                viewingCourse = null
            },
            onCompleteLesson = { lessonId ->
                onCompleteLesson(course.id, lessonId)
            },
            onTakeQuiz = {
                onStopVoice()
                viewingCourse = null
                activeQuizCourse = course
            }
        )
    }

    // Interactive Quiz Modal
    activeQuizCourse?.let { course ->
        QuizModalDialog(
            course = course,
            onDismiss = { activeQuizCourse = null },
            onSubmitQuiz = { score ->
                val passed = onSubmitQuiz(course.id, score)
                activeQuizCourse = null
            }
        )
    }

    // SSF Certificate Modal
    viewingCertificateBadge?.let { userBadge ->
        CertificateModalDialog(
            userBadge = userBadge,
            onDismiss = { viewingCertificateBadge = null }
        )
    }
}

@Composable
private fun CoursesListView(
    courses: List<Course>,
    progressMap: Map<String, CourseProgress>,
    onOpenCourse: (Course) -> Unit,
    onStartQuiz: (Course) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(TopicCategory.ALL) }

    val filteredCourses = remember(selectedCategory, courses) {
        if (selectedCategory == TopicCategory.ALL) courses
        else courses.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TopicCategory.values()) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category.displayName, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                        selectedLabelColor = CyanAccent,
                        containerColor = Slate800,
                        labelColor = Slate400
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) CyanAccent else Slate700
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredCourses) { course ->
                val progress = progressMap[course.id] ?: CourseProgress(course.id)
                val completedCount = progress.completedLessonIds.size
                val totalLessons = course.lessons.size
                val completionPercent = if (totalLessons > 0) (completedCount.toFloat() / totalLessons) else 0f

                val isUclaCourse = course.code.startsWith("UCLA") || course.category == TopicCategory.UCLA_MASTER
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCourse(course) }
                        .testTag("course_card_${course.id}"),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isUclaCourse) BorderStroke(1.5.dp, Color(0xFFFFD100)) else null
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(
                                containerColor = when (course.level) {
                                    CourseLevel.BEGINNER -> EmeraldGreen.copy(alpha = 0.2f)
                                    CourseLevel.INTERMEDIATE -> CyanAccent.copy(alpha = 0.2f)
                                    CourseLevel.ADVANCED -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    CourseLevel.GRADUATE_MASTER -> Color(0xFF2774AE).copy(alpha = 0.35f)
                                },
                                contentColor = when (course.level) {
                                    CourseLevel.BEGINNER -> EmeraldGreen
                                    CourseLevel.INTERMEDIATE -> CyanAccent
                                    CourseLevel.ADVANCED -> Color(0xFFF59E0B)
                                    CourseLevel.GRADUATE_MASTER -> Color(0xFFFFD100)
                                }
                            ) {
                                Text(
                                    text = course.level.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${course.estimatedHours} hrs",
                                    color = Slate400,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = course.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = course.subtitle,
                            color = Slate400,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { completionPercent },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (progress.quizPassed) EmeraldGreen else CyanAccent,
                                trackColor = Slate700
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$completedCount/$totalLessons lessons",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (progress.quizPassed) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Quiz Passed",
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SSF Badge Earned (${progress.quizScore}%)",
                                        color = EmeraldGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Text(
                                    text = "Pass Quiz to Unlock SSF Badge",
                                    color = Slate500,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { onOpenCourse(course) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (progress.quizPassed) Slate800 else CyanAccent,
                                    contentColor = if (progress.quizPassed) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (progress.quizPassed) "Review" else "Start Course",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SsfBadgesGridView(
    userBadges: List<UserBadge>,
    onViewCertificate: (UserBadge) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Security & Steganography Fundamentals (SSF)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Industry-standard verifiable credential badges for forensics, metadata analysis & anti-steganography defense.",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        items(userBadges) { userBadge ->
            val badge = userBadge.badge
            val isUnlocked = userBadge.unlocked

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isUnlocked) Color(badge.tier.colorHex) else Slate700,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .testTag("ssf_badge_card_${badge.code}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) Slate850 else Slate900.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge Shield / Insignia Icon
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUnlocked) Color(badge.tier.colorHex).copy(alpha = 0.2f)
                                else Slate800
                            )
                            .border(
                                width = 2.dp,
                                color = if (isUnlocked) Color(badge.tier.colorHex) else Slate700,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.Shield else Icons.Default.Lock,
                            contentDescription = badge.name,
                            tint = if (isUnlocked) Color(badge.tier.colorHex) else Slate500,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = badge.code,
                                color = if (isUnlocked) Color(badge.tier.colorHex) else Slate500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = badge.tier.tierName,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = badge.name,
                            color = if (isUnlocked) Color.White else Slate500,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = badge.description,
                            color = if (isUnlocked) Slate300 else Slate600,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isUnlocked) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Earned: ${userBadge.unlockedDate ?: "Active"}",
                                    color = EmeraldGreen,
                                    fontSize = 11.sp
                                )
                                OutlinedButton(
                                    onClick = { onViewCertificate(userBadge) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                                    border = BorderStroke(1.dp, CyanAccent),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View Certificate", fontSize = 11.sp)
                                }
                            }
                        } else {
                            Text(
                                text = "Criteria: ${badge.criteria}",
                                color = Slate500,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun DocumentationHubView(
    documentations: List<DocSection>
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredDocs = remember(searchQuery, documentations) {
        if (searchQuery.isBlank()) documentations
        else documentations.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.content.contains(searchQuery, ignoreCase = true) ||
            it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search EXIF tags, GDPR, Stego, Python...", color = Slate500, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate400)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Slate850,
                unfocusedContainerColor = Slate850,
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = Slate700,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("doc_search_field")
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredDocs) { doc ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .testTag("doc_card_${doc.id}"),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = doc.category.uppercase(),
                                color = CyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) "Collapse" else "Expand",
                                tint = Slate400
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = doc.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = doc.summary,
                            color = Slate400,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tags
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            doc.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(Slate800, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "#$tag", color = Slate300, fontSize = 10.sp)
                                }
                            }
                        }

                        // Expanded Content
                        AnimatedVisibility(visible = expanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(color = Slate700)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = doc.content,
                                    color = Slate200,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                doc.codeSnippet?.let { snippet ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = snippet,
                                            color = EmeraldGreen,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CourseViewerDialog(
    course: Course,
    progress: CourseProgress,
    voiceState: VoiceLectureState,
    onPlayVoiceLesson: (Lesson, Boolean) -> Unit,
    onPauseVoice: () -> Unit,
    onResumeVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onSetVoiceSpeed: (Float) -> Unit,
    onSetVoicePitch: (Float) -> Unit,
    onDismiss: () -> Unit,
    onCompleteLesson: (String) -> Unit,
    onTakeQuiz: () -> Unit
) {
    var activeLessonIndex by remember { mutableIntStateOf(0) }
    val activeLesson = course.lessons.getOrNull(activeLessonIndex) ?: return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(16.dp),
            color = Slate850
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = course.title,
                                color = if (course.code.startsWith("UCLA")) Color(0xFFFFD100) else CyanAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "Lesson ${activeLessonIndex + 1} of ${course.lessons.size} • ${activeLesson.estimatedMinutes} min",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Slate700, modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Lesson Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // AI Voice Lecturer Player Card
                    AiVoiceLecturePlayer(
                        lesson = activeLesson,
                        voiceState = voiceState,
                        onPlay = { readTakeawaysOnly ->
                            onPlayVoiceLesson(activeLesson, readTakeawaysOnly)
                        },
                        onPause = onPauseVoice,
                        onResume = onResumeVoice,
                        onStop = onStopVoice,
                        onSpeedChange = onSetVoiceSpeed,
                        onPitchChange = onSetVoicePitch,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Text(
                        text = activeLesson.title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = activeLesson.content,
                        color = Slate200,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    activeLesson.codeExample?.let { code ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Code Implementation:",
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = code,
                                color = EmeraldGreen,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    if (activeLesson.keyTakeaways.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Key Takeaways:",
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        activeLesson.keyTakeaways.forEach { takeaway ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", color = CyanAccent, fontWeight = FontWeight.Bold)
                                Text(takeaway, color = Slate300, fontSize = 13.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Footer Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { if (activeLessonIndex > 0) activeLessonIndex-- },
                        enabled = activeLessonIndex > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    }

                    Button(
                        onClick = {
                            onCompleteLesson(activeLesson.id)
                            if (activeLessonIndex < course.lessons.size - 1) {
                                activeLessonIndex++
                            } else {
                                onTakeQuiz()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (activeLessonIndex < course.lessons.size - 1) "Next Lesson" else "Take Course Quiz",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizModalDialog(
    course: Course,
    onDismiss: () -> Unit,
    onSubmitQuiz: (scorePercentage: Int) -> Unit
) {
    val quiz = course.quiz
    var selectedAnswers by remember { mutableStateOf(mapOf<Int, Int>()) }
    var submitted by remember { mutableStateOf(false) }

    val totalQuestions = quiz.questions.size
    val correctCount = quiz.questions.indices.count { idx ->
        selectedAnswers[idx] == quiz.questions[idx].correctOptionIndex
    }
    val scorePercentage = if (totalQuestions > 0) ((correctCount * 100) / totalQuestions) else 0
    val passed = scorePercentage >= quiz.passingScorePercentage

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = Slate850
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = quiz.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Passing Score: ${quiz.passingScorePercentage}%",
                            color = Slate400,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                HorizontalDivider(color = Slate700, modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(quiz.questions.indices.toList()) { qIdx ->
                        val question = quiz.questions[qIdx]
                        val selectedOpt = selectedAnswers[qIdx]

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Q${qIdx + 1}: ${question.question}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                question.options.forEachIndexed { optIdx, optionText ->
                                    val isSelected = selectedOpt == optIdx
                                    val isCorrect = optIdx == question.correctOptionIndex

                                    val bgColor = when {
                                        submitted && isCorrect -> EmeraldGreen.copy(alpha = 0.2f)
                                        submitted && isSelected && !isCorrect -> Color.Red.copy(alpha = 0.2f)
                                        isSelected -> CyanAccent.copy(alpha = 0.15f)
                                        else -> Slate850
                                    }

                                    val borderColor = when {
                                        submitted && isCorrect -> EmeraldGreen
                                        submitted && isSelected && !isCorrect -> Color.Red
                                        isSelected -> CyanAccent
                                        else -> Slate700
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bgColor)
                                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                            .clickable(enabled = !submitted) {
                                                selectedAnswers = selectedAnswers + (qIdx to optIdx)
                                            }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = if (!submitted) { { selectedAnswers = selectedAnswers + (qIdx to optIdx) } } else null,
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = CyanAccent,
                                                unselectedColor = Slate500
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = optionText,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                if (submitted) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = question.explanation,
                                        color = if (selectedOpt == question.correctOptionIndex) EmeraldGreen else Color(0xFFFFB4AB),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Submit or Results
                if (!submitted) {
                    val allAnswered = selectedAnswers.size == totalQuestions
                    Button(
                        onClick = { submitted = true },
                        enabled = allAnswered,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_quiz_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (allAnswered) "Submit Answers & Evaluate" else "Answer All Questions (${selectedAnswers.size}/$totalQuestions)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (passed) "🎉 Assessment Passed: $scorePercentage%" else "❌ Assessment Failed: $scorePercentage%",
                            color = if (passed) EmeraldGreen else Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { onSubmitQuiz(scorePercentage) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (passed) EmeraldGreen else Slate700,
                                contentColor = if (passed) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("claim_ssf_badge_button")
                        ) {
                            Text(
                                text = if (passed) "Claim SSF Badge & Certificate" else "Close & Retry Later",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificateModalDialog(
    userBadge: UserBadge,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val badge = userBadge.badge

    Dialog(onDismissRequest = onDismiss) {
        val isUcla = badge.tier == SsfBadgeTier.UCLA_MASTER || badge.code == "SSF-UCLA-MS" || badge.institutionName.contains("UCLA")

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Slate850,
            border = BorderStroke(2.dp, if (isUcla) Color(0xFFFFD100) else Color(badge.tier.colorHex))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header seal
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background((if (isUcla) Color(0xFFFFD100) else Color(badge.tier.colorHex)).copy(alpha = 0.2f))
                        .border(2.dp, if (isUcla) Color(0xFFFFD100) else Color(badge.tier.colorHex), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUcla) Icons.Default.School else Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = if (isUcla) Color(0xFFFFD100) else Color(badge.tier.colorHex),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isUcla) "UNIVERSITY OF CALIFORNIA, LOS ANGELES" else "GREAT LEARNING CERTIFICATE OF ACCOMPLISHMENT",
                    color = if (isUcla) Color(0xFFFFD100) else CyanAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                if (isUcla) {
                    Text(
                        text = "SAMUELI SCHOOL OF ENGINEERING — GRADUATE DIPLOMA",
                        color = Color(0xFF2774AE),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = badge.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isUcla) "Master of Science (M.S.) • Conferred Magna Cum Laude" else badge.tier.tierName,
                    color = if (isUcla) Color(0xFFFFD100) else Color(badge.tier.colorHex),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isUcla) {
                        "By authority of the Regents of the University of California, this Master's Degree certifies completion of graduate coursework and defense in ${badge.skillsValidated.joinToString(", ")}."
                    } else {
                        "This credential certifies that the holder has demonstrated verified proficiency in ${badge.skillsValidated.joinToString(", ")}."
                    },
                    color = Slate300,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Credential ID:", color = Slate400, fontSize = 11.sp)
                            Text(
                                userBadge.credentialId ?: "SSF-CRED-ONLINE",
                                color = EmeraldGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Issue Date:", color = Slate400, fontSize = 11.sp)
                            Text(userBadge.unlockedDate ?: "Verified", color = Color.White, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Passing Grade:", color = Slate400, fontSize = 11.sp)
                            Text("${userBadge.scorePercentage}%", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Slate700)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "I earned the ${badge.name} (${badge.code}) Credential from Great Learning PyMeta Engine! Credential ID: ${userBadge.credentialId}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share SSF Certificate"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

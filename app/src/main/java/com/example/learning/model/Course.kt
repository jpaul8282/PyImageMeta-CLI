package com.example.learning.model

enum class TopicCategory(val displayName: String) {
    ALL("All Topics"),
    FORENSICS("Digital Forensics"),
    PRIVACY("Privacy & Defense"),
    STEGANOGRAPHY("Steganography"),
    PYTHON("Python Automation"),
    UCLA_MASTER("UCLA Master's Track"),
    AI_VOICE("AI Voice & Forensics")
}

enum class CourseLevel(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    GRADUATE_MASTER("UCLA Master of Science")
}

data class Lesson(
    val id: String,
    val title: String,
    val content: String,
    val codeExample: String? = null,
    val keyTakeaways: List<String> = emptyList(),
    val estimatedMinutes: Int = 10,
    val voiceLectureScript: String? = null,
    val voiceLecturerPersona: String? = null
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

data class Quiz(
    val id: String,
    val title: String,
    val passingScorePercentage: Int = 75,
    val questions: List<QuizQuestion>
)

data class Course(
    val id: String,
    val code: String,
    val title: String,
    val subtitle: String,
    val category: TopicCategory,
    val level: CourseLevel,
    val estimatedHours: Int,
    val lessons: List<Lesson>,
    val quiz: Quiz,
    val associatedBadgeCode: String,
    val institutionName: String? = null,
    val isAiGenerated: Boolean = false,
    val defaultVoiceLecturer: String = "Dr. Bruin — UCLA AI Voice Fellow"
)

data class CourseProgress(
    val courseId: String,
    val completedLessonIds: Set<String> = emptySet(),
    val quizPassed: Boolean = false,
    val quizScore: Int = 0
)

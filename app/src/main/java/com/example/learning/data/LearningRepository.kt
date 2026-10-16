package com.example.learning.data

import com.example.learning.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LearningRepository private constructor() {

    private val _courses = MutableStateFlow<List<Course>>(LearningDataProvider.courses)
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()
    val documentations: List<DocSection> = LearningDataProvider.documentationSections

    private val _courseProgress = MutableStateFlow<Map<String, CourseProgress>>(
        mapOf(
            // Initial seed progress for nice onboarding experience
            "course_exif_101" to CourseProgress(
                courseId = "course_exif_101",
                completedLessonIds = setOf("l_exif_1"),
                quizPassed = false,
                quizScore = 0
            )
        )
    )
    val courseProgress: StateFlow<Map<String, CourseProgress>> = _courseProgress.asStateFlow()

    private val _userBadges = MutableStateFlow<List<UserBadge>>(
        LearningDataProvider.ssfBadges.map { badge ->
            UserBadge(
                badge = badge,
                unlocked = false,
                unlockedDate = null,
                scorePercentage = 0,
                credentialId = null
            )
        }
    )
    val userBadges: StateFlow<List<UserBadge>> = _userBadges.asStateFlow()

    fun addOrUpdateCourse(course: Course) {
        val current = _courses.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == course.id || it.code == course.code }
        if (existingIndex != -1) {
            current[existingIndex] = course
        } else {
            current.add(0, course)
        }
        _courses.value = current
    }

    fun completeLesson(courseId: String, lessonId: String) {
        val currentMap = _courseProgress.value.toMutableMap()
        val currentProgress = currentMap[courseId] ?: CourseProgress(courseId)
        val updatedLessons = currentProgress.completedLessonIds + lessonId
        currentMap[courseId] = currentProgress.copy(completedLessonIds = updatedLessons)
        _courseProgress.value = currentMap
    }

    fun submitQuiz(courseId: String, scorePercentage: Int): Boolean {
        val course = _courses.value.find { it.id == courseId } ?: return false
        val passed = scorePercentage >= course.quiz.passingScorePercentage

        val currentMap = _courseProgress.value.toMutableMap()
        val currentProgress = currentMap[courseId] ?: CourseProgress(courseId)
        currentMap[courseId] = currentProgress.copy(
            quizPassed = passed,
            quizScore = scorePercentage
        )
        _courseProgress.value = currentMap

        if (passed) {
            unlockBadgeForCourse(course, scorePercentage)
        }

        return passed
    }

    private fun unlockBadgeForCourse(course: Course, scorePercentage: Int) {
        val targetBadgeCode = course.associatedBadgeCode
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val todayStr = dateFormat.format(Date())
        val isUcla = targetBadgeCode == "SSF-UCLA-MS" || course.institutionName?.contains("UCLA", ignoreCase = true) == true
        val credId = if (isUcla) {
            "UCLA-MS-CS201-${UUID.randomUUID().toString().take(6).uppercase()}"
        } else {
            "SSF-GL-${UUID.randomUUID().toString().take(8).uppercase()}"
        }

        val updatedBadges = _userBadges.value.map { userBadge ->
            if (userBadge.badge.code == targetBadgeCode) {
                userBadge.copy(
                    unlocked = true,
                    unlockedDate = todayStr,
                    scorePercentage = scorePercentage,
                    credentialId = credId
                )
            } else {
                userBadge
            }
        }
        _userBadges.value = updatedBadges
    }

    companion object {
        val instance: LearningRepository by lazy { LearningRepository() }
    }
}

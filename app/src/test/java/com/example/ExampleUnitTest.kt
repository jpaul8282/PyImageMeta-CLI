package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun uclaMasterDegreeCourse_hasValidStructure() {
    val course = com.example.learning.generator.AiVoiceCourseGeneratorService.uclaMasterDegreeCourse
    assertEquals("UCLA-MS-CS201", course.code)
    assertEquals(com.example.learning.model.CourseLevel.GRADUATE_MASTER, course.level)
    assertEquals(6, course.lessons.size)
    assertEquals(5, course.quiz.questions.size)
    assertTrue(course.isAiGenerated)
    assertNotNull(course.lessons.first().voiceLectureScript)
    assertEquals("Dr. Bruin — UCLA AI Voice Fellow", course.defaultVoiceLecturer)
  }

  @Test
  fun uclaMasterBadge_hasValidCredentialTier() {
    val badge = com.example.learning.generator.AiVoiceCourseGeneratorService.uclaMasterBadge
    assertEquals("SSF-UCLA-MS", badge.code)
    assertEquals(com.example.learning.model.SsfBadgeTier.UCLA_MASTER, badge.tier)
    assertTrue(badge.skillsValidated.isNotEmpty())
    assertEquals("University of California, Los Angeles (UCLA)", badge.institutionName)
  }
}

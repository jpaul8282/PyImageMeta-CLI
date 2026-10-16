package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SampleDataProvider
import com.example.export.ExifCsvExporter
import com.example.export.ExifJsonExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PyMeta EXIF", appName)
  }

  @Test
  fun `verify sample metadata contains common EXIF tags`() {
    val samples = SampleDataProvider.generateSampleImages()
    assertTrue(samples.isNotEmpty())
    val first = samples.first()
    assertEquals("Nikon", first.make)
    assertEquals("Nikon Z7 II", first.model)
    assertEquals("1/1000s", first.exposureTime)
    assertEquals("f/4.0", first.aperture)
    assertEquals("100", first.iso)
    assertEquals("35mm", first.focalLength)
    assertNotNull(first.dateTime)
  }

  @Test
  fun `verify CSV and JSON exporters produce valid datasets`() {
    val samples = SampleDataProvider.generateSampleImages()
    val csv = ExifCsvExporter.generateCsv(samples)
    assertTrue(csv.contains("File Name,Title,Width,Height,Megapixels"))
    assertTrue(csv.contains("Make,Model,Lens Model"))
    assertTrue(csv.contains("Nikon Z7 II"))

    val json = ExifJsonExporter.generateJson(samples, prettyPrint = true, includeAllTags = true)
    assertTrue(json.startsWith("["))
    assertTrue(json.endsWith("]"))
    assertTrue(json.contains("\"camera\""))
    assertTrue(json.contains("\"shootingParameters\""))
    assertTrue(json.contains("\"geotag\""))
  }

  @Test
  fun `verify Great Learning courses and SSF badge progression`() {
    val repo = com.example.learning.data.LearningRepository.instance
    assertTrue(repo.courses.value.isNotEmpty())
    assertTrue(repo.documentations.isNotEmpty())
    assertTrue(repo.userBadges.value.isNotEmpty())

    // Complete lesson
    val course = repo.courses.value.first()
    val lesson = course.lessons.first()
    repo.completeLesson(course.id, lesson.id)
    val progress = repo.courseProgress.value[course.id]
    assertNotNull(progress)
    assertTrue(progress!!.completedLessonIds.contains(lesson.id))

    // Submit passing quiz
    val passed = repo.submitQuiz(course.id, 90)
    assertTrue(passed)

    // Verify SSF badge unlocked
    val unlockedBadge = repo.userBadges.value.find { it.badge.code == course.associatedBadgeCode }
    assertNotNull(unlockedBadge)
    assertTrue(unlockedBadge!!.unlocked)
    assertNotNull(unlockedBadge.credentialId)
    assertEquals(90, unlockedBadge.scorePercentage)
  }

  @Test
  fun `verify extracted image metadata contains ISO, shutter speed, and location`() {
    val samples = SampleDataProvider.generateSampleImages()
    val geotaggedImage = samples.find { it.hasGps && it.latitude != null && it.longitude != null }
    assertNotNull(geotaggedImage)
    assertEquals("100", geotaggedImage!!.iso)
    assertEquals("1/1000s", geotaggedImage.exposureTime)
    assertNotNull(geotaggedImage.latitude)
    assertNotNull(geotaggedImage.longitude)
    assertTrue(geotaggedImage.latitude!! > 0.0)
    assertEquals("Nikon Z7 II", geotaggedImage.model)
  }

  @Test
  fun `verify Room database persistent storage and paginated retrieval`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.db.AppDatabase.getDatabase(context)
    val dao = db.imageMetadataDao()
    val repo = com.example.db.ImageMetadataRepository(dao)

    // Clear and insert batch
    repo.clearAll()
    val samples = SampleDataProvider.generateSampleImages()
    val entities = samples.map { 
        com.example.db.ImageMetadataEntity(
            id = it.id,
            fileName = it.fileName,
            title = it.title,
            fileSizeKb = it.fileSizeKb,
            width = it.width,
            height = it.height,
            make = it.make,
            model = it.model,
            exposureTime = it.exposureTime,
            aperture = it.aperture,
            iso = it.iso,
            focalLength = it.focalLength,
            hasGps = it.hasGps,
            latitude = it.latitude,
            longitude = it.longitude
        )
    }
    repo.insertBatch(entities)

    // Verify total count
    val totalCount = repo.totalCount.first()
    assertEquals(samples.size, totalCount)

    // Verify pagination with limit 2, offset 0
    val page1 = repo.getPaged(page = 1, pageSize = 2).first()
    assertEquals(2, page1.size)

    // Verify pagination with limit 2, offset 2
    val page2 = repo.getPaged(page = 2, pageSize = 2).first()
    assertEquals(2, page2.size)
    assertNotEquals(page1.first().id, page2.first().id)

    // Verify search
    val searchResults = repo.searchPaged(query = "Nikon", page = 1, pageSize = 10).first()
    assertTrue(searchResults.isNotEmpty())
    assertTrue(searchResults.any { it.make == "Nikon" })
  }

  @Test
  fun `verify AI Meta Generator synthesizes rich captions, scenes, and GenAI prompt styles`() = runBlocking {
    val samples = SampleDataProvider.generateSampleImages()
    val sample = samples.first()
    val entity = com.example.db.ImageMetadataEntity(
        id = sample.id,
        fileName = sample.fileName,
        title = sample.title,
        fileSizeKb = sample.fileSizeKb,
        width = sample.width,
        height = sample.height,
        make = sample.make,
        model = sample.model,
        exposureTime = sample.exposureTime,
        aperture = sample.aperture,
        iso = sample.iso,
        focalLength = sample.focalLength,
        hasGps = sample.hasGps,
        latitude = sample.latitude,
        longitude = sample.longitude
    )

    val synthesized = com.example.engine.AiMetadataGeneratorEngine.generateMetadataForEntity(
        entity = entity,
        customCreativePrompt = "Alpine HDR panorama"
    )

    assertTrue(synthesized.isAiSynthesized)
    assertTrue(synthesized.aiCaption.isNotEmpty())
    assertTrue(synthesized.aiTags.isNotEmpty())
    assertTrue(synthesized.aiSceneType.isNotEmpty())
    assertTrue(synthesized.aiLighting.isNotEmpty())
    assertTrue(synthesized.aiPromptStyle.isNotEmpty())
    assertTrue(synthesized.aiPromptStyle.contains("Prompt:"))
  }
}

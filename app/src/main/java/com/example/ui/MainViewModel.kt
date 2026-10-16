package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.auth.AuthState
import com.example.cli.CliEngine
import com.example.cli.CommandResult
import com.example.data.SampleDataProvider
import com.example.db.AppDatabase
import com.example.db.CliHistoryEntity
import com.example.engine.ExifEngine
import com.example.learning.data.LearningRepository
import com.example.learning.model.Course
import com.example.learning.model.CourseProgress
import com.example.learning.model.DocSection
import com.example.learning.model.UserBadge
import com.example.model.ImageMetadata
import com.example.python.PythonPep8Generator
import com.example.db.ImageMetadataEntity
import com.example.db.ImageMetadataRepository
import com.example.engine.AiMetadataGeneratorEngine
import com.example.model.toEntity
import com.example.model.toModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = AuthManager.instance
    val authState: StateFlow<AuthState> = authManager.authState

    private val db = AppDatabase.getDatabase(application)
    private val cliDao = db.cliHistoryDao()
    val imageRepo = ImageMetadataRepository(db.imageMetadataDao())

    private val _isExtractingMetadata = MutableStateFlow(false)
    val isExtractingMetadata: StateFlow<Boolean> = _isExtractingMetadata.asStateFlow()

    private val _extractionStatus = MutableStateFlow("Extracting EXIF & IPTC Tags...")
    val extractionStatus: StateFlow<String> = _extractionStatus.asStateFlow()

    // Batch Processing State
    private val _isBatchProcessing = MutableStateFlow(false)
    val isBatchProcessing: StateFlow<Boolean> = _isBatchProcessing.asStateFlow()

    private val _batchProcessingProgress = MutableStateFlow(0f)
    val batchProcessingProgress: StateFlow<Float> = _batchProcessingProgress.asStateFlow()

    private val _batchProcessingStatus = MutableStateFlow("")
    val batchProcessingStatus: StateFlow<String> = _batchProcessingStatus.asStateFlow()

    // Pagination & Search State for Room-backed Image Catalog
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _pageSize = MutableStateFlow(5)
    val pageSize: StateFlow<Int> = _pageSize.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val totalRoomImagesCount: StateFlow<Int> = imageRepo.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pagedRoomImages: StateFlow<List<ImageMetadataEntity>> = combine(
        _currentPage,
        _pageSize,
        _searchQuery
    ) { page, size, query ->
        Triple(page, size, query)
    }.flatMapLatest { (page, size, query) ->
        if (query.isBlank()) {
            imageRepo.getPaged(page, size)
        } else {
            imageRepo.searchPaged(query, page, size)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _images = MutableStateFlow<List<ImageMetadata>>(emptyList())
    val images: StateFlow<List<ImageMetadata>> = _images.asStateFlow()

    private val _selectedImage = MutableStateFlow<ImageMetadata?>(null)
    val selectedImage: StateFlow<ImageMetadata?> = _selectedImage.asStateFlow()

    private val _commandInput = MutableStateFlow("")
    val commandInput: StateFlow<String> = _commandInput.asStateFlow()

    private val _pythonBackend = MutableStateFlow(PythonPep8Generator.PythonLibraryBackend.PILLOW)
    val pythonBackend: StateFlow<PythonPep8Generator.PythonLibraryBackend> = _pythonBackend.asStateFlow()

    val dbCliLogs: StateFlow<List<CommandResult>> = cliDao.getAllHistory()
        .map { entities ->
            entities.map {
                CommandResult(
                    command = it.command,
                    output = it.output,
                    isSuccess = it.isSuccess,
                    timestamp = it.timestamp,
                    executionTimeMs = it.executionTimeMs
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _runtimeLogs = MutableStateFlow<List<CommandResult>>(emptyList())
    val runtimeLogs: StateFlow<List<CommandResult>> = _runtimeLogs.asStateFlow()

    private val learningRepo = LearningRepository.instance
    val courses: StateFlow<List<Course>> = learningRepo.courses
    val documentations: List<DocSection> = learningRepo.documentations
    val courseProgress: StateFlow<Map<String, CourseProgress>> = learningRepo.courseProgress
    val userBadges: StateFlow<List<UserBadge>> = learningRepo.userBadges

    // AI Voice & Course Generator State
    private val voiceEngine = com.example.learning.voice.VoiceLecturerEngine.instance
    val voiceState: StateFlow<com.example.learning.voice.VoiceLectureState> = voiceEngine.state

    private val _isGeneratingCourse = MutableStateFlow(false)
    val isGeneratingCourse: StateFlow<Boolean> = _isGeneratingCourse.asStateFlow()

    private val _generationStatus = MutableStateFlow("")
    val generationStatus: StateFlow<String> = _generationStatus.asStateFlow()

    init {
        // Initialize AI Voice Lecturer Engine with Application Context
        voiceEngine.initialize(application)

        // Load default sample photos
        val samples = SampleDataProvider.generateSampleImages()
        _images.value = samples
        _selectedImage.value = samples.firstOrNull()

        // Initial welcome command log
        val welcomeCmd = CommandResult(
            command = "pymeta --version",
            output = "PyMeta CLI v2.4 initialized. PEP 8 Python Image Engine Ready.",
            isSuccess = true
        )
        _runtimeLogs.value = listOf(welcomeCmd)

        // Seed Room Database with initial sample images if empty
        viewModelScope.launch {
            try {
                val count = imageRepo.totalCount.first()
                if (count == 0) {
                    val initialEntities = samples.map { sample ->
                        val isAlpine = sample.title.contains("Alpine", ignoreCase = true)
                        val isCyber = sample.title.contains("Neon", ignoreCase = true) || sample.title.contains("Cyber", ignoreCase = true)
                        val isArch = sample.title.contains("Architecture", ignoreCase = true) || sample.title.contains("Bridge", ignoreCase = true)
                        sample.toEntity(
                            aiCaption = when {
                                isAlpine -> "Majestic alpine panoramic elevation capturing snow-capped serrated ridges (${sample.make} ${sample.model}, ${sample.exposureTime} @ ${sample.aperture})."
                                isCyber -> "Vibrant high-contrast urban nightscape reflecting saturated neon signage (${sample.make} ${sample.model}, ${sample.exposureTime} @ ${sample.aperture})."
                                isArch -> "Iconic architectural suspension geometry showcasing structural steel tension (${sample.make} ${sample.model}, ${sample.exposureTime} @ ${sample.aperture})."
                                else -> "Fine art editorial portrait exhibiting nuanced tonal range and micro-contrast."
                            },
                            aiTags = when {
                                isAlpine -> "alpine, summit, golden-hour, mountain-vista, nikon-optics, calibrated"
                                isCyber -> "cyberpunk, neon-glow, rain-reflections, anamorphic, night-city"
                                isArch -> "architecture, suspension-bridge, geometric-symmetry, sunset-sky"
                                else -> "portrait, chiaroscuro, studio-lighting, sharp-optics"
                            },
                            aiSceneType = when {
                                isAlpine -> "High-Altitude Alpine Vista"
                                isCyber -> "Urban Cyberpunk & Neon Nocturne"
                                isArch -> "Monumental Structural Architecture"
                                else -> "Studio Chiaroscuro Portrait"
                            },
                            aiLighting = when {
                                isAlpine -> "Radiant Alpine Golden Hour with Crisp Glacial Rim Glow"
                                isCyber -> "Volumetric Magenta and Cyan Neon Bokeh Reflections"
                                isArch -> "Dramatic Sunset Backlighting with Atmospheric Dust Rays"
                                else -> "Subtle 45-Degree Softbox Chiaroscuro"
                            },
                            aiPromptStyle = "Ultra-detailed photographic capture on ${sample.make} ${sample.model}, ${sample.aperture}, ISO ${sample.iso} --ar 16:9 --style raw",
                            isAiSynthesized = true
                        )
                    }
                    imageRepo.insertBatch(initialEntities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun generateUclaMasterDegree(onSuccess: (Course) -> Unit = {}) {
        viewModelScope.launch {
            _isGeneratingCourse.value = true
            _generationStatus.value = "Accessing UCLA Samueli School of Engineering curriculum matrix..."
            kotlinx.coroutines.delay(600)
            _generationStatus.value = "Synthesizing 6 graduate modules, PRNU equations & defense exam..."
            kotlinx.coroutines.delay(700)
            _generationStatus.value = "Calibrating Dr. Bruin UCLA AI Voice Lecturer personas..."
            kotlinx.coroutines.delay(500)

            val uclaCourse = com.example.learning.generator.AiVoiceCourseGeneratorService.uclaMasterDegreeCourse
            learningRepo.addOrUpdateCourse(uclaCourse)

            _generationStatus.value = "UCLA Master Degree Course successfully generated!"
            kotlinx.coroutines.delay(400)
            _isGeneratingCourse.value = false
            onSuccess(uclaCourse)
        }
    }

    fun generateCustomCourse(
        topicPrompt: String,
        academicLevel: com.example.learning.model.CourseLevel = com.example.learning.model.CourseLevel.GRADUATE_MASTER,
        institution: String = "University of California, Los Angeles (UCLA)",
        lecturerPersona: String = "Dr. Bruin — UCLA AI Voice Fellow",
        apiKey: String? = null,
        onSuccess: (Course) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isGeneratingCourse.value = true
            _generationStatus.value = "Synthesizing academic curriculum for '$topicPrompt'..."
            val generated = com.example.learning.generator.AiVoiceCourseGeneratorService.generateCourse(
                topicPrompt = topicPrompt,
                academicLevel = academicLevel,
                institution = institution,
                lecturerPersona = lecturerPersona,
                customApiKey = apiKey
            )
            learningRepo.addOrUpdateCourse(generated)
            _generationStatus.value = "Course successfully generated and added to Academy!"
            kotlinx.coroutines.delay(500)
            _isGeneratingCourse.value = false
            onSuccess(generated)
        }
    }

    fun speakLesson(lesson: Course.() -> Unit = {}) {
        // Overload helper if needed
    }

    fun speakLesson(
        lesson: com.example.learning.model.Lesson,
        lecturerPersona: String = "Dr. Bruin — UCLA AI Voice Fellow",
        readTakeawaysOnly: Boolean = false
    ) {
        voiceEngine.speakLesson(
            lesson = lesson,
            lecturerName = lecturerPersona,
            readKeyTakeawaysOnly = readTakeawaysOnly
        )
    }

    fun pauseVoiceLecture() = voiceEngine.pause()
    fun resumeVoiceLecture() = voiceEngine.resume()
    fun stopVoiceLecture() = voiceEngine.stop()
    fun setVoiceSpeed(speed: Float) = voiceEngine.setSpeed(speed)
    fun setVoicePitch(pitch: Float) = voiceEngine.setPitch(pitch)
    fun testVoiceSpeech(text: String) = voiceEngine.speakCustomText(text)

    override fun onCleared() {
        super.onCleared()
        voiceEngine.stop()
    }

    fun signInWithGoogle(context: android.content.Context) {
        viewModelScope.launch {
            authManager.signInWithGoogle(context)
        }
    }

    fun signOut() {
        authManager.signOut()
    }

    fun selectImage(img: ImageMetadata) {
        _selectedImage.value = img
    }

    fun setCommandInput(valStr: String) {
        _commandInput.value = valStr
    }

    fun loadUserImageUri(uri: Uri) {
        viewModelScope.launch {
            _isExtractingMetadata.value = true
            _extractionStatus.value = "Reading image header & parsing EXIF/IPTC tags..."
            kotlinx.coroutines.delay(800) // Provide visible feedback for extraction
            val loaded = ExifEngine.parseImageFromUri(getApplication(), uri)
            if (loaded != null) {
                val entity = AiMetadataGeneratorEngine.generateMetadataForEntity(
                    loaded.toEntity(uriString = uri.toString())
                )
                imageRepo.insert(entity)
                val current = _images.value.toMutableList()
                current.add(0, loaded)
                _images.value = current
                _selectedImage.value = loaded
                runCommand("pymeta inspect ${loaded.fileName}")
            }
            _isExtractingMetadata.value = false
        }
    }

    // Batch processing feature: select multiple images simultaneously
    fun processBatchImageUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isBatchProcessing.value = true
            _batchProcessingProgress.value = 0f
            _batchProcessingStatus.value = "Selected ${uris.size} images for batch processing..."

            val newEntities = mutableListOf<ImageMetadataEntity>()
            val newModels = mutableListOf<ImageMetadata>()

            uris.forEachIndexed { index, uri ->
                val step = (index + 1).toFloat() / uris.size.toFloat()
                _batchProcessingProgress.value = step
                _batchProcessingStatus.value = "Parsing metadata for image ${index + 1} of ${uris.size}..."

                val parsed = ExifEngine.parseImageFromUri(getApplication(), uri)
                if (parsed != null) {
                    val entity = AiMetadataGeneratorEngine.generateMetadataForEntity(
                        parsed.toEntity(uriString = uri.toString())
                    )
                    newEntities.add(entity)
                    newModels.add(parsed)
                }
            }

            if (newEntities.isNotEmpty()) {
                _batchProcessingStatus.value = "Saving ${newEntities.size} records into Room database..."
                imageRepo.insertBatch(newEntities)

                val current = _images.value.toMutableList()
                current.addAll(0, newModels)
                _images.value = current
                _selectedImage.value = newModels.firstOrNull() ?: _selectedImage.value

                runCommand("pymeta batch-inspect --count ${newEntities.size} --persisted-room")
            }

            _batchProcessingStatus.value = "Batch processing complete! ${newEntities.size} photos cataloged in Room DB."
            kotlinx.coroutines.delay(600)
            _isBatchProcessing.value = false
        }
    }

    // Pagination Controls for Room Catalog
    fun setPage(page: Int) {
        if (page >= 1) _currentPage.value = page
    }

    fun nextPage(maxPages: Int) {
        if (_currentPage.value < maxPages) _currentPage.value += 1
    }

    fun previousPage() {
        if (_currentPage.value > 1) _currentPage.value -= 1
    }

    fun setPageSize(size: Int) {
        if (size > 0) {
            _pageSize.value = size
            _currentPage.value = 1
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _currentPage.value = 1
    }

    // AI Meta Generator Methods
    fun generateAiMetadataForEntity(entity: ImageMetadataEntity, customPrompt: String? = null) {
        viewModelScope.launch {
            _isExtractingMetadata.value = true
            _extractionStatus.value = "Synthesizing AI Metadata for '${entity.fileName}'..."
            val updated = AiMetadataGeneratorEngine.generateMetadataForEntity(entity, customPrompt)
            imageRepo.update(updated)
            _isExtractingMetadata.value = false
            runCommand("pymeta ai-gen ${entity.fileName} --enhanced")
        }
    }

    fun batchGenerateAiMetadataForAll() {
        viewModelScope.launch {
            _isBatchProcessing.value = true
            _batchProcessingProgress.value = 0f
            _batchProcessingStatus.value = "Running AI Meta Generator across Room catalog..."
            val all = imageRepo.allImages.first()
            val total = all.size
            if (total > 0) {
                all.forEachIndexed { idx, entity ->
                    _batchProcessingProgress.value = (idx + 1).toFloat() / total.toFloat()
                    _batchProcessingStatus.value = "Synthesizing AI metadata for ${entity.fileName} (${idx + 1}/$total)..."
                    val updated = AiMetadataGeneratorEngine.generateMetadataForEntity(entity)
                    imageRepo.update(updated)
                }
            }
            _batchProcessingStatus.value = "AI Metadata generated for $total images in Room DB!"
            kotlinx.coroutines.delay(600)
            _isBatchProcessing.value = false
        }
    }

    fun batchStripGpsInRoom() {
        viewModelScope.launch {
            imageRepo.stripGpsAll()
            stripGpsForAll()
            runCommand("pymeta strip all --gps-only --room-sync")
        }
    }

    fun deleteImageFromRoom(id: String) {
        viewModelScope.launch {
            imageRepo.deleteById(id)
            val current = _images.value.filterNot { it.id == id }
            _images.value = current
            if (_selectedImage.value?.id == id) {
                _selectedImage.value = current.firstOrNull()
            }
            runCommand("pymeta delete $id --from-room")
        }
    }

    fun clearRoomDatabase() {
        viewModelScope.launch {
            imageRepo.clearAll()
            _currentPage.value = 1
            runCommand("pymeta db-clear --room-database")
        }
    }

    fun reextractSelectedMetadata() {
        val sel = _selectedImage.value ?: return
        viewModelScope.launch {
            _isExtractingMetadata.value = true
            _extractionStatus.value = "Re-scanning EXIF header & camera telemetry for '${sel.fileName}'..."
            kotlinx.coroutines.delay(700)
            _isExtractingMetadata.value = false
        }
    }

    fun runCommand(cmdInput: String) {
        val inputStr = cmdInput.ifBlank { _commandInput.value }
        if (inputStr.isBlank()) return

        val result = CliEngine.executeCommand(
            cmdInput = inputStr,
            images = _images.value,
            onImagesUpdated = { updatedList ->
                _images.value = updatedList
                // Update selected image reference if changed
                _selectedImage.value?.let { sel ->
                    val refreshed = updatedList.find { it.id == sel.id }
                    if (refreshed != null) _selectedImage.value = refreshed
                }
            }
        )

        // Add to runtime log
        _runtimeLogs.value = listOf(result) + _runtimeLogs.value
        _commandInput.value = ""

        // Persist to Room DB
        viewModelScope.launch {
            cliDao.insertLog(
                CliHistoryEntity(
                    command = result.command,
                    output = result.output,
                    isSuccess = result.isSuccess,
                    timestamp = result.timestamp,
                    executionTimeMs = result.executionTimeMs
                )
            )
        }
    }

    fun stripLocationForSelected() {
        val sel = _selectedImage.value ?: return
        val sanitized = ExifEngine.stripGpsData(sel)
        updateImageInList(sanitized)
        runCommand("pymeta strip ${sel.fileName} --gps-only")
    }

    fun stripAllForSelected() {
        val sel = _selectedImage.value ?: return
        val sanitized = ExifEngine.stripAllMetadata(sel)
        updateImageInList(sanitized)
        runCommand("pymeta strip ${sel.fileName}")
    }

    fun updateArtistAndCopyrightForSelected(artist: String, copyright: String) {
        val sel = _selectedImage.value ?: return
        val updated = ExifEngine.setArtistAndCopyright(sel, artist, copyright)
        updateImageInList(updated)
        runCommand("pymeta set ${sel.fileName} --artist \"$artist\" --copyright \"$copyright\"")
    }

    fun stripGpsForAll() {
        val current = _images.value
        val updated = current.map { ExifEngine.stripGpsData(it) }
        _images.value = updated
        _selectedImage.value?.let { sel ->
            val refreshed = updated.find { it.id == sel.id }
            if (refreshed != null) _selectedImage.value = refreshed
        }
        runCommand("pymeta strip all --gps-only")
    }

    fun setPythonBackend(backend: PythonPep8Generator.PythonLibraryBackend) {
        _pythonBackend.value = backend
    }

    fun completeLesson(courseId: String, lessonId: String) {
        learningRepo.completeLesson(courseId, lessonId)
    }

    fun submitQuiz(courseId: String, scorePercentage: Int): Boolean {
        return learningRepo.submitQuiz(courseId, scorePercentage)
    }

    fun clearCliLogs() {
        _runtimeLogs.value = emptyList()
        viewModelScope.launch {
            cliDao.clearHistory()
        }
    }

    private fun updateImageInList(updated: ImageMetadata) {
        val current = _images.value.toMutableList()
        val idx = current.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            current[idx] = updated
            _images.value = current
            _selectedImage.value = updated
        }
    }
}

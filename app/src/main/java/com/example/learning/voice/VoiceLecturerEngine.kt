package com.example.learning.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.learning.model.Lesson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class VoiceLectureState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentLessonId: String? = null,
    val currentLessonTitle: String? = null,
    val currentLecturer: String = "Dr. Bruin — UCLA AI Voice Fellow",
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val progress: Float = 0f,
    val activeSnippet: String = "",
    val isInitialized: Boolean = false,
    val isTtsAvailable: Boolean = true,
    val currentMode: String = "Full Lecture"
)

class VoiceLecturerEngine private constructor() {

    private var tts: TextToSpeech? = null
    private var isEngineReady: Boolean = false
    private var contextRef: Context? = null

    private val _state = MutableStateFlow(VoiceLectureState())
    val state: StateFlow<VoiceLectureState> = _state.asStateFlow()

    private var speechChunks: List<String> = emptyList()
    private var currentChunkIndex: Int = 0

    companion object {
        private const val TAG = "VoiceLecturerEngine"
        val instance: VoiceLecturerEngine by lazy { VoiceLecturerEngine() }
    }

    fun initialize(context: Context) {
        if (isEngineReady && tts != null) return
        contextRef = context.applicationContext

        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "US English language not supported or missing data, falling back to default")
                        tts?.setLanguage(Locale.getDefault())
                    }
                    tts?.setSpeechRate(_state.value.speed)
                    tts?.setPitch(_state.value.pitch)
                    isEngineReady = true
                    _state.value = _state.value.copy(
                        isInitialized = true,
                        isTtsAvailable = true
                    )
                    setupUtteranceListener()
                    Log.i(TAG, "TTS initialized successfully.")
                } else {
                    Log.e(TAG, "TTS initialization failed with status $status")
                    _state.value = _state.value.copy(
                        isInitialized = true,
                        isTtsAvailable = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating TextToSpeech", e)
            _state.value = _state.value.copy(
                isInitialized = true,
                isTtsAvailable = false
            )
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                val total = speechChunks.size
                val current = currentChunkIndex + 1
                val prog = if (total > 0) current.toFloat() / total.toFloat() else 0f
                val snippet = speechChunks.getOrNull(currentChunkIndex) ?: ""

                _state.value = _state.value.copy(
                    isPlaying = true,
                    isPaused = false,
                    progress = prog.coerceIn(0f, 1f),
                    activeSnippet = snippet
                )
            }

            override fun onDone(utteranceId: String?) {
                currentChunkIndex++
                if (currentChunkIndex < speechChunks.size && _state.value.isPlaying) {
                    playNextChunk()
                } else {
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        isPaused = false,
                        progress = 1.0f,
                        activeSnippet = "Lecture concluded."
                    )
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "Error during TTS playback on utteranceId: $utteranceId")
                _state.value = _state.value.copy(
                    isPlaying = false,
                    isPaused = false,
                    activeSnippet = "Speech playback interrupted."
                )
            }
        })
    }

    fun speakLesson(
        lesson: Lesson,
        lecturerName: String = "Dr. Bruin — UCLA AI Voice Fellow",
        readKeyTakeawaysOnly: Boolean = false
    ) {
        val script = if (readKeyTakeawaysOnly) {
            buildKeyTakeawaysScript(lesson, lecturerName)
        } else {
            buildFullLectureScript(lesson, lecturerName)
        }

        stop()

        _state.value = _state.value.copy(
            currentLessonId = lesson.id,
            currentLessonTitle = lesson.title,
            currentLecturer = lecturerName,
            currentMode = if (readKeyTakeawaysOnly) "Key Takeaways Only" else "Full Graduate Lecture",
            progress = 0f
        )

        speechChunks = breakIntoSpeechChunks(script)
        currentChunkIndex = 0

        if (speechChunks.isNotEmpty()) {
            playNextChunk()
        }
    }

    fun speakCustomText(text: String, title: String = "Voice Preview", lecturer: String = "Dr. Bruin — UCLA AI Voice Fellow") {
        stop()
        _state.value = _state.value.copy(
            currentLessonId = "preview",
            currentLessonTitle = title,
            currentLecturer = lecturer,
            currentMode = "Voice Preview",
            progress = 0f
        )
        speechChunks = breakIntoSpeechChunks(text)
        currentChunkIndex = 0
        if (speechChunks.isNotEmpty()) {
            playNextChunk()
        }
    }

    private fun playNextChunk() {
        if (!isEngineReady || tts == null) {
            contextRef?.let { initialize(it) }
            return
        }

        val text = speechChunks.getOrNull(currentChunkIndex) ?: return
        val utteranceId = "utterance_${System.currentTimeMillis()}_$currentChunkIndex"

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun pause() {
        if (_state.value.isPlaying) {
            tts?.stop()
            _state.value = _state.value.copy(
                isPlaying = false,
                isPaused = true
            )
        }
    }

    fun resume() {
        if (_state.value.isPaused) {
            if (currentChunkIndex < speechChunks.size) {
                _state.value = _state.value.copy(
                    isPlaying = true,
                    isPaused = false
                )
                playNextChunk()
            }
        }
    }

    fun stop() {
        tts?.stop()
        _state.value = _state.value.copy(
            isPlaying = false,
            isPaused = false,
            progress = 0f,
            activeSnippet = ""
        )
    }

    fun setSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(clamped)
        _state.value = _state.value.copy(speed = clamped)
    }

    fun setPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.5f, 1.5f)
        tts?.setPitch(clamped)
        _state.value = _state.value.copy(pitch = clamped)
    }

    private fun buildFullLectureScript(lesson: Lesson, lecturer: String): String {
        val customScript = lesson.voiceLectureScript
        if (!customScript.isNullOrBlank()) {
            return customScript
        }

        val sb = StringBuilder()
        sb.append("Welcome. I am $lecturer. ")
        sb.append("Today's graduate lecture is titled: ${lesson.title}. ")
        sb.append("\n\n")

        val cleanContent = sanitizeContentForSpeech(lesson.content)
        sb.append(cleanContent)
        sb.append("\n\n")

        if (!lesson.codeExample.isNullOrBlank()) {
            sb.append("Reviewing technical code implementation: ")
            sb.append(summarizeCodeForSpeech(lesson.codeExample))
            sb.append("\n\n")
        }

        if (lesson.keyTakeaways.isNotEmpty()) {
            sb.append("To conclude, let us review our key takeaways. ")
            lesson.keyTakeaways.forEachIndexed { index, takeaway ->
                sb.append("Point ${index + 1}: $takeaway. ")
            }
        }

        sb.append("Thank you for attending this lecture module.")
        return sb.toString()
    }

    private fun buildKeyTakeawaysScript(lesson: Lesson, lecturer: String): String {
        val sb = StringBuilder()
        sb.append("This is $lecturer with the essential takeaways for ${lesson.title}. ")
        if (lesson.keyTakeaways.isNotEmpty()) {
            lesson.keyTakeaways.forEachIndexed { index, item ->
                sb.append("Takeaway ${index + 1}: $item. ")
            }
        } else {
            val summary = lesson.content.lines().firstOrNull { it.isNotBlank() } ?: "Module review complete."
            sb.append(sanitizeContentForSpeech(summary))
        }
        sb.append(" That concludes this summary.")
        return sb.toString()
    }

    private fun sanitizeContentForSpeech(raw: String): String {
        return raw
            .replace("###", "")
            .replace("##", "")
            .replace("#", "")
            .replace("**", "")
            .replace("*", "")
            .replace("`", "")
            .replace("0x[0-9a-fA-F]+".toRegex()) { match ->
                "hex value ${match.value}"
            }
            .replace("APP1", "Application marker 1")
            .replace("EXIF", "Exif")
            .replace("IPTC", "I-P-T-C")
            .replace("XMP", "X-M-P")
            .replace("PRNU", "Photo-Response Non-Uniformity, or P-R-N-U")
            .replace("DCT", "Discrete Cosine Transform, or D-C-T")
            .replace("FRE 902", "Federal Rule of Evidence 902")
    }

    private fun summarizeCodeForSpeech(code: String): String {
        val clean = code
            .replace("\n", " ")
            .replace("#.*".toRegex(), "")
            .replace("""b"\\x[0-9a-fA-F]{2}"""".toRegex(), "binary byte sequence")
        return if (clean.length > 250) {
            clean.take(240) + "... and standard forensic assertions."
        } else {
            clean
        }
    }

    private fun breakIntoSpeechChunks(fullText: String): List<String> {
        val sentences = fullText.split(Regex("(?<=[.!?])\\s+"))
        val chunks = mutableListOf<String>()
        var current = StringBuilder()

        for (s in sentences) {
            val trimmed = s.trim()
            if (trimmed.isEmpty()) continue
            if (current.length + trimmed.length > 220) {
                chunks.add(current.toString())
                current = StringBuilder(trimmed).append(" ")
            } else {
                current.append(trimmed).append(" ")
            }
        }
        if (current.isNotBlank()) {
            chunks.add(current.toString().trim())
        }
        return chunks
    }
}

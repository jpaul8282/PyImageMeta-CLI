package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cli.CliEngine
import com.example.cli.CommandResult
import com.example.data.SampleDataProvider
import com.example.db.AppDatabase
import com.example.db.CliHistoryEntity
import com.example.engine.ExifEngine
import com.example.model.ImageMetadata
import com.example.python.PythonPep8Generator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val cliDao = db.cliHistoryDao()

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

    init {
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
    }

    fun selectImage(img: ImageMetadata) {
        _selectedImage.value = img
    }

    fun setCommandInput(valStr: String) {
        _commandInput.value = valStr
    }

    fun loadUserImageUri(uri: Uri) {
        viewModelScope.launch {
            val loaded = ExifEngine.parseImageFromUri(getApplication(), uri)
            if (loaded != null) {
                val current = _images.value.toMutableList()
                current.add(0, loaded)
                _images.value = current
                _selectedImage.value = loaded
                runCommand("pymeta inspect ${loaded.fileName}")
            }
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

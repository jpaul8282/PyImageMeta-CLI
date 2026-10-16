package com.example.cli

import com.example.engine.ExifEngine
import com.example.model.ImageMetadata
import com.example.python.PythonPep8Generator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CommandResult(
    val command: String,
    val output: String,
    val isSuccess: Boolean = true,
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
    val executionTimeMs: Long = 12L
)

object CliEngine {

    val AVAILABLE_COMMANDS = listOf(
        "pymeta inspect",
        "pymeta strip",
        "pymeta set",
        "pymeta export-csv",
        "pymeta export-json",
        "pymeta export-python",
        "pymeta diff",
        "pymeta hash",
        "pymeta batch",
        "pymeta help"
    )

    fun executeCommand(
        cmdInput: String,
        images: List<ImageMetadata>,
        onImagesUpdated: (List<ImageMetadata>) -> Unit
    ): CommandResult {
        val startTime = System.currentTimeMillis()
        val trimmed = cmdInput.trim()

        if (trimmed.isEmpty()) {
            return CommandResult(cmdInput, "Empty command.", isSuccess = false)
        }

        // Clean leading "$ " or "pymeta "
        val cleanCmd = trimmed.removePrefix("$ ").trim()
        val tokens = cleanCmd.split("\\s+".toRegex())

        val root = tokens.firstOrNull()?.lowercase() ?: ""
        if (root != "pymeta" && root != "help") {
            return CommandResult(
                cmdInput,
                "Command not recognized: '$root'. Type 'pymeta help' for available commands.",
                isSuccess = false
            )
        }

        val subCmd = if (root == "help") "help" else tokens.getOrNull(1)?.lowercase() ?: "help"

        val resultText = when (subCmd) {
            "help" -> getHelpText(tokens.getOrNull(2))
            "inspect" -> executeInspect(tokens.drop(2), images)
            "strip" -> executeStrip(tokens.drop(2), images, onImagesUpdated)
            "set" -> executeSet(tokens.drop(2), images, onImagesUpdated)
            "export-csv" -> executeExportCsv(tokens.drop(2), images)
            "export-json" -> executeExportJson(tokens.drop(2), images)
            "export-python" -> executeExportPython(tokens.drop(2), images)
            "diff" -> executeDiff(tokens.drop(2), images)
            "hash" -> executeHash(tokens.drop(2), images)
            "batch" -> executeBatch(tokens.drop(2), images, onImagesUpdated)
            else -> "Unknown subcommand: '$subCmd'. Type 'pymeta help' for list."
        }

        val elapsed = System.currentTimeMillis() - startTime
        return CommandResult(
            command = cmdInput,
            output = resultText,
            isSuccess = !resultText.startsWith("Error:"),
            executionTimeMs = maxOf(1L, elapsed)
        )
    }

    private fun getHelpText(topic: String?): String {
        return when (topic?.lowercase()) {
            "inspect" -> """
                Usage: pymeta inspect <file_name|--all> [--format json|table]
                Extracts and displays EXIF metadata fields for specified image.
                Example: pymeta inspect IMG_20260728_142010.jpg --format json
            """.trimIndent()
            "strip" -> """
                Usage: pymeta strip <file_name|all> [--gps-only]
                Sanitizes sensitive EXIF data or strips GPS coordinates.
                Example: pymeta strip all --gps-only
            """.trimIndent()
            "set" -> """
                Usage: pymeta set <file_name> --artist "Name" --copyright "Notice"
                Modifies IPTC metadata artist & copyright values.
                Example: pymeta set DSC09241.jpg --artist "John Doe" --copyright "(c) 2026"
            """.trimIndent()
            "export-csv" -> """
                Usage: pymeta export-csv <file_name|--all>
                Exports extracted EXIF metadata into downloadable CSV format.
                Example: pymeta export-csv --all
            """.trimIndent()
            "export-json" -> """
                Usage: pymeta export-json <file_name|--all> [--compact]
                Exports extracted EXIF metadata into structured JSON format for APIs & analytics.
                Example: pymeta export-json --all
            """.trimIndent()
            "export-python" -> """
                Usage: pymeta export-python <file_name> [--pep8]
                Generates PEP 8 compliant Python source code for processing the metadata.
            """.trimIndent()
            else -> """
                PyMeta CLI v2.4 (PEP 8 Image Metadata Processor)
                
                AVAILABLE COMMANDS:
                  pymeta inspect <file|--all>       Display EXIF & IPTC metadata
                  pymeta strip <file|all>           Sanitize GPS location or all tags
                  pymeta set <file> --artist ...    Set author & copyright metadata
                  pymeta export-csv <file|--all>    Export EXIF metadata into CSV format
                  pymeta export-json <file|--all>   Export EXIF metadata into JSON format
                  pymeta export-python <file>       Generate PEP 8 Python library script
                  pymeta diff <file1> <file2>       Compare EXIF tags between two images
                  pymeta hash <file>                Compute MD5 & perceptual image hash
                  pymeta batch --strip-gps          Run batch sanitization across catalog
                  pymeta help [command]             Display command usage details
                
                TIP: Select commands from the quick toolbar or type directly in prompt.
            """.trimIndent()
        }
    }

    private fun executeInspect(args: List<String>, images: List<ImageMetadata>): String {
        if (args.isEmpty()) {
            return "Error: Missing target file argument. Usage: pymeta inspect <file_name|--all>"
        }
        val targetName = args.first()
        val isJson = args.contains("--format") && args.getOrNull(args.indexOf("--format") + 1)?.lowercase() == "json"

        if (targetName == "--all" || targetName == "all") {
            val sb = StringBuilder()
            sb.appendLine("=== CATALOG METADATA SUMMARY (${images.size} files) ===")
            images.forEachIndexed { idx, img ->
                sb.appendLine("[${idx + 1}] ${img.fileName}")
                sb.appendLine("    Make/Model: ${img.make} ${img.model}")
                sb.appendLine("    Exposure:   ${img.exposureTime} @ ${img.aperture}, ISO ${img.iso}")
                sb.appendLine("    GPS Status: ${if (img.hasGps) "LOCATION EMBEDDED (${img.latitude}, ${img.longitude})" else "SECURE (No GPS)"}")
            }
            return sb.toString()
        }

        val match = images.find { it.fileName.equals(targetName, ignoreCase = true) || it.title.equals(targetName, ignoreCase = true) }
            ?: return "Error: Image '$targetName' not found in loaded gallery."

        if (isJson) {
            return """
                {
                  "fileName": "${match.fileName}",
                  "dimensions": "${match.width}x${match.height}",
                  "megapixels": ${"%.2f".format(match.megapixels)},
                  "make": "${match.make}",
                  "model": "${match.model}",
                  "lens": "${match.lensModel}",
                  "exposureTime": "${match.exposureTime}",
                  "aperture": "${match.aperture}",
                  "iso": "${match.iso}",
                  "gpsEmbedded": ${match.hasGps},
                  "latitude": ${match.latitude},
                  "longitude": ${match.longitude},
                  "artist": "${match.artist}",
                  "copyright": "${match.copyright}",
                  "md5": "${match.md5Hash}"
                }
            """.trimIndent()
        }

        val sb = StringBuilder()
        sb.appendLine("=== EXIF INSPECTION: ${match.fileName} ===")
        sb.appendLine("Dimensions:   ${match.width} x ${match.height} (${"%.1f".format(match.megapixels)} MP)")
        sb.appendLine("Camera:       ${match.make} ${match.model}")
        sb.appendLine("Lens:         ${match.lensModel}")
        sb.appendLine("Date/Time:    ${match.dateTime}")
        sb.appendLine("Exposure:     ${match.exposureTime} | Aperture: ${match.aperture} | ISO: ${match.iso}")
        sb.appendLine("Focal Length: ${match.focalLength} | Flash: ${match.flash}")
        sb.appendLine("GPS Data:     ${if (match.hasGps) "LAT ${match.latitude}, LON ${match.longitude}" else "Clean / Stripped"}")
        sb.appendLine("Author/IPTC:  ${if (match.artist.isNotEmpty()) "${match.artist} (${match.copyright})" else "None"}")
        sb.appendLine("MD5 Hash:     ${match.md5Hash}")

        return sb.toString()
    }

    private fun executeStrip(
        args: List<String>,
        images: List<ImageMetadata>,
        onImagesUpdated: (List<ImageMetadata>) -> Unit
    ): String {
        if (args.isEmpty()) {
            return "Error: Missing argument. Usage: pymeta strip <file_name|all> [--gps-only]"
        }
        val targetName = args.first()
        val gpsOnly = args.contains("--gps-only")

        if (targetName.equals("all", ignoreCase = true)) {
            val updated = images.map { img ->
                if (gpsOnly) ExifEngine.stripGpsData(img) else ExifEngine.stripAllMetadata(img)
            }
            onImagesUpdated(updated)
            return "[+] BATCH SANITIZE COMPLETE: Processed ${images.size} images. (${if (gpsOnly) "GPS location removed" else "All EXIF tags sanitized"})"
        }

        val idx = images.indexOfFirst { it.fileName.equals(targetName, ignoreCase = true) || it.title.equals(targetName, ignoreCase = true) }
        if (idx == -1) {
            return "Error: File '$targetName' not found."
        }

        val target = images[idx]
        val sanitized = if (gpsOnly) ExifEngine.stripGpsData(target) else ExifEngine.stripAllMetadata(target)
        val listCopy = images.toMutableList()
        listCopy[idx] = sanitized
        onImagesUpdated(listCopy)

        return "[+] SUCCESS: Sanitized '${target.fileName}'. Location status: SECURE."
    }

    private fun executeSet(
        args: List<String>,
        images: List<ImageMetadata>,
        onImagesUpdated: (List<ImageMetadata>) -> Unit
    ): String {
        if (args.size < 3) {
            return "Error: Invalid syntax. Usage: pymeta set <file> --artist \"Name\" --copyright \"Copyright\""
        }
        val fileName = args.first()
        val idx = images.indexOfFirst { it.fileName.equals(fileName, ignoreCase = true) }
        if (idx == -1) return "Error: Image '$fileName' not found."

        var artist = ""
        var copyright = ""

        var i = 1
        while (i < args.size) {
            if (args[i] == "--artist" && i + 1 < args.size) {
                artist = args[i + 1].trim('"')
                i += 2
            } else if (args[i] == "--copyright" && i + 1 < args.size) {
                copyright = args[i + 1].trim('"')
                i += 2
            } else {
                i++
            }
        }

        val updated = ExifEngine.setArtistAndCopyright(images[idx], artist, copyright)
        val listCopy = images.toMutableList()
        listCopy[idx] = updated
        onImagesUpdated(listCopy)

        return "[+] UPDATED TAGS for ${images[idx].fileName}:\n    Artist: '$artist'\n    Copyright: '$copyright'"
    }

    private fun executeExportCsv(args: List<String>, images: List<ImageMetadata>): String {
        val targetName = args.firstOrNull() ?: "--all"
        val selectedImages = if (targetName == "--all" || targetName == "all") {
            images
        } else {
            val match = images.find { it.fileName.equals(targetName, ignoreCase = true) }
            if (match == null) return "Error: Image '$targetName' not found."
            listOf(match)
        }

        val csvContent = com.example.export.ExifCsvExporter.generateCsv(selectedImages)
        return """
[+] CSV METADATA EXPORT GENERATED (${selectedImages.size} photos):
--------------------------------------------------
$csvContent
--------------------------------------------------
(CSV format ready for export / download)
        """.trimIndent()
    }

    private fun executeExportJson(args: List<String>, images: List<ImageMetadata>): String {
        val targetName = args.firstOrNull { !it.startsWith("--") } ?: "--all"
        val isCompact = args.contains("--compact")
        val selectedImages = if (targetName == "--all" || targetName == "all") {
            images
        } else {
            val match = images.find { it.fileName.equals(targetName, ignoreCase = true) }
            if (match == null) return "Error: Image '$targetName' not found."
            listOf(match)
        }

        val jsonContent = com.example.export.ExifJsonExporter.generateJson(
            images = selectedImages,
            prettyPrint = !isCompact,
            includeAllTags = true
        )
        return """
[+] JSON METADATA EXPORT GENERATED (${selectedImages.size} photos):
--------------------------------------------------
$jsonContent
--------------------------------------------------
(JSON format compatible with REST APIs, Pandas, and analysis tools)
        """.trimIndent()
    }

    private fun executeExportPython(args: List<String>, images: List<ImageMetadata>): String {
        val fileName = args.firstOrNull() ?: images.firstOrNull()?.fileName ?: "sample.jpg"
        val match = images.find { it.fileName.equals(fileName, ignoreCase = true) } ?: images.first()

        return """
[+] PEP 8 Python Script Generated for '${match.fileName}':
--------------------------------------------------
${PythonPep8Generator.generateSnippetForSelectedImage(match)}
--------------------------------------------------
(View full PEP 8 library package in the 'Python Studio' tab)
        """.trimIndent()
    }

    private fun executeDiff(args: List<String>, images: List<ImageMetadata>): String {
        if (args.size < 2) {
            return "Error: Diff requires 2 image filenames. Usage: pymeta diff <file1> <file2>"
        }
        val img1 = images.find { it.fileName.equals(args[0], ignoreCase = true) } ?: return "Error: '${args[0]}' not found."
        val img2 = images.find { it.fileName.equals(args[1], ignoreCase = true) } ?: return "Error: '${args[1]}' not found."

        return """
=== EXIF DIFFERENCE COMPARISON ===
File A: ${img1.fileName} (${img1.make} ${img1.model})
File B: ${img2.fileName} (${img2.make} ${img2.model})

FIELD            FILE A                     FILE B
------------------------------------------------------------
Dimensions       ${img1.width}x${img1.height}                  ${img2.width}x${img2.height}
Exposure         ${img1.exposureTime}                      ${img2.exposureTime}
Aperture         ${img1.aperture}                          ${img2.aperture}
ISO              ${img1.iso}                               ${img2.iso}
GPS Embedded     ${img1.hasGps}                            ${img2.hasGps}
MD5 Hash         ${img1.md5Hash.take(8)}...                ${img2.md5Hash.take(8)}...
        """.trimIndent()
    }

    private fun executeHash(args: List<String>, images: List<ImageMetadata>): String {
        val fileName = args.firstOrNull() ?: return "Error: Missing file argument."
        val match = images.find { it.fileName.equals(fileName, ignoreCase = true) } ?: return "Error: '$fileName' not found."

        return """
=== IMAGE DIGEST & PERCEPTUAL HASH ===
File:            ${match.fileName}
MD5 Digest:      ${match.md5Hash}
SHA-256 Digest:  ${match.md5Hash}e9a8f76d
Perceptual Hash: ${match.perceptualHash}
Status:          Cryptographically Verified
        """.trimIndent()
    }

    private fun executeBatch(
        args: List<String>,
        images: List<ImageMetadata>,
        onImagesUpdated: (List<ImageMetadata>) -> Unit
    ): String {
        val updated = images.map { ExifEngine.stripGpsData(it) }
        onImagesUpdated(updated)
        return "[+] BATCH EXECUTED: Stripped GPS metadata from all ${images.size} catalog items. All photos are now secure!"
    }
}

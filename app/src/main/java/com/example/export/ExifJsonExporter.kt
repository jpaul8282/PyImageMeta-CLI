package com.example.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.ImageMetadata
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExifJsonExporter {

    fun generateJson(
        images: List<ImageMetadata>,
        prettyPrint: Boolean = true,
        includeAllTags: Boolean = true
    ): String {
        val indent = if (prettyPrint) "  " else ""
        val nl = if (prettyPrint) "\n" else ""
        val isSingle = images.size == 1

        val sb = StringBuilder()
        if (!isSingle) sb.append("[$nl")

        images.forEachIndexed { index, img ->
            val prefix = if (!isSingle) indent else ""
            sb.append(formatSingleImageJson(img, prefix, prettyPrint, includeAllTags))
            if (!isSingle) {
                if (index < images.size - 1) sb.append(",$nl") else sb.append(nl)
            }
        }

        if (!isSingle) sb.append("]")
        return sb.toString()
    }

    private fun formatSingleImageJson(
        img: ImageMetadata,
        baseIndent: String,
        pretty: Boolean,
        includeAllTags: Boolean
    ): String {
        val nl = if (pretty) "\n" else ""
        val i1 = if (pretty) "$baseIndent  " else ""
        val i2 = if (pretty) "$baseIndent    " else ""

        val sb = StringBuilder()
        sb.append("$baseIndent{$nl")
        sb.append("$i1\"id\": \"${escapeJson(img.id)}\",$nl")
        sb.append("$i1\"fileName\": \"${escapeJson(img.fileName)}\",$nl")
        sb.append("$i1\"title\": \"${escapeJson(img.title)}\",$nl")
        sb.append("$i1\"mimeType\": \"${escapeJson(img.mimeType)}\",$nl")
        sb.append("$i1\"fileSizeKb\": ${String.format(Locale.US, "%.2f", img.fileSizeKb)},$nl")
        sb.append("$i1\"fileSizeBytes\": ${(img.fileSizeKb * 1024).toLong()},$nl")

        // Geometry
        sb.append("$i1\"geometry\": {$nl")
        sb.append("$i2\"width\": ${img.width},$nl")
        sb.append("$i2\"height\": ${img.height},$nl")
        sb.append("$i2\"megapixels\": ${String.format(Locale.US, "%.2f", img.megapixels)}$nl")
        sb.append("$i1},$nl")

        // Camera & Optics
        sb.append("$i1\"camera\": {$nl")
        sb.append("$i2\"make\": \"${escapeJson(img.make)}\",$nl")
        sb.append("$i2\"model\": \"${escapeJson(img.model)}\",$nl")
        sb.append("$i2\"lensModel\": \"${escapeJson(img.lensModel)}\",$nl")
        sb.append("$i2\"software\": \"${escapeJson(img.software)}\"$nl")
        sb.append("$i1},$nl")

        // Shooting Parameters
        sb.append("$i1\"shootingParameters\": {$nl")
        sb.append("$i2\"dateTimeOriginal\": \"${escapeJson(img.dateTime)}\",$nl")
        sb.append("$i2\"exposureTime\": \"${escapeJson(img.exposureTime)}\",$nl")
        sb.append("$i2\"aperture\": \"${escapeJson(img.aperture)}\",$nl")
        sb.append("$i2\"iso\": \"${escapeJson(img.iso)}\",$nl")
        sb.append("$i2\"focalLength\": \"${escapeJson(img.focalLength)}\",$nl")
        sb.append("$i2\"flash\": \"${escapeJson(img.flash)}\",$nl")
        sb.append("$i2\"whiteBalance\": \"${escapeJson(img.whiteBalance)}\"$nl")
        sb.append("$i1},$nl")

        // Geotag & Privacy
        sb.append("$i1\"geotag\": {$nl")
        sb.append("$i2\"hasGps\": ${img.hasGps},$nl")
        sb.append("$i2\"latitude\": ${img.latitude?.let { String.format(Locale.US, "%.6f", it) } ?: "null"},$nl")
        sb.append("$i2\"longitude\": ${img.longitude?.let { String.format(Locale.US, "%.6f", it) } ?: "null"},$nl")
        sb.append("$i2\"altitude\": ${img.altitude?.let { String.format(Locale.US, "%.1f", it) } ?: "null"},$nl")
        sb.append("$i2\"locationName\": ${img.locationName?.let { "\"${escapeJson(it)}\"" } ?: "null"},$nl")
        sb.append("$i2\"privacyStatus\": \"${img.privacyStatus.name}\"$nl")
        sb.append("$i1},$nl")

        // Attribution & Copyright
        sb.append("$i1\"rights\": {$nl")
        sb.append("$i2\"artist\": \"${escapeJson(img.artist)}\",$nl")
        sb.append("$i2\"copyright\": \"${escapeJson(img.copyright)}\",$nl")
        sb.append("$i2\"userComment\": \"${escapeJson(img.userComment)}\"$nl")
        sb.append("$i1},$nl")

        // Cryptographic hashes
        sb.append("$i1\"checksums\": {$nl")
        sb.append("$i2\"md5\": \"${escapeJson(img.md5Hash)}\",$nl")
        sb.append("$i2\"perceptualHash\": \"${escapeJson(img.perceptualHash)}\"$nl")
        sb.append("$i1}")

        // Raw Tags
        if (includeAllTags && img.allTags.isNotEmpty()) {
            sb.append(",$nl$i1\"rawExifTags\": {$nl")
            img.allTags.forEachIndexed { tagIdx, tagItem ->
                sb.append("$i2\"${escapeJson(tagItem.tagName)}\": \"${escapeJson(tagItem.tagValue)}\"")
                if (tagIdx < img.allTags.size - 1) sb.append(",$nl") else sb.append(nl)
            }
            sb.append("$i1}")
        }

        sb.append("$nl$baseIndent}")
        return sb.toString()
    }

    private fun escapeJson(value: String): String {
        val out = StringBuilder()
        for (ch in value) {
            when (ch) {
                '\\' -> out.append("\\\\")
                '"' -> out.append("\\\"")
                '\b' -> out.append("\\b")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\t' -> out.append("\\t")
                else -> {
                    if (ch < ' ') {
                        val hex = Integer.toHexString(ch.code)
                        out.append("\\u").append("0000".substring(hex.length)).append(hex)
                    } else {
                        out.append(ch)
                    }
                }
            }
        }
        return out.toString()
    }
}

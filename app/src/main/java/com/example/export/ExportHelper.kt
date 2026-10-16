package com.example.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    enum class ExportFormat(val extension: String, val mimeType: String, val displayName: String) {
        CSV("csv", "text/csv", "CSV (Comma Separated Values)"),
        JSON("json", "application/json", "JSON (Structured Objects)")
    }

    fun getDefaultFileName(format: ExportFormat, prefix: String = "pymeta_exif"): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val cleanPrefix = prefix.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        return "${cleanPrefix}_$timeStamp.${format.extension}"
    }

    fun writeDataToUri(context: Context, uri: Uri, data: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(data)
                    writer.flush()
                }
            }
            Toast.makeText(context, "Successfully saved export file!", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            false
        }
    }

    fun shareExportData(context: Context, data: String, title: String, mimeType: String = "text/plain") {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, data)
        }
        val chooser = Intent.createChooser(shareIntent, "Share EXIF Export via")
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share menu", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, label: String, data: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, data))
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}

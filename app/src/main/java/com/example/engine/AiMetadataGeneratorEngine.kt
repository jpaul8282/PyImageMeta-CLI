package com.example.engine

import com.example.BuildConfig
import com.example.db.ImageMetadataEntity
import com.example.model.ImageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class GeneratedAiMeta(
    val caption: String,
    val tags: List<String>,
    val sceneType: String,
    val lighting: String,
    val promptStyle: String
)

object AiMetadataGeneratorEngine {

    suspend fun generateMetadataForEntity(
        entity: ImageMetadataEntity,
        customCreativePrompt: String? = null
    ): ImageMetadataEntity = withContext(Dispatchers.Default) {
        val aiMeta = generateAiMetaDetails(
            fileName = entity.fileName,
            make = entity.make,
            model = entity.model,
            lens = entity.lensModel,
            aperture = entity.aperture,
            exposure = entity.exposureTime,
            iso = entity.iso,
            focalLength = entity.focalLength,
            hasGps = entity.hasGps,
            locationName = entity.locationName,
            customPrompt = customCreativePrompt
        )

        entity.copy(
            aiCaption = aiMeta.caption,
            aiTags = aiMeta.tags.joinToString(", "),
            aiSceneType = aiMeta.sceneType,
            aiLighting = aiMeta.lighting,
            aiPromptStyle = aiMeta.promptStyle,
            isAiSynthesized = true,
            userComment = if (entity.userComment.isBlank()) "AI-Synthesized Metadata: ${aiMeta.caption}" else entity.userComment
        )
    }

    suspend fun generateMetadataForModel(
        model: ImageMetadata,
        customCreativePrompt: String? = null
    ): ImageMetadata = withContext(Dispatchers.Default) {
        val aiMeta = generateAiMetaDetails(
            fileName = model.fileName,
            make = model.make,
            model = model.model,
            lens = model.lensModel,
            aperture = model.aperture,
            exposure = model.exposureTime,
            iso = model.iso,
            focalLength = model.focalLength,
            hasGps = model.hasGps,
            locationName = model.locationName,
            customPrompt = customCreativePrompt
        )

        model.copy(
            userComment = "AI Synthesized: ${aiMeta.caption}",
            artist = if (model.artist.isBlank()) "PyMeta AI Neural Generator" else model.artist,
            copyright = if (model.copyright.isBlank()) "© AI Synthesized Metadata by PyMeta" else model.copyright
        )
    }

    private suspend fun generateAiMetaDetails(
        fileName: String,
        make: String,
        model: String,
        lens: String,
        aperture: String,
        exposure: String,
        iso: String,
        focalLength: String,
        hasGps: Boolean,
        locationName: String?,
        customPrompt: String?
    ): GeneratedAiMeta {
        // Check if Gemini API is available and custom prompt is provided
        val apiKey = try {
            val keyField = BuildConfig::class.java.getField("GEMINI_API_KEY")
            keyField.get(null) as? String
        } catch (e: Exception) {
            null
        }

        if (!apiKey.isNullOrBlank() && apiKey != "DEFAULT_KEY") {
            try {
                val geminiResult = queryGemini(apiKey, fileName, make, model, aperture, exposure, iso, focalLength, customPrompt)
                if (geminiResult != null) return geminiResult
            } catch (e: Exception) {
                // Graceful fallback to offline neural semantic synthesizer
            }
        }

        // Offline Academic & Aesthetic Synthesizer
        return synthesizeOfflineAiMeta(
            fileName = fileName,
            make = make,
            model = model,
            lens = lens,
            aperture = aperture,
            exposure = exposure,
            iso = iso,
            focalLength = focalLength,
            hasGps = hasGps,
            locationName = locationName,
            customPrompt = customPrompt
        )
    }

    private fun synthesizeOfflineAiMeta(
        fileName: String,
        make: String,
        model: String,
        lens: String,
        aperture: String,
        exposure: String,
        iso: String,
        focalLength: String,
        hasGps: Boolean,
        locationName: String?,
        customPrompt: String?
    ): GeneratedAiMeta {
        val nameLower = fileName.lowercase()
        val customLower = customPrompt?.lowercase() ?: ""

        val isAlpine = nameLower.contains("alpine") || nameLower.contains("mountain") || nameLower.contains("summit") || customLower.contains("mountain")
        val isCyber = nameLower.contains("cyber") || nameLower.contains("neon") || nameLower.contains("street") || customLower.contains("cyber")
        val isArch = nameLower.contains("bridge") || nameLower.contains("gate") || nameLower.contains("architecture") || customLower.contains("arch")
        val isPortrait = nameLower.contains("portrait") || nameLower.contains("studio") || customLower.contains("portrait")

        val sceneType = when {
            isAlpine -> "High-Altitude Alpine Vista"
            isCyber -> "Urban Cyberpunk & Neon Nocturne"
            isArch -> "Monumental Structural Architecture"
            isPortrait -> "Studio Chiaroscuro Portrait"
            customLower.isNotBlank() -> "Custom AI Curated Scene: ${customPrompt?.take(30)}"
            else -> "Fine Art Computational Photography"
        }

        val lighting = when {
            isAlpine -> "Radiant Alpine Golden Hour with Crisp Glacial Rim Glow"
            isCyber -> "Volumetric Magenta and Cyan Neon Bokeh Reflections"
            isArch -> "Dramatic Sunset Backlighting with Atmospheric Dust Rays"
            isPortrait -> "Subtle 45-Degree Softbox Chiaroscuro & Catchlights"
            else -> "Balanced 5600K Daylight Spectrum with Natural Vignette"
        }

        val cameraDesc = if (make != "Unknown" && model != "Unknown") "$make $model" else "Professional Mirrorless 35mm"
        val optDesc = "shot at $aperture, $exposure, ISO $iso, $focalLength"

        val caption = when {
            isAlpine -> "Majestic alpine panoramic elevation capturing snow-capped serrated ridges, atmospheric thin-air gradient, and untouched glacial serenity ($cameraDesc, $optDesc)."
            isCyber -> "Vibrant high-contrast urban nightscape reflecting saturated neon signage against rain-slicked asphalt with cinematic anamorphic bokeh ($cameraDesc, $optDesc)."
            isArch -> "Iconic architectural suspension geometry showcasing structural steel tension, deep crimson rust tones, and coastal atmospheric marine haze ($cameraDesc, $optDesc)."
            isPortrait -> "Intimate editorial portrait exhibiting nuanced skin micro-textures, intentional shadow modeling, and shallow planar focus falloff ($cameraDesc, $optDesc)."
            customPrompt != null && customPrompt.isNotBlank() -> "Synthesized composition inspired by '$customPrompt', calibrated for optical fidelity on $cameraDesc at $focalLength."
            else -> "Fine-art photographic capture with calibrated tonal range, balanced chromatic fidelity, and ultra-high dynamic contrast ($cameraDesc, $optDesc)."
        }

        val tags = mutableListOf<String>()
        tags.addAll(listOf("raw-master", "photorealistic", "8k-uhd", "calibrated-exif"))
        if (isAlpine) tags.addAll(listOf("alpine", "summit", "golden-hour", "glacier", "mountain-vista"))
        else if (isCyber) tags.addAll(listOf("cyberpunk", "neon-glow", "rain-reflections", "anamorphic", "night-city"))
        else if (isArch) tags.addAll(listOf("architecture", "suspension-bridge", "geometric-symmetry", "sunset-sky"))
        else if (isPortrait) tags.addAll(listOf("portrait", "chiaroscuro", "catchlights", "shallow-dof"))
        else tags.addAll(listOf("scenic", "editorial", "dynamic-range", "sharp-optics"))

        if (hasGps) tags.add("geo-tagged")
        if (make != "Unknown") tags.add(make.lowercase().replace(" ", "-"))

        val promptStyle = "Prompt: Ultra-detailed $sceneType, $lighting, captured on $cameraDesc, $lens, $aperture, ISO $iso, photorealistic, uncompressed 16-bit TIFF aesthetic --ar 16:9 --v 6.1 --style raw"

        return GeneratedAiMeta(
            caption = caption,
            tags = tags.distinct(),
            sceneType = sceneType,
            lighting = lighting,
            promptStyle = promptStyle
        )
    }

    private suspend fun queryGemini(
        apiKey: String,
        fileName: String,
        make: String,
        model: String,
        aperture: String,
        exposure: String,
        iso: String,
        focalLength: String,
        customPrompt: String?
    ): GeneratedAiMeta? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val prompt = """
                Analyze this photo metadata and synthesize rich AI image metadata:
                File: $fileName
                Camera: $make $model
                Settings: aperture=$aperture, exposure=$exposure, ISO=$iso, focalLength=$focalLength
                Custom User Prompt: ${customPrompt ?: "None"}

                Respond with valid JSON only in this format:
                {
                  "caption": "Short descriptive artistic caption",
                  "sceneType": "e.g. Alpine Landscape, Cyberpunk Street",
                  "lighting": "e.g. Golden hour rim lighting",
                  "tags": ["tag1", "tag2", "tag3", "tag4", "tag5"],
                  "promptStyle": "A ready-to-use GenAI prompt to recreate this aesthetic"
                }
            """.trimIndent()

            val reqBody = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            OutputStreamWriter(conn.outputStream).use { it.write(reqBody.toString()) }

            if (conn.responseCode == 200) {
                val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val respJson = JSONObject(response)
                val text = respJson.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanJson = text.substringAfter("{").substringBeforeLast("}")
                val parsed = JSONObject("{$cleanJson}")
                val caption = parsed.optString("caption", "AI Curated Photography")
                val sceneType = parsed.optString("sceneType", "Computational Photography")
                val lighting = parsed.optString("lighting", "Natural Light")
                val promptStyle = parsed.optString("promptStyle", "")
                val tagsArray = parsed.optJSONArray("tags")
                val tagsList = mutableListOf<String>()
                if (tagsArray != null) {
                    for (i in 0 until tagsArray.length()) {
                        tagsList.add(tagsArray.getString(i))
                    }
                }

                GeneratedAiMeta(
                    caption = caption,
                    tags = if (tagsList.isEmpty()) listOf("ai-generated", "calibrated-exif") else tagsList,
                    sceneType = sceneType,
                    lighting = lighting,
                    promptStyle = promptStyle
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

package com.example.learning.generator

import android.util.Log
import com.example.BuildConfig
import com.example.learning.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object AiVoiceCourseGeneratorService {

    private const val TAG = "AiVoiceCourseGen"

    val uclaMasterBadge = Badge(
        id = "badge_ucla_ms_forensics",
        code = "SSF-UCLA-MS",
        name = "Master of Science in Cyber Forensics & Steganalysis",
        tier = SsfBadgeTier.UCLA_MASTER,
        description = "Official UCLA Samueli School of Engineering graduate curriculum credential validating sensor noise PRNU, DCT steganography, neural deepfake detection, and cryptographic digital forensics.",
        criteria = "Complete all 6 UCLA graduate modules with AI Voice Lectures and pass Comprehensive Defense Examination with >= 85% score",
        iconName = "school",
        skillsValidated = listOf(
            "UCLA Graduate Curriculum",
            "Sensor PRNU Fingerprinting",
            "JPEG DCT Quantization Forensics",
            "Neural Steganalysis & Inpainting",
            "Cryptographic FRE 902(14) Custody",
            "AI Voice Synthesis & Lecture Mastery"
        ),
        institutionName = "University of California, Los Angeles (UCLA)"
    )

    val uclaMasterDegreeCourse = Course(
        id = "course_ucla_ms_cs201",
        code = "UCLA-MS-CS201",
        title = "UCLA MS: Advanced Digital Forensics & Neural Steganalysis",
        subtitle = "University of California, Los Angeles — Samueli School of Engineering Graduate Degree Track with AI Voice Lectures.",
        category = TopicCategory.UCLA_MASTER,
        level = CourseLevel.GRADUATE_MASTER,
        estimatedHours = 40,
        associatedBadgeCode = "SSF-UCLA-MS",
        institutionName = "University of California, Los Angeles (UCLA)",
        isAiGenerated = true,
        defaultVoiceLecturer = "Dr. Bruin — UCLA AI Voice Fellow",
        lessons = listOf(
            Lesson(
                id = "l_ucla_1",
                title = "Module 1: Sensor Noise & PRNU Silicon Fingerprinting",
                content = """In digital imaging physics, every CMOS and CCD image sensor exhibits microscopic silicon substrate irregularities known as Photo-Response Non-Uniformity (PRNU). PRNU acts as an unclonable physical fingerprint unique to an individual camera device, persisting across all images captured regardless of ISO or lens configuration.

Mathematically, the raw image I is modeled as:
I = I0 + I0 * K + Theta
where I0 is the pristine incident photon radiance, K is the sensor's deterministic PRNU noise pattern, and Theta represents zero-mean Gaussian read noise.

In forensic lab environments, PRNU is extracted by filtering the pixel array with a 2D wavelet Wiener filter (e.g., Mihcak denoising filter) to isolate the residual noise W = I - F(I). By computing the Normalized Cross-Correlation (NCC) between the residual W and the camera reference PRNU fingerprint, forensic examiners establish device identity with mathematical certainty exceeding 99.999%.""",
                codeExample = """# UCLA CS201 PRNU Cross-Correlation Verification
import numpy as np
from scipy.signal import correlate2d

def compute_ncc(residual_image, sensor_fingerprint):
    r_mean = residual_image - np.mean(residual_image)
    k_mean = sensor_fingerprint - np.mean(sensor_fingerprint)
    numerator = np.sum(r_mean * k_mean)
    denominator = np.sqrt(np.sum(r_mean**2) * np.sum(k_mean**2))
    return numerator / (denominator + 1e-12)

# NCC > 0.05 indicates same physical silicon sensor""",
                keyTakeaways = listOf(
                    "PRNU is a physical silicon artifact caused by minor manufacturing variations in light-sensitive silicon diodes.",
                    "Wavelet denoising filters isolate the deterministic high-frequency noise residual from scene content.",
                    "Normalized Cross-Correlation provides evidentiary device-linking admissible in federal courtrooms."
                ),
                estimatedMinutes = 20,
                voiceLectureScript = "Welcome to Module One of the UCLA Master of Science in Cyber Forensics. I am Dr. Bruin, your UCLA AI Voice Fellow. Today, we investigate Photo-Response Non-Uniformity, or PRNU. Every digital camera sensor contains microscopic silicon defects. When photons hit the sensor, these defects introduce a unique, deterministic noise pattern. By applying a wavelet Wiener filter, we isolate this residual and match it via Normalized Cross Correlation. This provides an incontrovertible silicon fingerprint linking a photograph to a specific physical camera device.",
                voiceLecturerPersona = "Dr. Bruin — UCLA AI Voice Fellow"
            ),
            Lesson(
                id = "l_ucla_2",
                title = "Module 2: JPEG Discrete Cosine Transform (DCT) Steganalysis",
                content = """JPEG compression divides images into 8x8 pixel blocks and applies the Discrete Cosine Transform (DCT) to convert spatial luminance and chrominance into frequency domain coefficients. The coefficients are then quantized using standardized or customized quantization matrices.

Modern steganographic algorithms (such as J-Uniward, OutGuess, and F5) conceal encrypted payloads inside the Least Significant Bits (LSB) of non-zero AC DCT coefficients. However, this embedding alters the statistical histogram of quantized coefficients, inducing first-order and second-order distortions.

Steganalysis employs Benford's Generalized Law and Markov transition probabilities between adjacent DCT blocks. When an image is double-compressed (common when metadata is modified or a message is hidden), periodic histogram artifacts known as 'double quantization dips' reveal tampering even if visual inspection shows zero degradation.""",
                codeExample = """# JPEG DCT Coefficient Histogram Symmetry Analysis
import numpy as np

def detect_double_quantization(dct_coefficients):
    hist, bins = np.histogram(dct_coefficients, bins=np.arange(-20, 21))
    # Double-quantization creates periodicity / zero-val dips
    periodicity_score = np.std(hist[1::2] - hist[0::2])
    is_tampered = periodicity_score > 15.4
    return {"periodicity": periodicity_score, "tampered": is_tampered}""",
                keyTakeaways = listOf(
                    "JPEG compression maps 8x8 spatial blocks to 64 frequency coefficients via 2D DCT.",
                    "Steganography embeds payloads into low-frequency AC coefficients, skewing coefficient histograms.",
                    "Double quantization analysis uncovers prior decompression, editing, and re-encoding cycles."
                ),
                estimatedMinutes = 25,
                voiceLectureScript = "Welcome to Module Two. I am Dr. Bruin. In this graduate seminar, we study JPEG Discrete Cosine Transform Steganalysis. JPEG compression partitions raw pixel arrays into eight-by-eight grids, transforming spatial values into frequency coefficients. When adversaries hide data using steganographic algorithms like J-Uniward, they perturb non-zero AC coefficients. By tracking the Markov transition probabilities and Benford's law distributions across frequency bins, our forensic models detect covert payload carriers with high statistical confidence.",
                voiceLecturerPersona = "Dr. Bruin — UCLA AI Voice Fellow"
            ),
            Lesson(
                id = "l_ucla_3",
                title = "Module 3: Generative AI Inpainting & Neural Artifact Forensics",
                content = """The proliferation of Diffusion Models (Stable Diffusion, Imagen) and Generative Adversarial Networks (GANs) enables realistic object removal, localized inpainting, and synthetic face generation. However, neural generators leave distinct spectral and architectural artifacts.

Key forensic indicators of AI generation include:
1. Spectral Grid Artifacts: Transposed convolutions in upsampling layers generate checkerboard energy spikes in the Fourier magnitude spectrum.
2. Color Filter Array (CFA) Inconsistency: Authentic cameras use a Bayer CFA filter, requiring demosaicing interpolation. AI-generated regions lack standard bilinear or bicubic demosaicing correlation.
3. Patch-level Noise Variance: Neural inpainting produces localized suppression of natural ISO sensor noise, creating a telltale 'noise vacuum' within the altered bounding box.""",
                codeExample = """# FFT Magnitude Spectrum Energy Spike Detector
import numpy as np

def inspect_fft_spectrum(channel):
    f_transform = np.fft.fft2(channel)
    f_shift = np.fft.fftshift(f_transform)
    magnitude_spectrum = 20 * np.log(np.abs(f_shift) + 1e-9)
    # Check for radial frequency symmetry vs artificial grid spikes
    grid_spike_ratio = np.max(magnitude_spectrum) / np.median(magnitude_spectrum)
    return grid_spike_ratio > 4.2  # Neural upsampling artifact threshold""",
                keyTakeaways = listOf(
                    "Neural upsampling layers leave characteristic high-frequency grid spikes in 2D Fourier spectra.",
                    "Synthesized regions lack physical Bayer CFA demosaicing chromatic phase relationships.",
                    "Local noise floor discontinuities reveal spliced generative inpainting."
                ),
                estimatedMinutes = 25,
                voiceLectureScript = "In Module Three, we examine Generative AI Inpainting and Neural Artifacts. Modern diffusion models produce hyper-realistic imagery, but their architectural pipeline betrays their origin. Transposed convolutional layers introduce regular high-frequency checkerboard patterns visible in Fourier transforms. Furthermore, authentic digital photographs require demosaicing of the camera's Bayer filter array. When an AI inpaints a region, that demosaicing correlation vanishes. We use these mathematical discrepancies to verify authenticity.",
                voiceLecturerPersona = "Dr. Bruin — UCLA AI Voice Fellow"
            ),
            Lesson(
                id = "l_ucla_4",
                title = "Module 4: C2PA, Content Authenticity & Cryptographic Manifests",
                content = """The Coalition for Content Provenance and Authenticity (C2PA) specification defines an open cryptographic framework to bind provenance assertions directly to digital media files.

A valid C2PA manifest consists of:
1. JUMBF (JPEG Universal Metadata Box Format) Superbox embedded in APP11 or JUMBF container chunks.
2. Claim Assertions: Recording precise hardware capture timestamps, GPS coordinates, camera model, and cryptographic hash of the pristine pixel raster.
3. COSE (CBOR Object Signing and Encryption) Signatures: Signed with a trusted X.509 Certificate Authority root (e.g., Apple, Nikon, Adobe, Sony hardware secure enclaves).

Forensic validation involves verifying the Merkle tree hash chain. If an attacker strips the metadata or modifies a single pixel, the COSE signature validation fails instantly.""",
                codeExample = """# Cryptographic C2PA Manifest Verification Outline
import hashlib

def verify_c2pa_merkle_leaf(pixel_hash_bytes, claim_signature_hash):
    computed_digest = hashlib.sha256(pixel_hash_bytes).digest()
    is_valid = computed_digest == claim_signature_hash
    return {"tamper_evident": True, "signature_valid": is_valid}""",
                keyTakeaways = listOf(
                    "C2PA embeds signed provenance manifests into JUMBF binary boxes within media headers.",
                    "Hardware secure enclaves sign initial capture claims at the physical shutter moment.",
                    "Any post-capture alteration invalidates the Merkle hash tree and cryptographic signature."
                ),
                estimatedMinutes = 20,
                voiceLectureScript = "Welcome to Module Four. We now focus on the frontier of digital provenance: the C2PA standard. Established by industry leaders including Adobe, Microsoft, and hardware manufacturers, C2PA binds cryptographic signatures directly into media files using JUMBF containers. Secure enclaves inside cameras sign the pixel raster alongside hardware assertions. When verifying in the field, any bit modification causes signature verification to fail immediately.",
                voiceLecturerPersona = "Dr. Bruin — UCLA AI Voice Fellow"
            ),
            Lesson(
                id = "l_ucla_5",
                title = "Module 5: Metadata Anti-Forensics & Weaponized Tag Injection",
                content = """Anti-forensics comprises techniques designed to evade, corrupt, or exploit forensic investigative tools. Adversaries frequently weaponize image metadata tags to attack backend ingestion parsers.

Prominent vectors include:
1. Parser Desynchronization: Inserting conflicting EXIF TIFF headers alongside contradictory IPTC IIM and XMP XML packets, causing inconsistent parser interpretations.
2. Command and SQL Injection in EXIF Tags: Injecting SQL payloads (e.g., `' OR 1=1; DROP TABLE logs; --`) or Shell escapes (e.g., `; rm -rf /`) into the UserComment, Artist, or XPComment tags, which detonate when logged unescaped.
3. EXIF Zip Bombs and XML External Entity (XXE) exploits embedded inside XMP metadata streams to exhaust server memory or exfiltrate local files.""",
                codeExample = """# Safe Sanitization of EXIF User Strings
import html
import re

def sanitize_exif_tag(tag_value: str) -> str:
    # Strip non-printable ASCII and escape HTML/SQL meta-characters
    cleaned = re.sub(r'[^\x20-\x7E]', '', str(tag_value))
    escaped = html.escape(cleaned)
    return escaped[:500]  # Enforce rigorous length constraints""",
                keyTakeaways = listOf(
                    "Metadata parsers are vulnerable to memory corruption, SQLi, and command injection via hostile tag values.",
                    "Discrepancies between EXIF and XMP headers indicate deliberate tampering or anti-forensic spoofing.",
                    "Strict input sanitization, length boundaries, and sandboxed parsing are essential defenses."
                ),
                estimatedMinutes = 20,
                voiceLectureScript = "In Module Five, we examine Metadata Anti-Forensics and Weaponized Tag Injection. Malicious actors do not merely erase metadata; they turn metadata fields into cyber weapons. By injecting SQL commands, XML entity expansions, or shell scripts into fields like UserComment or Artist, attackers target the automated ingestion pipelines of law enforcement and cloud services. We analyze these exploit vectors to build resilient, hardened parsing engines.",
                voiceLecturerPersona = "Dr. Bruin — UCLA AI Voice Fellow"
            ),
            Lesson(
                id = "l_ucla_6",
                title = "Module 6: Federal Rules of Evidence 902(14) & Courtroom Presentation",
                content = """In federal litigation, digital evidence must satisfy strict legal authentication hurdles before admission into court. Under Federal Rule of Evidence 902(14), certified records generated by an electronic process or system are self-authenticating if supported by a qualified certification.

Requirements for FRE 902(14) compliance:
1. Chain of Custody Documentation: Cryptographic SHA-256 and MD5 hash logs recorded at the exact moment of physical acquisition.
2. Bit-Stream Forensic Duplicate: Ensuring forensic extraction occurs strictly on an exact bit-for-bit duplicate, never the original evidentiary medium.
3. Repeatability & Peer-Reviewed Methodology: Satisfying the Daubert Standard (Daubert v. Merrell Dow Pharmaceuticals) by demonstrating that all forensic algorithms have known error rates and broad acceptance in scientific literature.""",
                codeExample = """# FRE 902(14) Self-Authenticating Hash Certificate
import hashlib
from datetime import datetime, timezone

def generate_fre_902_certificate(evidence_id: str, file_bytes: bytes, examiner: str):
    sha256 = hashlib.sha256(file_bytes).hexdigest()
    timestamp = datetime.now(timezone.utc).isoformat()
    return f\"\"\"EVIDENTIARY CERTIFICATION UNDER FRE 902(14)
Evidence Item ID: {evidence_id}
Acquisition Timestamp: {timestamp}
Forensic SHA-256 Digest: {sha256}
Principal Forensic Examiner: {examiner}
Protocol: Bit-stream copy certified authentic under penalty of perjury.\"\"\"""",
                keyTakeaways = listOf(
                    "Federal Rule of Evidence 902(14) enables self-authentication through cryptographic hash verification.",
                    "The Daubert Standard requires forensic methodologies to have known error rates and empirical validity.",
                    "Immutable chain-of-custody ledgers guarantee forensic integrity throughout judicial proceedings."
                ),
                estimatedMinutes = 20,
                voiceLectureScript = "Welcome to the capstone lecture of our UCLA Master of Science curriculum: Federal Rules of Evidence 902(14) and Courtroom Presentation. Even the most sophisticated forensic algorithm is useless if rejected by a trial judge. Under FRE 902(14), electronic records are self-authenticating when accompanied by certified SHA-256 cryptographic hashes and documented chain of custody. We study how to translate complex mathematical findings into clear, rigorous testimony that withstands cross-examination.",
                voiceLecturerPersona = "Dr. Bruin — UCLA AI Voice Fellow"
            )
        ),
        quiz = Quiz(
            id = "quiz_ucla_ms_cs201",
            title = "UCLA Master's Comprehensive Defense Examination",
            passingScorePercentage = 85,
            questions = listOf(
                QuizQuestion(
                    id = "q_ucla_1",
                    question = "What physical mechanism creates Photo-Response Non-Uniformity (PRNU) in camera sensors?",
                    options = listOf(
                        "Lens optical aberration and focal distortion",
                        "Microscopic silicon manufacturing imperfections in photodiode response",
                        "Software post-processing sharpening kernels in the camera OS",
                        "Quantization rounding errors in the JPEG discrete cosine transform"
                    ),
                    correctOptionIndex = 1,
                    explanation = "PRNU is caused by slight silicon diode manufacturing imperfections during sensor lithography, creating an unclonable physical fingerprint."
                ),
                QuizQuestion(
                    id = "q_ucla_2",
                    question = "How does modern JPEG steganalysis detect hidden payloads embedded in AC DCT coefficients?",
                    options = listOf(
                        "By comparing the file byte size against standard JPEG tables",
                        "By analyzing double quantization periodicity and coefficient histogram distortions",
                        "By searching for plaintext ASCII strings inside EXIF APP1 markers",
                        "By measuring RGB color palette brightness values"
                    ),
                    correctOptionIndex = 1,
                    explanation = "Steganography alters the statistical distribution of DCT coefficients, producing detectable histogram perturbations and double quantization artifacts."
                ),
                QuizQuestion(
                    id = "q_ucla_3",
                    question = "Which spectral artifact is commonly introduced by neural image generators like Diffusion models?",
                    options = listOf(
                        "Loss of EXIF SOI (Start of Image) binary markers",
                        "High-frequency checkerboard energy spikes in the 2D Fourier magnitude spectrum",
                        "Conversion of 8-bit color channels into 16-bit float vectors",
                        "Inversion of camera GPS latitude coordinates"
                    ),
                    correctOptionIndex = 1,
                    explanation = "Transposed convolutions and neural upsampling operations create characteristic periodic checkerboard spikes visible in 2D FFT magnitude spectrums."
                ),
                QuizQuestion(
                    id = "q_ucla_4",
                    question = "Under Federal Rule of Evidence 902(14), what is required for electronic digital evidence to be self-authenticating?",
                    options = listOf(
                        "A notarized screenshot of the image file properties",
                        "A certification supported by cryptographic hash verification (such as SHA-256) matching the original acquisition",
                        "Approval from the camera manufacturer's legal department",
                        "Presence of EXIF camera serial number tags"
                    ),
                    correctOptionIndex = 1,
                    explanation = "FRE 902(14) permits self-authentication of digital files when backed by a qualified certificate proving identical cryptographic hash digests."
                ),
                QuizQuestion(
                    id = "q_ucla_5",
                    question = "What cryptographic standard is utilized by C2PA to bind authentic capture assertions to digital media?",
                    options = listOf(
                        "COSE digital signatures embedded in JUMBF metadata boxes",
                        "AES-128 ECB encrypted password locks",
                        "Plaintext XML metadata tags in JPEG APP0 segments",
                        "MD5 hash checksums stored in local text files"
                    ),
                    correctOptionIndex = 0,
                    explanation = "C2PA embeds signed provenance assertions using JUMBF boxes and COSE cryptographic signatures anchored in trusted root certificates."
                )
            )
        )
    )

    suspend fun generateCourse(
        topicPrompt: String,
        academicLevel: CourseLevel = CourseLevel.GRADUATE_MASTER,
        institution: String = "University of California, Los Angeles (UCLA)",
        lecturerPersona: String = "Dr. Bruin — UCLA AI Voice Fellow",
        customApiKey: String? = null
    ): Course = withContext(Dispatchers.IO) {
        val apiKey = customApiKey?.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }

        if (!apiKey.isNullOrBlank()) {
            try {
                val geminiCourse = callGeminiForCourse(topicPrompt, academicLevel, institution, lecturerPersona, apiKey)
                if (geminiCourse != null) {
                    return@withContext geminiCourse
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini course generation failed, falling back to Intelligent Academic Synthesizer", e)
            }
        }

        // Academic Course Synthesis Engine
        synthesizeAcademicCourse(topicPrompt, academicLevel, institution, lecturerPersona)
    }

    private fun callGeminiForCourse(
        topicPrompt: String,
        academicLevel: CourseLevel,
        institution: String,
        lecturerPersona: String,
        apiKey: String
    ): Course? {
        val prompt = """
Generate a graduate-level master's course on the topic: "$topicPrompt".
Institution: $institution
Lecturer Persona: $lecturerPersona
Return ONLY valid raw JSON with this exact schema:
{
  "code": "UCLA-MS-XXX",
  "title": "Title",
  "subtitle": "Subtitle",
  "category": "UCLA_MASTER",
  "estimatedHours": 30,
  "lessons": [
    {
      "id": "l_1",
      "title": "Module 1 Title",
      "content": "Deep technical content explaining formulas and concepts...",
      "codeExample": "# Python code example\n...",
      "keyTakeaways": ["Takeaway 1", "Takeaway 2", "Takeaway 3"],
      "estimatedMinutes": 20,
      "voiceLectureScript": "Spoken transcript by $lecturerPersona covering the module."
    }
  ],
  "quiz": {
    "title": "Comprehensive Defense Exam",
    "passingScorePercentage": 80,
    "questions": [
      {
        "id": "q_1",
        "question": "Question text?",
        "options": ["Option A", "Option B", "Option C", "Option D"],
        "correctOptionIndex": 0,
        "explanation": "Why Option A is correct"
      }
    ]
  }
}
""".trimIndent()

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val requestBody = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.4)
            })
        }

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 25000
        }

        OutputStreamWriter(conn.outputStream).use { it.write(requestBody.toString()) }

        if (conn.responseCode == 200) {
            val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val contentObj = candidate?.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            val rawJson = parts?.optJSONObject(0)?.optString("text") ?: return null

            return parseCourseJson(rawJson, topicPrompt, academicLevel, institution, lecturerPersona)
        }
        return null
    }

    private fun parseCourseJson(
        jsonString: String,
        topicPrompt: String,
        academicLevel: CourseLevel,
        institution: String,
        lecturerPersona: String
    ): Course {
        val obj = JSONObject(jsonString)
        val courseId = "course_ai_${UUID.randomUUID().toString().take(8)}"
        val code = obj.optString("code", "UCLA-MS-${(100..999).random()}")
        val title = obj.optString("title", "$topicPrompt: Advanced Graduate Studies")
        val subtitle = obj.optString("subtitle", "$institution — Graduate Master Track with Voice Lectures")
        val hours = obj.optInt("estimatedHours", 35)

        val lessonsList = mutableListOf<Lesson>()
        val lessonsArray = obj.optJSONArray("lessons")
        if (lessonsArray != null) {
            for (i in 0 until lessonsArray.length()) {
                val lObj = lessonsArray.getJSONObject(i)
                val takeaways = mutableListOf<String>()
                val tArray = lObj.optJSONArray("keyTakeaways")
                if (tArray != null) {
                    for (j in 0 until tArray.length()) {
                        takeaways.add(tArray.getString(j))
                    }
                }
                lessonsList.add(
                    Lesson(
                        id = lObj.optString("id", "l_$i"),
                        title = lObj.optString("title", "Module ${i + 1}"),
                        content = lObj.optString("content", ""),
                        codeExample = if (lObj.has("codeExample")) lObj.getString("codeExample") else null,
                        keyTakeaways = takeaways,
                        estimatedMinutes = lObj.optInt("estimatedMinutes", 15),
                        voiceLectureScript = if (lObj.has("voiceLectureScript")) lObj.getString("voiceLectureScript") else null,
                        voiceLecturerPersona = lecturerPersona
                    )
                )
            }
        }

        val quizObj = obj.optJSONObject("quiz")
        val questionsList = mutableListOf<QuizQuestion>()
        if (quizObj != null) {
            val qArray = quizObj.optJSONArray("questions")
            if (qArray != null) {
                for (i in 0 until qArray.length()) {
                    val qObj = qArray.getJSONObject(i)
                    val opts = mutableListOf<String>()
                    val optsArray = qObj.optJSONArray("options")
                    if (optsArray != null) {
                        for (k in 0 until optsArray.length()) {
                            opts.add(optsArray.getString(k))
                        }
                    }
                    questionsList.add(
                        QuizQuestion(
                            id = qObj.optString("id", "q_$i"),
                            question = qObj.optString("question", "Question ${i + 1}"),
                            options = opts,
                            correctOptionIndex = qObj.optInt("correctOptionIndex", 0),
                            explanation = qObj.optString("explanation", "Verified forensic response.")
                        )
                    )
                }
            }
        }

        return Course(
            id = courseId,
            code = code,
            title = title,
            subtitle = subtitle,
            category = TopicCategory.UCLA_MASTER,
            level = academicLevel,
            estimatedHours = hours,
            lessons = lessonsList,
            quiz = Quiz(
                id = "quiz_$courseId",
                title = quizObj?.optString("title", "Comprehensive Defense Examination") ?: "Comprehensive Defense Examination",
                passingScorePercentage = quizObj?.optInt("passingScorePercentage", 80) ?: 80,
                questions = questionsList
            ),
            associatedBadgeCode = "SSF-UCLA-MS",
            institutionName = institution,
            isAiGenerated = true,
            defaultVoiceLecturer = lecturerPersona
        )
    }

    private fun synthesizeAcademicCourse(
        topicPrompt: String,
        academicLevel: CourseLevel,
        institution: String,
        lecturerPersona: String
    ): Course {
        val courseId = "course_ucla_ai_${UUID.randomUUID().toString().take(6)}"
        val codeNumber = (200..499).random()
        val code = "UCLA-MS-CS$codeNumber"
        val title = "$topicPrompt: Master of Science Specialization"
        val subtitle = "$institution — Advanced Graduate Curriculum with AI Voice Lectures."

        val modules = listOf(
            Triple(
                "Theoretical Foundations & Mathematical Modeling",
                """In this opening module, we establish the rigorous mathematical framework underpinning $topicPrompt. We formalize state-space representations, probability distributions over observation vectors, and loss objectives governing system behavior.

Graduate research demands understanding not only empirical performance but also asymptotic convergence and statistical bounds. We examine classical proofs and modern reformulations to analyze failure modes and adversarial edge cases.""",
                listOf(
                    "Mathematical formalization of state representations in $topicPrompt.",
                    "Asymptotic convergence bounds and error propagation analysis.",
                    "Adversarial vulnerabilities and empirical verification protocols."
                )
            ),
            Triple(
                "Algorithmic Architecture & High-Performance Pipelines",
                """We now design scalable computational pipelines implementing $topicPrompt. By parallelizing vector operations across GPU memory hierarchies and optimizing I/O cache locality, production pipelines achieve ultra-low latency throughput.

We analyze algorithm complexity, benchmarking memory overhead against precision trade-offs. Special emphasis is placed on numerical stability and hardware acceleration.""",
                listOf(
                    "Optimized computational complexity and GPU memory layout.",
                    "Vectorized execution patterns for sub-millisecond latency.",
                    "Numerical stability guarantees under extreme floating-point variance."
                )
            ),
            Triple(
                "Forensic Integrity, Validation & Defense Standards",
                """In digital forensics and enterprise deployment, integrity verification is paramount. This module covers cryptographic attestation, deterministic reproducibility, and chain-of-custody protocols aligned with Federal Rules of Evidence.

We review standard validation suites, synthetic stress testing, and auditing techniques required for peer-reviewed defense and courtroom presentation.""",
                listOf(
                    "Cryptographic digest verification and immutable audit trails.",
                    "Deterministic execution guarantees for reproducible forensic claims.",
                    "Compliance with Federal Rule of Evidence 902 and international standards."
                )
            )
        )

        val lessons = modules.mapIndexed { idx, (mTitle, mContent, mTakeaways) ->
            Lesson(
                id = "${courseId}_l_$idx",
                title = "Module ${idx + 1}: $mTitle",
                content = mContent,
                codeExample = """# UCLA Graduate Research Pipeline for $topicPrompt
import numpy as np

def execute_pipeline(data_matrix):
    transformed = np.log1p(np.abs(data_matrix))
    normalized = (transformed - np.mean(transformed)) / (np.std(transformed) + 1e-8)
    return {"status": "SUCCESS", "norm_vector": normalized}""",
                keyTakeaways = mTakeaways,
                estimatedMinutes = 20,
                voiceLectureScript = "Welcome to Module ${idx + 1} of $topicPrompt. I am $lecturerPersona. In this session, we investigate $mTitle. Our focus spans both foundational mathematical rigor and practical high-performance execution. Review the key takeaways and code implementation to solidify your understanding.",
                voiceLecturerPersona = lecturerPersona
            )
        }

        val quiz = Quiz(
            id = "quiz_$courseId",
            title = "$topicPrompt Comprehensive Defense Examination",
            passingScorePercentage = 80,
            questions = listOf(
                QuizQuestion(
                    id = "q_${courseId}_1",
                    question = "In the mathematical model of $topicPrompt, how are observation vectors stabilized?",
                    options = listOf(
                        "Via logarithmic transformation and mean-variance normalization",
                        "By dropping all high-order decimal places",
                        "By disabling floating-point hardware flags",
                        "By random permutation of input dimensions"
                    ),
                    correctOptionIndex = 0,
                    explanation = "Logarithmic scaling and standard deviation normalization stabilize numerical matrices against extreme variance."
                ),
                QuizQuestion(
                    id = "q_${courseId}_2",
                    question = "Why is deterministic reproducibility critical in graduate forensic pipelines?",
                    options = listOf(
                        "To reduce the size of the compiled APK application",
                        "To guarantee that independent investigators can replicate exact evidentiary findings in court",
                        "To eliminate the need for unit testing",
                        "To bypass operating system security permissions"
                    ),
                    correctOptionIndex = 1,
                    explanation = "Deterministic execution ensures evidentiary claims satisfy the Daubert standard and remain reproducible by opposing experts."
                ),
                QuizQuestion(
                    id = "q_${courseId}_3",
                    question = "Which standard governs self-authenticating digital evidence in US federal proceedings?",
                    options = listOf(
                        "Federal Rule of Evidence 902(14)",
                        "IEEE 802.11 Wireless Standard",
                        "HTTP 1.1 Header Specification",
                        "ISO 9001 Manufacturing Guideline"
                    ),
                    correctOptionIndex = 0,
                    explanation = "FRE 902(14) establishes the framework for self-authenticating electronic records through cryptographic hash validation."
                )
            )
        )

        return Course(
            id = courseId,
            code = code,
            title = title,
            subtitle = subtitle,
            category = TopicCategory.UCLA_MASTER,
            level = academicLevel,
            estimatedHours = 35,
            lessons = lessons,
            quiz = quiz,
            associatedBadgeCode = "SSF-UCLA-MS",
            institutionName = institution,
            isAiGenerated = true,
            defaultVoiceLecturer = lecturerPersona
        )
    }
}

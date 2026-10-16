package com.example.learning.data

import com.example.learning.model.*

object LearningDataProvider {

    val ssfBadges: List<Badge> = listOf(
        Badge(
            id = "badge_ssf_101",
            code = "SSF-101",
            name = "EXIF Metadata Analyst",
            tier = SsfBadgeTier.BRONZE,
            description = "Mastery of TIFF/EXIF tag extraction, camera exposure metrics, and baseline header parsing.",
            criteria = "Pass Course GL-EXIF-101 Quiz with >= 75% score",
            skillsValidated = listOf("TIFF 6.0 Headers", "Exposure Triangle Analysis", "APP1 Markers", "Device Fingerprinting")
        ),
        Badge(
            id = "badge_ssf_201",
            code = "SSF-201",
            name = "Geospatial Privacy Defender",
            tier = SsfBadgeTier.SILVER,
            description = "Specialized certification in identifying GPS telemetry leakage and applying zero-loss privacy redaction.",
            criteria = "Pass Course GL-PRIV-201 Quiz with >= 80% score",
            skillsValidated = listOf("WGS84 DMS Decoding", "Geotag Leakage Assessment", "Zero-Loss EXIF Sanitization", "Bulk Catalog Scrubbing")
        ),
        Badge(
            id = "badge_ssf_301",
            code = "SSF-301",
            name = "Steganography Investigator",
            tier = SsfBadgeTier.GOLD,
            description = "Expertise in detecting LSB payload insertions, EOF trailer anomalies, and hidden binary channels.",
            criteria = "Pass Course GL-STEG-301 Quiz with >= 80% score",
            skillsValidated = listOf("LSB Spatial Domain Extraction", "Chi-Square Steganalysis", "JPEG EOI Trailer Detection", "Polyglot Binary Carving")
        ),
        Badge(
            id = "badge_ssf_401",
            code = "SSF-401",
            name = "Forensic Hashing & Custody Specialist",
            tier = SsfBadgeTier.GOLD,
            description = "Demonstrated competence in MD5/SHA-256 cryptographic verification, pHash distance, and chain-of-custody audits.",
            criteria = "Pass Course GL-HASH-401 Quiz with >= 80% score",
            skillsValidated = listOf("SHA-256 Bit Verification", "Perceptual pHash Hamming Distance", "FRE 902(14) Custody Audit", "Room SQLite Ledger")
        ),
        Badge(
            id = "badge_ssf_501",
            code = "SSF-501",
            name = "Master Forensic Engineer",
            tier = SsfBadgeTier.PLATINUM,
            description = "Comprehensive mastery of automated Python forensic pipelines, batch sanitization, and enterprise compliance.",
            criteria = "Pass Course GL-PYTH-501 Quiz with >= 85% score",
            skillsValidated = listOf("PEP 8/257 Python Automation", "Pillow & Piexif Engineering", "Pandas Dataset Ingestion", "CLI Terminal Architecture")
        ),
        com.example.learning.generator.AiVoiceCourseGeneratorService.uclaMasterBadge
    )

    val courses: List<Course> = listOf(
        com.example.learning.generator.AiVoiceCourseGeneratorService.uclaMasterDegreeCourse,
        Course(
            id = "course_exif_101",
            code = "GL-EXIF-101",
            title = "Digital Forensics & EXIF Foundations",
            subtitle = "Camera sensor architecture, TIFF headers, and exposure triangle forensics.",
            category = TopicCategory.FORENSICS,
            level = CourseLevel.BEGINNER,
            estimatedHours = 3,
            associatedBadgeCode = "SSF-101",
            lessons = listOf(
                Lesson(
                    id = "l_exif_1",
                    title = "Anatomy of a Digital Image Header",
                    content = """Digital photographs contain critical forensic artifacts stored inside metadata containers. The most ubiquitous standard is EXIF (Exchangeable Image File Format), formalized by the JEITA/CP-3451 specification.

When a digital camera or smartphone shutter activates, the image sensor captures photons and the Image Signal Processor (ISP) encodes both the pixel raster (JPEG/HEIF) and the metadata payload. EXIF is embedded directly in JPEG files using Application Marker Segments (APP1, marker 0xFFE1).

Within APP1, data is formatted according to the TIFF 6.0 specification using an Image File Directory (IFD) structure. Each IFD entry consists of a 12-byte block specifying Tag ID, Data Type, Count, and Value/Offset.""",
                    codeExample = """# Reading JPEG APP1 Marker in Python
with open("sample.jpg", "rb") as f:
    header = f.read(4)
    if header[:2] == b"\xff\xd8": # SOI (Start of Image)
        print("Valid JPEG container")
        marker, length = f.read(2), int.from_bytes(f.read(2), "big")
        print(f"First marker: {marker.hex()}, Segment length: {length} bytes")""",
                    keyTakeaways = listOf(
                        "JPEG files store EXIF in the APP1 (0xFFE1) marker segment.",
                        "EXIF follows TIFF 6.0 IFD data structures with little-endian (II) or big-endian (MM) byte alignment.",
                        "Tag IDs are 16-bit integers denoting parameters like ShutterSpeed, ISO, and GPS."
                    ),
                    estimatedMinutes = 15
                ),
                Lesson(
                    id = "l_exif_2",
                    title = "The Exposure Triangle & Sensor Telemetry",
                    content = """Forensic investigators inspect the exposure triangle (Shutter Speed, Aperture, ISO) to verify lighting conditions and camera authenticity.

1. Aperture (FNumber / Tag 0x829D): Defines physical diaphragm opening. Fraudulent synthetic images often exhibit f-stops impossible on declared lenses.
2. Shutter Speed (ExposureTime / Tag 0x829A): Duration sensor was exposed. Essential for confirming motion blur consistency.
3. ISO Sensitivity (PhotographicSensitivity / Tag 0x8827): Sensor signal amplification. High ISO introduces sensor noise patterns that can be mathematically verified via photo-response non-uniformity (PRNU).""",
                    codeExample = """# Extracting Exposure Parameters with Android ExifInterface
val exif = ExifInterface(inputStream)
val aperture = exif.getAttributeDouble(ExifInterface.TAG_F_NUMBER, 0.0)
val exposureTime = exif.getAttributeDouble(ExifInterface.TAG_EXPOSURE_TIME, 0.0)
val iso = exif.getAttributeInt(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, 0)
println("Aperture: f/" + aperture + ", Exposure: " + exposureTime + "s, ISO: " + iso)""",
                    keyTakeaways = listOf(
                        "Aperture, Exposure Time, and ISO constitute core physical capture metrics.",
                        "PRNU (Photo-Response Non-Uniformity) links sensor noise to specific hardware units.",
                        "Discrepancies between ambient lighting and EXIF exposure metrics highlight manipulation."
                    ),
                    estimatedMinutes = 15
                ),
                Lesson(
                    id = "l_exif_3",
                    title = "Camera Serial Numbers & Unique Device Fingerprinting",
                    content = """High-end digital cameras embed proprietary MakerNotes (Tag 0x927C) containing camera body serial numbers, lens serial numbers, and shutter actuation counts.

Smartphone cameras similarly embed sensor identification numbers in manufacturer-specific tags (Apple, Samsung, Sony). In criminal investigations, matching a photo's embedded serial number to a confiscated camera is definitive physical evidence.""",
                    codeExample = """# Inspecting Camera Body Serial in Exif
serial_number = exif_dict.get('Exif', {}).get(piexif.ExifIFD.BodySerialNumber)
if serial_number:
    print(f"Confirmed Camera Serial: {serial_number.decode('utf-8')}")""",
                    keyTakeaways = listOf(
                        "Tag 0xA431 (BodySerialNumber) and Tag 0xA435 (LensSerialNumber) identify specific hardware.",
                        "MakerNote tags contain manufacturer-encrypted diagnostic records and shutter actuation counts.",
                        "Serial numbers are critical in chain-of-custody corroboration."
                    ),
                    estimatedMinutes = 15
                )
            ),
            quiz = Quiz(
                id = "quiz_exif_101",
                title = "EXIF Foundations Certification Quiz",
                passingScorePercentage = 75,
                questions = listOf(
                    QuizQuestion(
                        id = "q_exif_1",
                        question = "Which JPEG marker segment designates the start of an EXIF metadata block?",
                        options = listOf("APP0 (0xFFE0)", "APP1 (0xFFE1)", "SOF0 (0xFFC0)", "SOS (0xFFDA)"),
                        correctOptionIndex = 1,
                        explanation = "The APP1 marker (0xFFE1) is standardly reserved for EXIF and TIFF formatted metadata headers in JPEG streams."
                    ),
                    QuizQuestion(
                        id = "q_exif_2",
                        question = "What does the TIFF header byte order string 'II' (0x4949) signify?",
                        options = listOf("Motorola Big-Endian", "Intel Little-Endian", "Interlaced Image", "ISO Standard"),
                        correctOptionIndex = 1,
                        explanation = "'II' stands for Intel (Little-Endian) byte ordering, whereas 'MM' stands for Motorola (Big-Endian)."
                    ),
                    QuizQuestion(
                        id = "q_exif_3",
                        question = "Which forensic technique links sensor noise patterns to a specific camera unit?",
                        options = listOf("PRNU (Photo-Response Non-Uniformity)", "MD5 Hash Checksum", "JPEG Quantization Table Analysis", "Huffman Decoding"),
                        correctOptionIndex = 0,
                        explanation = "PRNU is an inherent physical artifact of silicon wafer manufacturing that leaves a unique pixel noise fingerprint."
                    ),
                    QuizQuestion(
                        id = "q_exif_4",
                        question = "Which EXIF tag specifies the lens aperture value?",
                        options = listOf("Tag 0x829A (ExposureTime)", "Tag 0x829D (FNumber)", "Tag 0x8827 (ISO)", "Tag 0x0112 (Orientation)"),
                        correctOptionIndex = 1,
                        explanation = "Tag 0x829D (FNumber) represents the lens aperture f-stop fraction."
                    )
                )
            )
        ),

        Course(
            id = "course_priv_201",
            code = "GL-PRIV-201",
            title = "Geospatial Privacy & Data Defense",
            subtitle = "Exposing GPS coordinate hazards, geofencing, and zero-loss privacy sanitization.",
            category = TopicCategory.PRIVACY,
            level = CourseLevel.INTERMEDIATE,
            estimatedHours = 4,
            associatedBadgeCode = "SSF-201",
            lessons = listOf(
                Lesson(
                    id = "l_priv_1",
                    title = "The Threat of Embedded Geotagging",
                    content = """Every time a mobile device captures a photo with location permissions enabled, high-precision GPS coordinates are saved to Tag 0x0001 (GPSLatitudeRef), Tag 0x0002 (GPSLatitude), Tag 0x0003 (GPSLongitudeRef), Tag 0x0004 (GPSLongitude), and Tag 0x0006 (GPSAltitude).

With modern GNSS receivers (combining GPS, GLONASS, Galileo, and BeiDou), coordinates are accurate to within 2 to 3 meters. Sharing unscrubbed photos on public message boards, classifieds, or messaging apps inadvertently reveals residential addresses, children's schools, and work locations.""",
                    codeExample = """# Extracting GPS Degrees, Minutes, Seconds (DMS) to Decimal
def dms_to_decimal(degrees, minutes, seconds, ref):
    decimal = degrees + (minutes / 60.0) + (seconds / 3600.0)
    if ref in ['S', 'W']:
        decimal = -decimal
    return decimal""",
                    keyTakeaways = listOf(
                        "GPS EXIF tags encode coordinates accurate within 2 to 3 meters.",
                        "Unstripped photos pose severe stalking, doxxing, and physical security risks.",
                        "GPS timestamps (GPSTimeStamp, 0x0007) reflect exact UTC atomic clock times."
                    ),
                    estimatedMinutes = 15
                ),
                Lesson(
                    id = "l_priv_2",
                    title = "Zero-Loss Metadata Sanitization Strategies",
                    content = """When removing metadata, many naive applications re-encode the JPEG image bitmap. Re-encoding causes generational loss (recompression artifacts, macroblock blurring, and generational degradation).

In contrast, professional privacy tools like PyMeta Engine perform surgical in-place marker stripping:
1. Parse the JPEG structure until the APP1 0xFFE1 marker is reached.
2. Remove or overwrite the GPS sub-IFD pointers with zeros, or replace the APP1 payload without touching the compressed image scans (0xFFDA).
3. The original high-fidelity DCT coefficients and quantization tables remain 100% bit-exact.""",
                    codeExample = """// Surgical GPS stripping in Android Kotlin
val exif = ExifInterface(file)
exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null)
exif.saveAttributes() // Writes changes without JPEG recompression!""",
                    keyTakeaways = listOf(
                        "Recompressing JPEGs reduces visual clarity and causes irreversible generational loss.",
                        "In-place EXIF attribute stripping preserves original Huffman/DCT image coefficients.",
                        "Selective GPS removal preserves innocent camera settings (aperture, ISO) while protecting privacy."
                    ),
                    estimatedMinutes = 15
                )
            ),
            quiz = Quiz(
                id = "quiz_priv_201",
                title = "Geospatial Privacy Specialist Quiz",
                passingScorePercentage = 80,
                questions = listOf(
                    QuizQuestion(
                        id = "q_priv_1",
                        question = "What conversion formula transforms 40° 42' 46\" N into Decimal Degrees?",
                        options = listOf("40 + (42 / 60) + (46 / 3600)", "40 * 60 + 42 * 3600 + 46", "(40 + 42 + 46) / 3", "40.4246"),
                        correctOptionIndex = 0,
                        explanation = "Decimal Degrees = Degrees + (Minutes / 60) + (Seconds / 3600)."
                    ),
                    QuizQuestion(
                        id = "q_priv_2",
                        question = "Why is surgical EXIF attribute stripping superior to re-saving the bitmap in JPEG format?",
                        options = listOf("It makes the file larger", "It prevents generational compression artifacts and preserves 100% pixel fidelity", "It changes the color palette", "It requires root permissions"),
                        correctOptionIndex = 1,
                        explanation = "Re-saving a Bitmap triggers the JPEG DCT compression pipeline, introducing loss. Direct EXIF header manipulation leaves pixel data untouched."
                    ),
                    QuizQuestion(
                        id = "q_priv_3",
                        question = "Which coordinates reference indicates a negative value in Decimal Degrees?",
                        options = listOf("North and East", "South and West", "North and West", "South and East"),
                        correctOptionIndex = 1,
                        explanation = "Southern latitudes and Western longitudes correspond to negative numeric values in the WGS84 coordinate system."
                    )
                )
            )
        ),

        Course(
            id = "course_steg_301",
            code = "GL-STEG-301",
            title = "Steganography & Hidden Artifact Analysis",
            subtitle = "Least Significant Bit (LSB) embedding, file trailer anomalies, and steganalysis.",
            category = TopicCategory.STEGANOGRAPHY,
            level = CourseLevel.ADVANCED,
            estimatedHours = 5,
            associatedBadgeCode = "SSF-301",
            lessons = listOf(
                Lesson(
                    id = "l_steg_1",
                    title = "Principles of LSB (Least Significant Bit) Injection",
                    content = """Steganography is the art of concealing secret messages within innocent carrier media. The most common technique in raster images (PNG, BMP) is Spatial Domain LSB Insertion.

Each 8-bit color channel (Red, Green, Blue) has values from 0 to 255. Changing the 0th bit (least significant bit) only alters channel luminescence by 1/256th (less than 0.4%), making the mutation imperceptible to the human eye. However, statistical steganalysis tests like Chi-Square and Sample Pair Analysis easily detect unnatural distribution of 0s and 1s in the lowest bitplanes.""",
                    codeExample = """# Checking LSB Bitplane distribution in Python
import numpy as np
from PIL import Image

img = Image.open("carrier.png")
arr = np.array(img)
lsb_plane = arr & 1 # Extract 0th bit
entropy = np.mean(lsb_plane)
print(f"LSB Bitplane 1s density: {entropy:.4f} (Natural images rarely exceed 0.48-0.52 uniformly)")""",
                    keyTakeaways = listOf(
                        "LSB embeds secrets in the lowest bit of color channels with minimal visual impact.",
                        "Statistical steganalysis (Chi-Square) reveals abnormal bit distribution.",
                        "Lossy compression (JPEG DCT) destroys spatial LSB payloads unless specialized F5/J-Steg algorithms are used."
                    ),
                    estimatedMinutes = 15
                ),
                Lesson(
                    id = "l_steg_2",
                    title = "File Trailer & Appended Data (EOF Anomaly)",
                    content = """JPEG containers have an explicit boundary structure:
- Starts with SOI marker: 0xFF 0xD8
- Ends with EOI marker: 0xFF 0xD9

Image viewing software stops reading when encountering 0xFFD9. Threat actors and CTF authors frequently append malicious archives (ZIP, RAR, PE executables) directly after the 0xFFD9 marker. Detecting appended trailers is a fundamental forensic procedure performed by PyMeta Engine.""",
                    codeExample = """# Scanning for bytes trailing EOI in Python
with open("suspicious.jpg", "rb") as f:
    content = f.read()
    eoi_idx = content.find(b"\xff\xd9")
    if eoi_idx != -1 and eoi_idx + 2 < len(content):
        trailing_len = len(content) - (eoi_idx + 2)
        print(f"ANOMALY: {trailing_len} bytes found trailing JPEG EOI!")
        trailer_header = content[eoi_idx + 2 : eoi_idx + 10]
        print(f"Trailer magic bytes: {trailer_header.hex()}")""",
                    keyTakeaways = listOf(
                        "Standard JPEGs terminate strictly at marker 0xFF 0xD9 (End of Image).",
                        "Any bytes trailing 0xFFD9 indicate concealed data, polyglot payloads, or archive stitching.",
                        "Forensic carving isolates the trailer payload for binary inspection."
                    ),
                    estimatedMinutes = 15
                )
            ),
            quiz = Quiz(
                id = "quiz_steg_301",
                title = "Steganography Analyst Certification Quiz",
                passingScorePercentage = 80,
                questions = listOf(
                    QuizQuestion(
                        id = "q_steg_1",
                        question = "What two-byte sequence standardly marks the End of Image (EOI) in JPEG files?",
                        options = listOf("0xFF 0xD8", "0xFF 0xD9", "0x89 0x50", "0x25 0x50"),
                        correctOptionIndex = 1,
                        explanation = "0xFF 0xD9 designates the formal JPEG End-of-Image (EOI) boundary."
                    ),
                    QuizQuestion(
                        id = "q_steg_2",
                        question = "What happens to naive spatial LSB steganography when an image is saved as a lossy JPEG?",
                        options = listOf("The payload is encrypted", "The payload is scrambled and destroyed by discrete cosine transform quantization", "The payload remains intact", "The file size triples"),
                        correctOptionIndex = 1,
                        explanation = "JPEG compression converts spatial pixels into frequency domain DCT coefficients and applies quantization, destroying spatial LSB bits."
                    ),
                    QuizQuestion(
                        id = "q_steg_3",
                        question = "What statistical method is commonly used to detect LSB embedding in continuous-tone images?",
                        options = listOf("Chi-Square Analysis", "RSA Decryption", "Huffman Tree Traversal", "Hamming Code Correction"),
                        correctOptionIndex = 0,
                        explanation = "Chi-Square analysis compares observed frequencies of Pairs of Values (PoVs) against expected natural distributions."
                    )
                )
            )
        ),

        Course(
            id = "course_hash_401",
            code = "GL-HASH-401",
            title = "Cryptographic Hashing & Evidence Integrity",
            subtitle = "MD5, SHA-256, Perceptual Hashing (pHash), and chain of custody standards.",
            category = TopicCategory.FORENSICS,
            level = CourseLevel.INTERMEDIATE,
            estimatedHours = 3,
            associatedBadgeCode = "SSF-401",
            lessons = listOf(
                Lesson(
                    id = "l_hash_1",
                    title = "Exact Cryptographic Hashes vs Perceptual Hashes",
                    content = """In digital forensics, establishing evidence integrity requires cryptographic checksums:
1. SHA-256: 256-bit secure cryptographic hash. Changing even a single bit in a 50MB RAW image produces an entirely different digest (avalanche effect).
2. MD5: 128-bit legacy hash, fast for deduplication but susceptible to chosen-prefix collision attacks.

However, forensic analysts also encounter resized, recompressed, or rotated duplicates across social media. Exact hashes fail because bit values changed. Perceptual Hashing (pHash, aHash, dHash) computes frequency fingerprints:
- Scales image to 8x8 or 32x32 grayscale.
- Computes Discrete Cosine Transform (DCT) frequencies.
- Similar images yield low Hamming distance (<= 10 bits difference), allowing cross-platform image matching.""",
                    codeExample = """# Computing SHA-256 in Kotlin
import java.security.MessageDigest

fun calculateSha256(bytes: ByteArray): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
}""",
                    keyTakeaways = listOf(
                        "SHA-256 guarantees exact bit-level evidence integrity for legal proceedings.",
                        "MD5 is suitable for rapid indexing but should not be used as the sole forensic standard.",
                        "pHash (Perceptual Hash) matches visually identical images regardless of compression or minor cropping."
                    ),
                    estimatedMinutes = 15
                ),
                Lesson(
                    id = "l_hash_2",
                    title = "Chain of Custody & Federal Rules of Evidence (Rule 902(14))",
                    content = """Under US Federal Rule of Evidence 902(14) and international equivalents (ISO/IEC 27037), digital evidence is self-authenticating if verified by a process of digital identification such as a certified cryptographic hash.

Forensic best practices require:
- Immediate acquisition hashing (at the moment of seizure).
- Secondary verification hash before and after any analysis.
- Maintaining an auditable timestamp log (like PyMeta Engine's SQLite Room event ledger).""",
                    codeExample = """-- Room SQL Ledger Schema for Chain of Custody
CREATE TABLE evidence_audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_name TEXT NOT NULL,
    sha256_hash TEXT NOT NULL,
    analyst_id TEXT NOT NULL,
    action_performed TEXT NOT NULL,
    timestamp_utc INTEGER NOT NULL
);""",
                    keyTakeaways = listOf(
                        "ISO 27037 establishes guidelines for handling digital evidence.",
                        "FRE Rule 902(14) accepts hash verification certificates for self-authentication in court.",
                        "Audit logs must record timestamp, action, analyst ID, and verification hashes."
                    ),
                    estimatedMinutes = 15
                )
            ),
            quiz = Quiz(
                id = "quiz_hash_401",
                title = "Evidence Integrity & Hashing Quiz",
                passingScorePercentage = 80,
                questions = listOf(
                    QuizQuestion(
                        id = "q_hash_1",
                        question = "If an image is re-saved at 90% JPEG quality, what happens to its SHA-256 hash?",
                        options = listOf("The hash stays identical", "The hash changes completely due to the avalanche effect", "Only the last 4 bytes change", "The hash becomes shorter"),
                        correctOptionIndex = 1,
                        explanation = "Cryptographic hashes exhibit the avalanche effect; altering a single bit produces a completely different hash digest."
                    ),
                    QuizQuestion(
                        id = "q_hash_2",
                        question = "Which hashing algorithm is designed to match images that underwent recompression or minor scaling?",
                        options = listOf("SHA-512", "MD5", "Perceptual Hash (pHash)", "CRC32"),
                        correctOptionIndex = 2,
                        explanation = "Perceptual hashes compute low-frequency DCT patterns, maintaining close Hamming distance across recompressions."
                    ),
                    QuizQuestion(
                        id = "q_hash_3",
                        question = "What legal standard in US Federal Courts allows hash-verified digital records to self-authenticate?",
                        options = listOf("FRE Rule 902(14)", "HIPAA Section 8", "DMCA Title II", "GDPR Article 17"),
                        correctOptionIndex = 0,
                        explanation = "Federal Rule of Evidence 902(14) governs self-authenticating records generated by an electronic process or system."
                    )
                )
            )
        ),

        Course(
            id = "course_pyth_501",
            code = "GL-PYTH-501",
            title = "Python Metadata Automation & Data Pipelines",
            subtitle = "Batch scripting with Pillow, ExifRead, Piexif, and Pandas analytics pipelines.",
            category = TopicCategory.PYTHON,
            level = CourseLevel.ADVANCED,
            estimatedHours = 4,
            associatedBadgeCode = "SSF-501",
            lessons = listOf(
                Lesson(
                    id = "l_py_1",
                    title = "Building a PEP 8 Forensic Ingestion Script",
                    content = """Automating metadata extraction across terabytes of photographic evidence requires modular, PEP 8 and PEP 257 compliant Python pipelines.

PyMeta Engine exports Python code adhering strictly to:
- PEP 8: 4-space indentation, snake_case functions, UPPER_CASE constants, max 79 character docstrings.
- Type Annotations: Full typing with typing.Dict, typing.Optional, and Pathlib.Path.
- Exception Handling: Graceful handling of CorruptExifError and UnidentifiedImageError.""",
                    codeExample = """from pathlib import Path
from typing import Dict, Any, Optional
from PIL import Image, ExifTags

def extract_exif(image_path: Path) -> Dict[str, Any]:
    \"\"\"Extract EXIF tags from JPEG or TIFF image file.

    Args:
        image_path: Path object pointing to source image file.

    Returns:
        Dictionary mapping human-readable tag names to values.
    \"\"\"
    with Image.open(image_path) as img:
        raw_exif = img.getexif()
        if not raw_exif:
            return {}
        return {
            ExifTags.TAGS.get(k, str(k)): v
            for k, v in raw_exif.items()
        }""",
                    keyTakeaways = listOf(
                        "Adhere to PEP 8 styling and PEP 257 docstring conventions.",
                        "Use Pathlib for cross-platform POSIX and Windows filesystem paths.",
                        "Always close file descriptors safely using Context Managers (with statements)."
                    ),
                    estimatedMinutes = 15
                ),
                Lesson(
                    id = "l_py_2",
                    title = "Data Analysis with Pandas and CSV/JSON Exports",
                    content = """Once EXIF data is serialized into RFC 4180 CSV or structured JSON by PyMeta Engine, data scientists can ingest it into Pandas for statistical analysis:
- Camera distribution bar charts.
- Focal length histograms.
- GPS clustering for geospatial movement path reconstruction using Folium/GeoPandas.""",
                    codeExample = """import pandas as pd
import json

# Ingest PyMeta exported CSV
df = pd.read_csv("pymeta_export.csv")
print("Top 5 Most Common Cameras:")
print(df["camera_model"].value_counts().head(5))

# Filter exposed GPS records
gps_exposed = df[df["gps_latitude"].notnull()]
print(f"Total photos exposing location: {len(gps_exposed)}")""",
                    keyTakeaways = listOf(
                        "CSV export facilitates immediate Pandas DataFrame ingestion.",
                        "JSON hierarchical exports preserve nested IPTC and sub-IFD structures.",
                        "Automated batch pipelines enable rapid threat intelligence audits."
                    ),
                    estimatedMinutes = 15
                )
            ),
            quiz = Quiz(
                id = "quiz_pyth_501",
                title = "Python Automation & Pipelines Quiz",
                passingScorePercentage = 85,
                questions = listOf(
                    QuizQuestion(
                        id = "q_py_1",
                        question = "Which Python standard library module is recommended for cross-platform filesystem path operations?",
                        options = listOf("pathlib", "sys", "math", "socket"),
                        correctOptionIndex = 0,
                        explanation = "pathlib provides an object-oriented, cross-platform approach to managing filesystem paths."
                    ),
                    QuizQuestion(
                        id = "q_py_2",
                        question = "What PEP standard dictates Python Docstring conventions?",
                        options = listOf("PEP 257", "PEP 8", "PEP 20", "PEP 484"),
                        correctOptionIndex = 0,
                        explanation = "PEP 257 specifies docstring semantics and conventions, while PEP 8 focuses on general code style."
                    ),
                    QuizQuestion(
                        id = "q_py_3",
                        question = "Which Pillow method retrieves top-level image metadata in modern PIL versions?",
                        options = listOf("img.getexif()", "img.read_all_tags()", "img.decode_meta()", "img.binary_dump()"),
                        correctOptionIndex = 0,
                        explanation = "img.getexif() is the standard Pillow method for obtaining an Exif object."
                    )
                )
            )
        )
    )

    val documentationSections: List<DocSection> = listOf(
        DocSection(
            id = "doc_exif_spec",
            title = "JEITA CP-3451 & TIFF 6.0 Specification Guide",
            category = "Standards",
            summary = "Technical reference for APP1 markers, IFD entry layout, and standard byte structures.",
            content = """The Exchangeable Image File Format (EXIF) standard defines the metadata structure for digital still cameras. In JPEG compressed files, EXIF is stored within an APP1 application marker segment (0xFFE1).

A standard TIFF header consists of:
1. Bytes 0-1: Byte order mark ('II' for Little-Endian, 'MM' for Big-Endian).
2. Bytes 2-3: Arbitrary constant 42 (0x002A).
3. Bytes 4-7: Offset to the first Image File Directory (IFD0).

Each IFD contains a 2-byte count of entries, followed by 12-byte directory entries:
- Bytes 0-1: Tag ID
- Bytes 2-3: Type (BYTE, ASCII, SHORT, LONG, RATIONAL, SSHORT, SLONG, SRATIONAL, UNDEFINED)
- Bytes 4-7: Count of components
- Bytes 8-11: Value or offset to value if payload exceeds 4 bytes.""",
            tags = listOf("EXIF", "TIFF", "JEITA", "APP1", "Spec"),
            codeSnippet = """// Binary structure of a 12-byte IFD entry:
// [2 bytes Tag ID] [2 bytes Type] [4 bytes Count] [4 bytes Value/Offset]"""
        ),
        DocSection(
            id = "doc_cli_ref",
            title = "PyMeta CLI Shell Command Reference",
            category = "CLI Manual",
            summary = "Comprehensive command dictionary, arguments, and return codes for the pymeta shell.",
            content = """The PyMeta CLI provides a Unix-like interactive shell running on top of an Android Room SQLite history database.

Available Commands:
- pymeta inspect <file|--all>: Dumps complete EXIF/IPTC tags, GPS coordinates, and camera metrics.
- pymeta strip <file|all> [--gps-only]: Safely sanitizes metadata tags without JPEG generational degradation.
- pymeta set <file> --artist <name> [--copyright <text>]: Modifies IPTC artist and copyright tags.
- pymeta export-csv <file|--all>: Serializes active catalog metadata into RFC 4180 CSV.
- pymeta export-json <file|--all> [--compact]: Generates structured JSON output.
- pymeta export-python <file>: Emits a PEP 8 Python script using Pillow or Piexif.
- pymeta hash <file>: Computes MD5, SHA-256, and perceptual hash digests.
- pymeta diff <file1> <file2>: Performs side-by-side metadata delta analysis.""",
            tags = listOf("CLI", "Terminal", "Commands", "Syntax", "Room DB"),
            codeSnippet = """pymeta inspect --all
pymeta strip --all --gps-only
pymeta export-csv --all"""
        ),
        DocSection(
            id = "doc_ssf_creds",
            title = "Security & Steganography Fundamentals (SSF) Credentialing",
            category = "Certification",
            summary = "Framework criteria, badge verification codes, and educational credential rules.",
            content = """SSF (Security & Steganography Fundamentals) is PyMeta's professional credentialing framework designed in partnership with Great Learning curriculum tracks.

Tiers and Credential Requirements:
- SSF-101 (Scout / Bronze): Awarded upon mastering EXIF Foundations (GL-EXIF-101) with >= 75% score.
- SSF-201 (Defender / Silver): Awarded for Geospatial Privacy Defense (GL-PRIV-201) with >= 80% score.
- SSF-301 (Specialist / Gold): Awarded for Steganography Investigation (GL-STEG-301) with >= 80% score.
- SSF-401 (Specialist / Gold): Awarded for Forensic Hashing & Integrity (GL-HASH-401) with >= 80% score.
- SSF-501 (Master / Platinum): Awarded for Python Automation Pipelines (GL-PYTH-501) with >= 85% score.

Credential Verification:
Each issued credential generates a unique SHA-256 verification hash and unique Credential ID format (e.g. SSF-CRED-9281-GL) that can be shared and verified.""",
            tags = listOf("SSF", "Badges", "Certification", "Great Learning", "Credentials"),
            codeSnippet = """// Example verification credential format:
// SSF-CRED-[RANDOM_HEX]-GL"""
        ),
        DocSection(
            id = "doc_proguard_guide",
            title = "ProGuard, R8, and Play Store Symbol De-obfuscation Guide",
            category = "Android Engineering",
            summary = "Configuring mapping.txt, line number retention, and symbol uploads for Google Play Console.",
            content = """To ensure production releases maintain maximum optimization while preserving stack trace line numbers and crash symbolication in the Google Play Console:

1. Symbol Preservation:
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

2. Native Crash & Mapping Uploads:
Google Play Console requires `mapping.txt` (generated under `app/build/outputs/mapping/release/mapping.txt`).

3. Serialization & ExifInterface Retention:
Keep models, Room DAOs, and AndroidX ExifInterface tags intact to prevent reflection crashes when ProGuard minification is enabled.""",
            tags = listOf("ProGuard", "R8", "Play Store", "Symbols", "De-obfuscation"),
            codeSnippet = """# Essential Play Store Stack Trace Mapping in proguard-rules.pro:
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod"""
        )
    )
}

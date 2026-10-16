# ==============================================================================
# PyMeta Engine - Production ProGuard & R8 Configuration
# Optimized for Google Play Store Releases with Full De-obfuscation & Stack Trace Symbols
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Play Store Crash Reporting, Mapping & Symbol De-obfuscation
# ------------------------------------------------------------------------------
# Retain line numbers and source file names for Google Play Console crash de-obfuscation
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve inner classes, annotations, and generic signatures for reflection & debugging
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# Keep parameter names for public APIs and debugging
-keepparameternames

# ------------------------------------------------------------------------------
# 2. Jetpack Compose & Material 3 Optimization
# ------------------------------------------------------------------------------
# Keep Compose runtime and compiler stability markers
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }

# Retain Composable functions and prevent stripping during bytecode shrinking
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}

# Keep Compose state holders and mutable states
-keepclassmembers class * {
    androidx.compose.runtime.MutableState *;
    androidx.compose.runtime.State *;
}

# ------------------------------------------------------------------------------
# 3. Kotlin Standard Library, Coroutines & Flow
# ------------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep Kotlin Metadata for reflection if needed
-keepclassmembers class * {
    kotlin.Metadata *;
}

# ------------------------------------------------------------------------------
# 4. AndroidX ExifInterface & Media Tag Parsers
# ------------------------------------------------------------------------------
# Critical: ExifInterface requires preserving tag constants, reflection access, and stream decoders
-keep class androidx.exifinterface.media.ExifInterface { *; }
-keepclassmembers class androidx.exifinterface.media.ExifInterface {
    public static final java.lang.String TAG_*;
    public <methods>;
    public <fields>;
}

# ------------------------------------------------------------------------------
# 5. Room Database & SQLite Entities
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.Database *;
    @androidx.room.Insert *;
    @androidx.room.Query *;
    @androidx.room.Update *;
    @androidx.room.Delete *;
}
-dontwarn androidx.room.paging.**

# ------------------------------------------------------------------------------
# 6. PyMeta Data Models, Exporters & Learning Domain
# ------------------------------------------------------------------------------
# Prevent R8 from obfuscating JSON/CSV fields, metadata containers, and learning badges
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.model.** { *; }

-keep class com.example.learning.model.** { *; }
-keepclassmembers class com.example.learning.model.** { *; }

-keep class com.example.learning.data.** { *; }
-keepclassmembers class com.example.learning.data.** { *; }

-keep class com.example.export.** { *; }
-keepclassmembers class com.example.export.** { *; }

-keep class com.example.auth.** { *; }
-keepclassmembers class com.example.auth.** { *; }

-keep class com.example.cli.** { *; }
-keepclassmembers class com.example.cli.** { *; }

-keep class com.example.db.** { *; }
-keepclassmembers class com.example.db.** { *; }

# ------------------------------------------------------------------------------
# 7. Google Credential Manager & Firebase Auth
# ------------------------------------------------------------------------------
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.google.firebase.**

# ------------------------------------------------------------------------------
# 8. General Release Optimizations & Hygiene
# ------------------------------------------------------------------------------
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
-dontusemixedcaseclassnames
-repackageclasses ''
-allowaccessmodification

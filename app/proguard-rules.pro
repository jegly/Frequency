# Preserve source file names and line numbers for readable crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Kotlin generic type signatures (needed for StateFlow, coroutines, etc.)
-keepattributes Signature

# Keep annotations (required by Glide, Compose, and Kotlin metadata)
-keepattributes *Annotation*

# ── Data models (serialized to/from JSON by PlaylistStorageManager) ───────────
-keep class com.jegly.frequency.model.** { *; }

# ── Glide ─────────────────────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
    *** rewind();
}
-dontwarn com.bumptech.glide.**

# ── Kotlin coroutines ─────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ── Kotlin metadata (needed for reflection-based libraries) ───────────────────
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ── Enums ─────────────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── AndroidX media (MediaBrowserService, MediaController) ─────────────────────
-keep class androidx.media.** { *; }
-keep class android.support.v4.media.** { *; }

# ── Biometric ─────────────────────────────────────────────────────────────────
-keep class androidx.biometric.** { *; }

# ── Splash screen ─────────────────────────────────────────────────────────────
-keep class androidx.core.splashscreen.** { *; }

# ── DocumentFile (SAF folder import) ─────────────────────────────────────────
-keep class androidx.documentfile.** { *; }

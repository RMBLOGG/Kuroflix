# ============================================================
# FIX: NPE gara-gara R8 obfuscate model class yang dipakai Moshi
# (field kebaca null padahal non-null). Cuma kejadian di build
# minify-on (perf/release), gak kejadian di debug.
# ============================================================

# --- Kotlin metadata & reflection (wajib buat moshi-kotlin reflection based) ---
-keep class kotlin.Metadata { *; }
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault, Signature, InnerClasses, EnclosingMethod

# --- Moshi ---
-keep,allowobfuscation,allowshrinking interface com.squareup.moshi.JsonQualifier
-keepclassmembers class kotlin.Metadata { public <methods>; }
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-dontwarn okio.**

# Keep semua generated JsonAdapter (dari moshi-kotlin-codegen/ksp)
-keep class **JsonAdapter { *; }
-keepclassmembers class * extends com.squareup.moshi.JsonAdapter { *; }
-keepclasseswithmembers class * {
    @com.squareup.moshi.FromJson <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.ToJson <methods>;
}

# Keep semua data class model kita yang dipakai Moshi (@JsonClass) beserta
# constructor & field-nya SUPAYA NAMA/URUTAN FIELD GAK BERUBAH pas serialize/deserialize.
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class com.dayynime.kuroflix.data.network.** {
    *** Companion;
    <init>(...);
    <fields>;
}
-keep class com.dayynime.kuroflix.data.network.** { *; }
-keep class com.dayynime.kuroflix.data.model.** { *; }

# --- Retrofit ---
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface com.dayynime.kuroflix.data.network.** {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# --- OkHttp ---
-dontwarn okhttp3.**

# --- Room ---
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.**

# --- ExoPlayer/Media3 ---
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }

# --- Data class umum: constructor + field, biar aman kalau ada model lain nanti ---
-keepclassmembers class com.dayynime.kuroflix.** {
    public <init>(...);
}

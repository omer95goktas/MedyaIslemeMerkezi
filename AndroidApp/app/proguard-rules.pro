# Proguard Rules for Medya ve Belge İşleme Merkezi

# Keep Retrofit & OkHttp models
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Compose rules
-keepclassmembers class * extends androidx.compose.runtime.State { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { *; }

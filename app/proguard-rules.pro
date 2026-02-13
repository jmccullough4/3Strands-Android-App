# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keep class com.threestrandscattle.app.models.** { *; }
-keep class com.threestrandscattle.app.services.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Remove debug and verbose logging in release builds
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}

# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keep class com.threestrandscattle.app.models.** { *; }
-keep class com.threestrandscattle.app.services.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

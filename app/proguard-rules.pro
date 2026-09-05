# ProGuard / R8 Rules for SpeakFree P2P (WebRTC + Firebase Firestore)

# WebRTC JNI & Native methods
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**
-keepclasseswithmembernames class * {
    native <methods>;
}

# Firebase Firestore Serialization
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class com.google.firebase.firestore.** {
    public void set*(...);
    public <fields>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Google Play Billing Library v7
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.api.**

# Coil Image Loading
-dontwarn coil.**

# Remove Log calls in release to shrink size and enhance security
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}



# Keep Kotlin metadata / coroutines
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Kotlin serialization (used by core models)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.finley.android.sudoku.**$$serializer { *; }
-keepclassmembers class com.finley.android.sudoku.** {
    *** Companion;
}
-keepclasseswithmembers class com.finley.android.sudoku.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Google Play Billing
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# Multiplatform settings (SharedPreferences / NSUserDefaults / localStorage)
-keep class com.russhwolf.settings.** { *; }
-dontwarn com.russhwolf.settings.**

# Compose runtime keeps its own rules; suppress harmless metadata warnings
-dontwarn org.jetbrains.compose.**

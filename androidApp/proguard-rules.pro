# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep data classes
-keep class com.tadevolta.gym.data.models.** { *; }
-keep class com.tadevolta.gym.domain.entities.** { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.tadevolta.gym.data.models.**$$serializer { *; }
-keepclassmembers class com.tadevolta.gym.data.models.** {
    *** Companion;
}
-keepclasseswithmembers class com.tadevolta.gym.data.models.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep SQLDelight
-keep class com.tadevolta.gym.data.local.** { *; }

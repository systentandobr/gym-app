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

# SLF4J - Ignorar classes ausentes (não usamos SLF4J no Android, usamos android.util.Log)
# O ktor-client-logging referencia SLF4J, mas no Android usamos um logger customizado
-dontwarn org.slf4j.**
-dontwarn org.slf4j.impl.**
# Ignorar especificamente a classe StaticLoggerBinder que está causando o erro
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Proteger código de criptografia do Android Security Crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Proteger classes relacionadas a criptografia
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

# Proteger MasterKey e EncryptedSharedPreferences
-keep class androidx.security.crypto.MasterKey { *; }
-keep class androidx.security.crypto.EncryptedSharedPreferences { *; }

# Manter métodos de criptografia
-keepclassmembers class * {
    @androidx.security.crypto.* <methods>;
}

# Evitar ofuscar erros de criptografia para facilitar debug
-keep class javax.crypto.AEADBadTagException { *; }
-keep class java.io.IOException { *; }

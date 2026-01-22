plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.tadevolta.gym"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tadevolta.gym"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"https://api-prd.systentando.com\"")
            buildConfigField("String", "SYS_SEGURANCA_BASE_URL", "\"https://auth.systentando.com\"")
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_BASE_URL", "\"https://api-prd.systentando.com\"")
            buildConfigField("String", "SYS_SEGURANCA_BASE_URL", "\"https://auth.systentando.com\"")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":shared"))
    
    // Ktor Client (para KAPT)
    implementation("io.ktor:ktor-client-android:2.3.6")
    
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Coil (imagens)
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")
    
    // Encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
    javacOptions {
        option("--add-opens", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
        option("--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED")
        option("--add-opens", "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED")
        option("--add-opens", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED")
        option("--add-opens", "jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED")
        option("--add-opens", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
        option("--add-opens", "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
        option("--add-opens", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED")
        option("--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED")
        option("--add-opens", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
    }
    useBuildCache = true
}

// Configurar JVM args para processos KAPT (necessário para Java 17+)
tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask>().configureEach {
    kaptProcessJvmArgs.add("--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
    kaptProcessJvmArgs.add("--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED")
    kaptProcessJvmArgs.add("--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED")
    kaptProcessJvmArgs.add("--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED")
    kaptProcessJvmArgs.add("--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED")
    kaptProcessJvmArgs.add("--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
    kaptProcessJvmArgs.add("--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
    kaptProcessJvmArgs.add("--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED")
    kaptProcessJvmArgs.add("--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED")
    kaptProcessJvmArgs.add("--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
    kaptProcessJvmArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
}

// Garantir que o módulo shared seja compilado antes do androidApp
tasks.matching { it.name.startsWith("assemble") }.configureEach {
    dependsOn(":shared:build")
}

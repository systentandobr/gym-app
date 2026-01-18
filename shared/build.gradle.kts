plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.22"
    id("com.android.library")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                // HTTP Client
                implementation("io.ktor:ktor-client-core:2.3.6")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.6")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")
                implementation("io.ktor:ktor-client-logging:2.3.6")
                implementation("io.ktor:ktor-client-auth:2.3.6")
                
                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                
                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
                
                // SQLDelight
                implementation("app.cash.sqldelight:runtime:2.0.0")
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:2.3.6")
                implementation("app.cash.sqldelight:android-driver:2.0.0")
                implementation("androidx.security:security-crypto:1.1.0-alpha06")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
    }
}

sqldelight {
    databases {
        create("TadevoltaDatabase") {
            packageName.set("com.tadevolta.gym.data.local")
            generateAsync.set(false)
        }
    }
}

android {
    namespace = "com.tadevolta.gym.shared"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
        buildConfigField("String", "API_BASE_URL", "\"https://api-dev.tadevolta.com\"")
        buildConfigField("String", "SYS_SEGURANCA_BASE_URL", "\"https://auth-dev.systentando.com\"")
    }
    
    buildFeatures {
        buildConfig = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

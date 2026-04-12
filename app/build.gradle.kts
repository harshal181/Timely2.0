import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.om.timely"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.om.timely"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val properties = Properties()
        val localPropsObj = project.rootProject.file("local.properties")
        if (localPropsObj.exists()) {
            properties.load(localPropsObj.inputStream())
        }
        
        buildConfigField("String", "BASE_URL", "\"http://${properties.getProperty("SERVER_IP_ADDRESS","192.168.158.166")}:8000\"")
        

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${properties.getProperty("CLOUDINARY_CLOUD_NAME", "YOUR_CLOUD_NAME")}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"${properties.getProperty("CLOUDINARY_API_KEY", "YOUR_KEY")}\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${properties.getProperty("CLOUDINARY_API_SECRET", "YOUR_SECRET")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.text.google.fonts)
    
    // Koin
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.workmanager)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.cio)
//    implementation(libs.ktor.client.sse)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Security Crypto
    implementation(libs.androidx.security.crypto)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.ui.text.google.fonts)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.common.jvm)
    ksp(libs.androidx.room.compiler)

    // Reorderable
    implementation("sh.calvin.reorderable:reorderable:2.4.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    // Provides .await() on Firebase Tasks for clean coroutine integration
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    
    // Cloudinary for Image Uploads
    implementation("com.cloudinary:cloudinary-android:3.0.2")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
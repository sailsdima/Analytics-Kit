plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.analyticskit.rn"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    // The native AnalyticsKit SDK
    implementation("io.github.sailsdima:analyticskit:1.0.0")

    // React Native
    implementation("com.facebook.react:react-android:0.73.0")

    // Coroutines (needed for the bridge's own scope)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}


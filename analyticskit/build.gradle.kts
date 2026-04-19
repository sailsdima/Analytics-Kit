plugins {
    alias(libs.plugins.android.library)
    id("com.vanniktech.maven.publish") version "0.31.0"
}

android {
    namespace = "com.analyticskit"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.androidx.lifecycle.process)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.google.truth)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.test.core)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.sailsdima",
        artifactId = "analyticskit",
        version = "1.0.0"
    )

    pom {
        name.set("AnalyticsKit")
        description.set("A modern, lightweight analytics SDK for Android")
        url.set("https://github.com/sailsdima/Analytics-Kit")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("sailsdima")
                name.set("Dmytro Petrenko")
            }
        }

        scm {
            url.set("https://github.com/sailsdima/Analytics-Kit")
            connection.set("scm:git:github.com/sailsdima/Analytics-Kit.git")
            developerConnection.set("scm:git:ssh://github.com/sailsdima/Analytics-Kit.git")
        }
    }

    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
}

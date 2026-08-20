plugins {
    id("com.android.library")
    `maven-publish`
    signing
}

android {
    namespace = "net.mamby.androidkit.foundation"
    compileSdk = 37
    resourcePrefix = "androidkit_foundation_"

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xexplicit-api=strict")
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

extra["POM_ARTIFACT_ID"] = "foundation"
extra["POM_NAME"] = "Android Kit Foundation"
extra["POM_DESCRIPTION"] = "Small, testable Android platform foundations without UI dependencies."
apply(from = rootProject.file("gradle/publish-android-library.gradle.kts"))

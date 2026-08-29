plugins {
    id("com.android.library")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.screenshot)
    jacoco
}

android {
    namespace = "net.mamby.androidkit.testing"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

jacoco {
    toolVersion = "0.8.15"
}

dependencies {
    implementation(project(":foundation"))
    implementation(project(":localization"))
    implementation(project(":compose"))
    implementation(project(":navigation3"))

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    screenshotTestImplementation(composeBom)

    implementation(libs.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3.adaptive)
    implementation(libs.material3.adaptive.navigation3)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)

    testImplementation(libs.junit4)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.test.junit.ext)

    debugImplementation(libs.compose.ui.test.manifest)

    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.compose.ui.tooling)
}

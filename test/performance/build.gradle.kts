plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.baseline.profile)
}

android {
    namespace = "net.mamby.androidkit.performance"
    compileSdk = 37
    targetProjectPath = ":demo"

    defaultConfig {
        minSdk = 28
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.benchmark.macro.junit4)
    implementation(libs.junit4)
    implementation(libs.test.junit.ext)
    implementation(libs.test.runner)
    implementation(libs.test.uiautomator)
}

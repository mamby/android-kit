plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.baseline.profile)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}


apply(from = rootProject.file("gradle/validate-androidkit-resources.gradle"))

val supportedLanguageTags = providers.gradleProperty("androidKitSupportedLocales")
    .get().split(',').map(String::trim).sortedWith(String.CASE_INSENSITIVE_ORDER)

android {
    namespace = "net.mamby.androidkit.demo"
    compileSdk = 37
    ndkVersion = "30.0.16248370"

    defaultConfig {
        applicationId = "net.mamby.androidkit.demo"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "SUPPORTED_LANGUAGE_TAGS", "\"${supportedLanguageTags.joinToString(",")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        localeFilters += supportedLanguageTags.map { tag ->
            // Android resource lookup requires legacy aliases, even inside BCP 47 qualifiers.
            val subtags = tag.split('-').toMutableList()
            subtags[0] = when (subtags[0]) {
                "id" -> "in"
                "he" -> "iw"
                "yi" -> "ji"
                else -> subtags[0]
            }
            if (subtags.size == 1) subtags.single() else subtags.joinToString("+", prefix = "b+")
        }
        generateLocaleConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
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

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "META-INF/DEPENDENCIES",
        )
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        fatal += "MissingTranslation"
        fatal += "ExtraTranslation"
    }
}

dependencies {
    implementation(project(":foundation"))
    implementation(project(":localization"))
    implementation(project(":compose"))
    implementation(project(":navigation3"))

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.activity.compose)
    implementation(libs.appcompat)
    implementation(libs.biometric)
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.core.splashscreen)
    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.material3.adaptive)
    implementation(libs.material3.adaptive.layout)
    implementation(libs.material3.adaptive.navigation3)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.profile.installer)
    implementation(libs.kotlinx.serialization.core)

    baselineProfile(project(":test:performance"))

    debugImplementation(libs.compose.ui.tooling)
}

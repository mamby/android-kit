plugins {
    id("com.android.library")
    alias(libs.plugins.compose.compiler)
    `maven-publish`
    signing
}

// Full local AAR for the consumer localization contract (AGP's public Artifact API).
androidComponents.onVariants { variant ->
    configurations.consumable("${variant.name}AndroidKitLocalizationElements") {
        attributes {
            fun <T : Any> copyAttribute(key: Attribute<T>) {
                attribute(key, variant.runtimeConfiguration.attributes.getAttribute(key)!!)
            }
            variant.runtimeConfiguration.attributes.keySet().forEach { copyAttribute(it) }
            attribute(Usage.USAGE_ATTRIBUTE, objects.named<Usage>("androidkit-localization"))
        }
        outgoing.artifact(variant.artifacts.get(com.android.build.api.artifact.SingleArtifact.AAR)) {
            type = "aar"
        }
    }
}

android {
    namespace = "net.mamby.androidkit.navigation3"
    compileSdk = 37
    resourcePrefix = "androidkit_navigation3_"

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
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

dependencies {
    api(project(":compose"))
    api(libs.navigation3.runtime)
    implementation(libs.material3.adaptive.navigation3)
    implementation(libs.navigation3.ui)
}

extra["POM_ARTIFACT_ID"] = "navigation3"
extra["POM_NAME"] = "Android Kit Navigation 3"
extra["POM_DESCRIPTION"] = "Saveable multi-back-stack state and adaptive helpers for Jetpack Navigation 3."
apply(from = rootProject.file("gradle/publish-android-library.gradle.kts"))

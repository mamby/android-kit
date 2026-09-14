plugins {
    id("com.android.library")
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
    namespace = "net.mamby.androidkit.localization"
    compileSdk = 37
    resourcePrefix = "androidkit_localization_"

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

dependencies {
    api(project(":foundation"))
    implementation(libs.appcompat)
}

extra["POM_ARTIFACT_ID"] = "localization"
extra["POM_NAME"] = "Android Kit Localization"
extra["POM_DESCRIPTION"] = "Official per-app locale management and locale-aware Android formatting."
apply(from = rootProject.file("gradle/publish-android-library.gradle.kts"))

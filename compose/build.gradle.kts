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
    namespace = "net.mamby.androidkit.compose"
    compileSdk = 37
    resourcePrefix = "androidkit_compose_"

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
    api(project(":foundation"))
    api(platform(libs.compose.bom))
    api(libs.compose.foundation)
    api(libs.compose.material3)
    api(libs.compose.ui)
    implementation(libs.activity.compose)
    implementation(libs.compose.animation)
    implementation(libs.material3.adaptive)
    implementation(libs.material3.adaptive.layout)
    implementation(libs.material3.adaptive.navigation.suite)
}

extra["POM_ARTIFACT_ID"] = "compose"
extra["POM_NAME"] = "Android Kit Compose"
extra["POM_DESCRIPTION"] = "Opinionated Material 3 themes, adaptive layouts, forms and floating navigation."
apply(from = rootProject.file("gradle/publish-android-library.gradle.kts"))

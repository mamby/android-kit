plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baseline.profile) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.screenshot) apply false
    `maven-publish`
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

pluginManagement {
    repositories {
        settings.providers.gradleProperty("androidKitOfflineRepository").orNull?.let {
            maven {
                url = uri(it)
                metadataSources {
                    mavenPom()
                    gradleMetadata()
                    artifact()
                }
            }
        }
        maven("https://dl.google.com/android/maven2/")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        providers.gradleProperty("androidKitOfflineRepository").orNull?.let {
            maven {
                url = uri(it)
                metadataSources {
                    mavenPom()
                    gradleMetadata()
                    artifact()
                }
            }
        }
        maven("https://dl.google.com/android/maven2/")
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidKit"

include(
    ":bom",
    ":compose",
    ":demo",
    ":foundation",
    ":localization",
    ":navigation3",
    ":test",
)

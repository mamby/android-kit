import org.gradle.api.publish.maven.MavenPublication

plugins {
    `java-platform`
    `maven-publish`
    signing
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api("${project.group}:foundation:${project.version}")
        api("${project.group}:localization:${project.version}")
        api("${project.group}:compose:${project.version}")
        api("${project.group}:navigation3:${project.version}")
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["javaPlatform"])
            artifactId = "bom"
            pom {
                name.set("Android Kit BOM")
                description.set("Version alignment for all Android Kit artifacts.")
                url.set(providers.gradleProperty("POM_URL"))
                licenses {
                    license {
                        name.set(providers.gradleProperty("POM_LICENSE_NAME"))
                        url.set(providers.gradleProperty("POM_LICENSE_URL"))
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set(providers.gradleProperty("POM_DEVELOPER_ID"))
                        name.set(providers.gradleProperty("POM_DEVELOPER_NAME"))
                    }
                }
                scm {
                    url.set(providers.gradleProperty("POM_SCM_URL"))
                    connection.set(providers.gradleProperty("POM_SCM_CONNECTION"))
                    developerConnection.set(providers.gradleProperty("POM_SCM_DEV_CONNECTION"))
                }
            }
        }
    }
    repositories {
        maven {
            name = "staging"
            url = uri(rootProject.layout.projectDirectory.dir("staging-repository"))
        }
    }
}

val signingKey = providers.gradleProperty("signingInMemoryKey").orNull
val signingPassword = providers.gradleProperty("signingInMemoryKeyPassword").orNull
if (!signingKey.isNullOrBlank()) {
    signing {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

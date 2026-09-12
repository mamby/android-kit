import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

abstract class ValidateAndroidKitLocalization : DefaultTask() {
    @get:InputFile
    abstract val catalogFile: RegularFileProperty

    @get:InputFile
    abstract val snapshotFile: RegularFileProperty

    @TaskAction
    fun validate() {
        val catalog = readObject(catalogFile.get().asFile)
        val snapshot = readObject(snapshotFile.get().asFile)
        val catalogStrings = objectValue(catalog["strings"], "strings")
        val snapshotStrings = objectValue(snapshot["strings"], "strings")
        val locales = listValue(snapshot, "locales").map { it.toString() }.toSet()
        val errors = mutableListOf<String>()

        catalogStrings.forEach { (rawKey, catalogValue) ->
            val key = rawKey.toString()
            val catalogEntry = objectValue(catalogValue, key)
            val snapshotEntry = snapshotStrings[rawKey] ?: snapshotStrings[key]
            if (snapshotEntry == null) {
                errors += "$key is missing from the host snapshot."
                return@forEach
            }

            val snapshotObject = objectValue(snapshotEntry, key)
            val catalogRevision = numberValue(catalogEntry, "revision", key)
            val snapshotRevision = numberValue(snapshotObject, "revision", key)
            if (catalogRevision != snapshotRevision) {
                errors += "$key changed in AndroidKit (revision $catalogRevision); update its host translation."
            }

            val translations = objectValue(snapshotObject["translations"], "translations")
            locales.filterNot { it.equals("en", ignoreCase = true) }.forEach { locale ->
                val translation = translations[locale]?.toString().orEmpty().trim()
                if (translation.isEmpty()) {
                    errors += "$key has no translation for supported locale $locale."
                }
            }
        }

        snapshotStrings.keys.filterNot(catalogStrings::containsKey).forEach { key ->
            errors += "$key is no longer present in AndroidKit; remove it from the host snapshot."
        }

        if (errors.isNotEmpty()) {
            throw GradleException(
                "AndroidKit localization validation failed:\n" + errors.joinToString("\n") { "- $it" },
            )
        }
    }

    private fun readObject(file: java.io.File): Map<*, *> =
        (JsonSlurper().parse(file) as? Map<*, *>)
            ?: error("Expected a JSON object in ${file.absolutePath}.")

    private fun objectValue(value: Any?, name: String): Map<*, *> =
        (value as? Map<*, *>) ?: error("Expected object '$name'.")

    private fun listValue(value: Map<*, *>, name: String): List<*> =
        (value[name] as? List<*>) ?: error("Expected array '$name'.")

    private fun numberValue(value: Map<*, *>, name: String, key: String): Int =
        (value[name] as? Number)?.toInt() ?: error("Expected numeric '$name' for '$key'.")
}

val catalogPath = providers.gradleProperty("androidKitLocalizationCatalog").orNull
val snapshotPath = providers.gradleProperty("androidKitLocalizationSnapshot").orNull

if (catalogPath != null && snapshotPath != null) {
    tasks.register<ValidateAndroidKitLocalization>("validateAndroidKitLocalization") {
        catalogFile.set(rootProject.layout.projectDirectory.file(catalogPath))
        snapshotFile.set(rootProject.layout.projectDirectory.file(snapshotPath))
    }
    tasks.matching {
        it.name == "check" ||
            it.name == "preBuild" ||
            it.name.startsWith("pre") && it.name.endsWith("Build") ||
            it.name.startsWith("assemble") ||
            it.name.startsWith("bundle") ||
            it.name.startsWith("lint")
    }.configureEach {
        dependsOn("validateAndroidKitLocalization")
    }
}

package net.mamby.androidkit.compose.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.text.Normalizer
import java.util.Locale
import net.mamby.androidkit.compose.R
import org.json.JSONObject

private const val BuiltInLanguageTag = "en-x-andkit"

internal fun builtInSearchTerms(id: String): AndroidKitSettingsSearchTerms =
    AndroidKitSettingsSearchTerms(mapOf(BuiltInLanguageTag to listOf(id)))

internal fun mergeSearchTerms(
    primary: AndroidKitSettingsSearchTerms?,
    additional: List<AndroidKitSettingsSearchTerms>,
): AndroidKitSettingsSearchTerms? {
    val all = listOfNotNull(primary) + additional
    if (all.isEmpty()) return null
    val merged = linkedMapOf<String, MutableList<String>>()
    all.forEach { terms ->
        terms.byLanguageTag.forEach { (tag, values) -> merged.getOrPut(tag) { mutableListOf() }.addAll(values) }
    }
    return AndroidKitSettingsSearchTerms(merged.mapValues { (_, values) -> values.distinct() })
}

internal data class SettingsSearchLexicon(
    val termsByEntryId: Map<String, List<String>>,
)

@Composable
internal fun rememberSettingsSearchLexicon(): SettingsSearchLexicon {
    val resources = LocalContext.current.resources
    return remember(resources) {
        resources.openRawResource(R.raw.androidkit_compose_settings_search_lexicon)
            .bufferedReader()
            .use { reader -> parseSettingsSearchLexicon(reader.readText()) }
    }
}

private fun parseSettingsSearchLexicon(json: String): SettingsSearchLexicon {
    val root = JSONObject(json)
    val entries = root.getJSONObject("entries")
    val result = linkedMapOf<String, List<String>>()
    entries.keys().forEach { id ->
        val locales = entries.getJSONObject(id)
        val terms = buildList {
            locales.keys().forEach { tag ->
                val values = locales.getJSONArray(tag)
                repeat(values.length()) { index -> add(values.getString(index)) }
            }
        }
        result[id] = terms
    }
    return SettingsSearchLexicon(result)
}

internal fun AndroidKitSettingsSearchTerms.expandedTerms(
    lexicon: SettingsSearchLexicon,
): List<String> = byLanguageTag.flatMap { (tag, values) ->
    if (tag == BuiltInLanguageTag) values.flatMap { lexicon.termsByEntryId[it].orEmpty() } else values
}

private val SearchMarks = Regex("\\p{M}+")
private val SearchSeparators = Regex("[^\\p{L}\\p{N}]+")

internal fun normalizeSettingsSearchText(value: String): String =
    Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        .replace(SearchMarks, "")
        .lowercase(Locale.ROOT)
        .replace(SearchSeparators, " ")
        .trim()
        .replace(Regex("\\s+"), " ")

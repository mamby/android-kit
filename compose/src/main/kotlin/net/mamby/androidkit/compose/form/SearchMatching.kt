package net.mamby.androidkit.compose.form

import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

private val SearchMarks = Regex("\\p{M}+")
private val SearchSeparators = Regex("[^\\p{L}\\p{N}]+")

internal fun normalizeSearchText(value: String): String =
    Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        .replace(SearchMarks, "")
        .lowercase(Locale.ROOT)
        .replace(SearchSeparators, " ")
        .trim()

internal fun searchQueryTokens(query: String): List<String> =
    normalizeSearchText(query).split(' ').filter(String::isNotBlank)

/** Declaration order is the relevance order; equally relevant results keep their source order. */
internal enum class SearchMatch {
    ExactLabel,
    LabelPrefix,
    LabelSubstring,
    VisibleText,
    Alias,
}

internal fun searchMatch(
    label: String,
    visibleText: List<String>,
    aliases: List<String>,
    tokens: List<String>,
): SearchMatch? {
    return SearchDocument("", label, visibleText, aliases).prepare()
        .match(tokens, tokens.joinToString(" "))
}

/** Searchable text is independent of host callbacks, enabled state and result rendering. */
internal data class SearchDocument(
    val key: String,
    val label: String,
    val visibleText: List<String>,
    val aliases: List<String>,
)

internal data class PreparedSearchText(
    val label: String,
    val visibleText: String,
    val allText: String,
) {
    fun match(tokens: List<String>, normalizedQuery: String): SearchMatch? {
        if (tokens.isEmpty() || !tokens.all(allText::contains)) return null
        return when {
            label == normalizedQuery -> SearchMatch.ExactLabel
            label.startsWith(normalizedQuery) -> SearchMatch.LabelPrefix
            label.contains(normalizedQuery) -> SearchMatch.LabelSubstring
            tokens.all(visibleText::contains) -> SearchMatch.VisibleText
            else -> SearchMatch.Alias
        }
    }
}

private fun SearchDocument.prepare(): PreparedSearchText = PreparedSearchText(
    label = normalizeSearchText(label),
    visibleText = normalizeSearchText(visibleText.joinToString(" ")),
    allText = normalizeSearchText((visibleText + aliases).joinToString(" ")),
)

internal suspend fun prepareSearchDocuments(documents: List<SearchDocument>): List<PreparedSearchText> =
    documents.map { document ->
        currentCoroutineContext().ensureActive()
        document.prepare()
    }

internal data class IndexedSearchMatch(val index: Int, val score: SearchMatch)

internal suspend fun matchSearchDocuments(
    documents: List<PreparedSearchText>,
    query: String,
): List<IndexedSearchMatch> {
    val tokens = searchQueryTokens(query)
    if (tokens.isEmpty()) return emptyList()
    val normalizedQuery = tokens.joinToString(" ")
    val buckets = List(SearchMatch.entries.size) { mutableListOf<IndexedSearchMatch>() }
    documents.forEachIndexed { index, document ->
        currentCoroutineContext().ensureActive()
        document.match(tokens, normalizedQuery)?.let { score ->
            buckets[score.ordinal] += IndexedSearchMatch(index, score)
        }
    }
    return buckets.flatten()
}

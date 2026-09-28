package net.mamby.androidkit.compose.form

import java.text.Normalizer
import java.util.Locale

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
    if (tokens.isEmpty()) return null
    val normalizedLabel = normalizeSearchText(label)
    val directText = normalizeSearchText(visibleText.joinToString(" "))
    val allText = normalizeSearchText((visibleText + aliases).joinToString(" "))
    if (!tokens.all(allText::contains)) return null
    val normalizedQuery = tokens.joinToString(" ")
    return when {
        normalizedLabel == normalizedQuery -> SearchMatch.ExactLabel
        normalizedLabel.startsWith(normalizedQuery) -> SearchMatch.LabelPrefix
        normalizedLabel.contains(normalizedQuery) -> SearchMatch.LabelSubstring
        tokens.all(directText::contains) -> SearchMatch.VisibleText
        else -> SearchMatch.Alias
    }
}

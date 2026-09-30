package net.mamby.androidkit.compose.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class PreparedSearchIndex(
    val source: List<SearchDocument>,
    val documents: List<PreparedSearchText>,
)

private data class SearchResults(
    val index: PreparedSearchIndex,
    val query: String,
    val matches: List<IndexedSearchMatch>,
)

/** Null means the current request is pending; outgoing results never remain actionable. */
@Composable
internal fun rememberSearchMatches(
    documents: List<SearchDocument>,
    query: String,
): List<IndexedSearchMatch>? {
    val prepared by produceState<PreparedSearchIndex?>(null, documents) {
        value = withContext(Dispatchers.Default) {
            PreparedSearchIndex(documents, prepareSearchDocuments(documents))
        }
    }
    val currentIndex = prepared?.takeIf { it.source == documents }
    val result by produceState<SearchResults?>(null, currentIndex, query) {
        value = currentIndex?.let { index ->
            withContext(Dispatchers.Default) {
                SearchResults(index, query, matchSearchDocuments(index.documents, query))
            }
        }
    }
    if (query.isBlank()) return emptyList()
    return result?.takeIf { it.index === currentIndex && it.query == query }?.matches
}

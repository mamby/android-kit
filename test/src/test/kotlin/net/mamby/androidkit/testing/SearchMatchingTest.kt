package net.mamby.androidkit.testing

import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import net.mamby.androidkit.compose.form.SearchDocument
import net.mamby.androidkit.compose.form.PreparedSearchText
import net.mamby.androidkit.compose.form.prepareSearchDocuments
import net.mamby.androidkit.compose.form.matchSearchDocuments
import net.mamby.androidkit.compose.form.SearchMatch
import net.mamby.androidkit.compose.form.normalizeSearchText
import net.mamby.androidkit.compose.form.searchMatch
import net.mamby.androidkit.compose.form.searchQueryTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchMatchingTest {
    @Test
    fun preparedIndexPreservesRankingSourceOrderAndCanBeReusedForAnotherQuery() = runBlocking {
        val documents = listOf(
            SearchDocument("alias", "Appointment", listOf("Appointment"), listOf("clinic")),
            SearchDocument("prefix", "Clinic visits", listOf("Clinic visits"), emptyList()),
            SearchDocument("exact", "Clinic", listOf("Clinic"), emptyList()),
            SearchDocument("alias-second", "Appointment", listOf("Appointment"), listOf("clinic")),
            SearchDocument("unicode", "Café", listOf("Café", "Paris"), listOf("balade")),
        )
        val prepared = prepareSearchDocuments(documents)
        assertEquals(listOf(2, 1, 0, 3), matchSearchDocuments(prepared, "clinic").map { it.index })
        assertEquals(listOf(4), matchSearchDocuments(prepared, "CAFE balade paris").map { it.index })
        assertEquals(emptyList<Int>(), matchSearchDocuments(prepared, "missing").map { it.index })
    }

    @Test
    fun indexingAndMatchingStopWhenCancelledDuringIteration() {
        val document = SearchDocument("clinic", "Clinic", listOf("Clinic"), emptyList())
        var indexed = 0
        assertThrows(CancellationException::class.java) {
            runBlocking {
                val job = coroutineContext.job
                prepareSearchDocuments(object : AbstractList<SearchDocument>() {
                    override val size = 100
                    override fun get(index: Int): SearchDocument {
                        indexed++
                        if (index == 5) job.cancel()
                        return document
                    }
                })
            }
        }
        assertTrue(indexed in 6 until 100)
        val prepared = runBlocking { prepareSearchDocuments(listOf(document)) }.single()
        var matched = 0
        assertThrows(CancellationException::class.java) {
            runBlocking {
                val job = coroutineContext.job
                matchSearchDocuments(object : AbstractList<PreparedSearchText>() {
                    override val size = 100
                    override fun get(index: Int): PreparedSearchText {
                        matched++
                        if (index == 5) job.cancel()
                        return prepared
                    }
                }, "clinic")
            }
        }
        assertTrue(matched in 6 until 100)
    }

    @Test
    fun normalizationHandlesAllUnicodeMarksAndIgnoresTheProcessLocale() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"))
            assertEquals("cafe in paris", normalizeSearchText("  CAFÉ, IN PARIS!  "))
            // Nonspacing, spacing-combining, and enclosing marks share the runtime contract.
            assertEquals("ab", normalizeSearchText("a\u0301\u0903\u20DDb"))
            assertEquals(emptyList<String>(), searchQueryTokens("🔎 !"))
        } finally {
            Locale.setDefault(original)
        }
    }

    @Test
    fun everyTokenMustMatchEvenWhenSpreadAcrossVisibleTextAndAliases() {
        assertEquals(SearchMatch.Alias, searchMatch(
            label = "Café walk", visibleText = listOf("Café walk", "Saturday in Paris"),
            aliases = listOf("balade"), tokens = searchQueryTokens("paris balade"),
        ))
        assertNull(searchMatch(
            label = "Café walk", visibleText = listOf("Café walk", "Saturday in Paris"),
            aliases = listOf("balade"), tokens = searchQueryTokens("paris missing"),
        ))
    }

    @Test
    fun visibleLabelRelevancePrecedesSupportingTextAndAliases() {
        val tokens = searchQueryTokens("clinic")
        val matches = listOf(
            searchMatch("Clinic", listOf("Clinic"), emptyList(), tokens),
            searchMatch("Clinic visits", listOf("Clinic visits"), emptyList(), tokens),
            searchMatch("Visit the clinic", listOf("Visit the clinic"), emptyList(), tokens),
            searchMatch("Appointment", listOf("At the clinic"), emptyList(), tokens),
            searchMatch("Appointment", listOf("Appointment"), listOf("clinic"), tokens),
        )
        assertEquals(listOf(SearchMatch.ExactLabel, SearchMatch.LabelPrefix,
            SearchMatch.LabelSubstring, SearchMatch.VisibleText, SearchMatch.Alias), matches)
    }
}

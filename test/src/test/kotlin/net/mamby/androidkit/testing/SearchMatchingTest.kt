package net.mamby.androidkit.testing

import java.util.Locale
import net.mamby.androidkit.compose.form.SearchMatch
import net.mamby.androidkit.compose.form.normalizeSearchText
import net.mamby.androidkit.compose.form.searchMatch
import net.mamby.androidkit.compose.form.searchQueryTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchMatchingTest {
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

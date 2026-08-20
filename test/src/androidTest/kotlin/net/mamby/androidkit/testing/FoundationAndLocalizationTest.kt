package net.mamby.androidkit.testing

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale
import net.mamby.androidkit.foundation.ExternalIntents
import net.mamby.androidkit.localization.AppLocaleManager
import net.mamby.androidkit.localization.LocalizedFormatters
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoundationAndLocalizationTest {
    @Test
    fun externalIntentFactoriesPreserveTypedPayloads() {
        val share = ExternalIntents.shareText(title = "Release", text = "Android Kit 1.0")
        assertEquals(Intent.ACTION_SEND, share.action)
        assertEquals("text/plain", share.type)
        assertEquals("Release", share.getStringExtra(Intent.EXTRA_TITLE))
        assertEquals("Android Kit 1.0", share.getStringExtra(Intent.EXTRA_TEXT))

        val email = ExternalIntents.email(
            address = "hello+kit@example.com",
            subject = "Hello & welcome",
            body = "Line one / line two",
        )
        assertEquals(Intent.ACTION_SENDTO, email.action)
        assertEquals("mailto", email.data?.scheme)
        assertArrayEquals(
            arrayOf("hello+kit@example.com"),
            email.getStringArrayExtra(Intent.EXTRA_EMAIL),
        )
        assertEquals("Hello & welcome", email.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals("Line one / line two", email.getStringExtra(Intent.EXTRA_TEXT))

        val dial = ExternalIntents.dial("+33 1 23 45 67 89")
        assertEquals(Intent.ACTION_DIAL, dial.action)
        assertEquals("tel", dial.data?.scheme)
        assertEquals("+33 1 23 45 67 89", dial.data?.schemeSpecificPart)
    }

    @Test
    fun formattersHonorTheirExplicitLocale() {
        val value = 1_234.5
        val french = LocalizedFormatters.number(value, Locale.FRANCE, maximumFractionDigits = 1)
        val english = LocalizedFormatters.number(value, Locale.US, maximumFractionDigits = 1)

        assertNotEquals(french, english)
        assertTrue(french.contains(DecimalFormatSymbols.getInstance(Locale.FRANCE).decimalSeparator))
        assertTrue(english.contains(DecimalFormatSymbols.getInstance(Locale.US).decimalSeparator))

        val euro = LocalizedFormatters.currency(value, "EUR", Locale.FRANCE)
        assertTrue(euro.contains(Currency.getInstance("EUR").getSymbol(Locale.FRANCE)))

        val date = LocalizedFormatters.date(
            value = LocalDate.of(2026, 8, 20),
            locale = Locale.FRANCE,
            style = FormatStyle.LONG,
        )
        assertTrue(date.contains("2026"))
    }

    @Test
    fun localeManagerCanonicalizesAndRejectsUnsupportedTags() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = AppLocaleManager(
            context = context,
            supportedLanguageTags = setOf("en-US", "fr", "ar"),
        )

        assertTrue(manager.isSupported("en-us"))
        assertTrue(manager.isSupported(" FR "))
        assertThrows(IllegalArgumentException::class.java) {
            manager.setApplicationLanguage("de")
        }
        assertThrows(IllegalArgumentException::class.java) {
            AppLocaleManager(context, setOf("%%%"))
        }
    }
}

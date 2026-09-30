package net.mamby.androidkit.testing

import android.content.Intent
import android.net.Uri
import android.icu.text.ListFormatter
import androidx.core.content.IntentCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale
import net.mamby.androidkit.foundation.ExternalIntents
import net.mamby.androidkit.localization.AppLocaleManager
import net.mamby.androidkit.localization.LocalizedFormatters
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertNull
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

        val query = "Café & library / 42?"
        val map = ExternalIntents.mapSearch(query)
        assertEquals(Intent.ACTION_VIEW, map.action)
        assertEquals("geo:0,0?q=${Uri.encode(query)}", map.dataString)
        val uri = Uri.parse("https://example.org/path?q=a%26b")
        assertEquals(uri, ExternalIntents.view(uri).data)
        val chooser = ExternalIntents.chooser(share, "Share release")
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        assertEquals("Share release", chooser.getStringExtra(Intent.EXTRA_TITLE))
        val target = IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java)
        assertEquals(share.action, target?.action)
        assertEquals("Android Kit 1.0", target?.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun formattersHonorTheirExplicitLocale() {
        val value = 1_234.5
        val date = LocalDate.of(2026, 8, 20)
        val time = LocalTime.of(16, 35)
        // Interleave locales, precision and styles to expose incorrect formatter cache keys.
        for (locale in listOf(Locale.FRANCE, Locale.US, Locale.FRANCE)) {
            for (digits in listOf(0, 2, 1)) {
                val expected = NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = digits }
                assertEquals(expected.format(value), LocalizedFormatters.number(value, locale, digits))
            }
            for (currency in listOf("EUR", "USD", "EUR")) {
                val expected = NumberFormat.getCurrencyInstance(locale).apply { this.currency = Currency.getInstance(currency) }
                assertEquals(expected.format(value), LocalizedFormatters.currency(value, currency, locale))
            }
            for (style in listOf(FormatStyle.LONG, FormatStyle.SHORT, FormatStyle.LONG)) {
                assertEquals(DateTimeFormatter.ofLocalizedDate(style).withLocale(locale).format(date),
                    LocalizedFormatters.date(date, locale, style))
            }
            assertEquals(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale).format(time),
                LocalizedFormatters.time(time, locale))
            val dateTime = LocalDateTime.of(date, time)
            assertEquals(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(locale).format(dateTime), LocalizedFormatters.dateTime(dateTime, locale))
            val items = listOf("Alpha", "Beta", "Gamma")
            assertEquals(ListFormatter.getInstance(locale).format(items), LocalizedFormatters.list(items, locale))
        }
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

    @Test
    fun applicationLocaleSelectionAndResetUseTheExplicitHostPolicy() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val manager = AppLocaleManager(instrumentation.targetContext, setOf("en-US", "fr"))
        // AppCompat's locale API requires a created host delegate, just like the demo.
        ActivityScenario.launch(LocaleHostActivity::class.java).use { scenario ->
            scenario.onActivity {
                val original = AppCompatDelegate.getApplicationLocales()
                try {
                    manager.setApplicationLanguage(" FR ")
                    assertEquals("fr", manager.selectedLanguageTag())
                    assertEquals(Locale.FRENCH, manager.effectiveLocale())
                    assertThrows(IllegalArgumentException::class.java) { manager.setApplicationLanguage("de") }
                    assertEquals("fr", manager.selectedLanguageTag())
                    manager.setApplicationLanguage(null)
                    assertNull(manager.selectedLanguageTag())
                    assertEquals(manager.systemLocale(), manager.effectiveLocale())
                } finally {
                    AppCompatDelegate.setApplicationLocales(original)
                }
            }
        }
    }
}

class LocaleHostActivity : AppCompatActivity()

package net.mamby.androidkit.localization

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.LocaleManagerCompat
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Thin, app-owned language selector backed by the official AppCompat per-app locale API.
 *
 * The consuming app remains responsible for declaring its supported locales and providing
 * translated resources.
 */
public class AppLocaleManager(
    context: Context,
    supportedLanguageTags: Set<String>,
) {
    private val applicationContext: Context = context.applicationContext
    private val supportedTags: Set<String> = supportedLanguageTags.mapTo(linkedSetOf(), ::canonicalTag)

    init {
        require(supportedTags.isNotEmpty()) { "At least one supported language tag is required." }
    }

    public fun selectedLanguageTag(): String? =
        AppCompatDelegate.getApplicationLocales()
            .get(0)
            ?.toLanguageTag()
            ?.let(::canonicalTag)

    public fun systemLocale(): Locale =
        LocaleManagerCompat.getSystemLocales(applicationContext).get(0) ?: Locale.getDefault()

    public fun effectiveLocale(): Locale =
        selectedLanguageTag()?.let(Locale::forLanguageTag) ?: systemLocale()

    public fun isSupported(languageTag: String): Boolean = canonicalTag(languageTag) in supportedTags

    public fun setApplicationLanguage(languageTag: String?) {
        val locales = languageTag?.let { requestedTag ->
            val canonical = canonicalTag(requestedTag)
            require(canonical in supportedTags) { "Unsupported application language: $requestedTag" }
            LocaleListCompat.forLanguageTags(canonical)
        } ?: LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(locales)
    }

    private companion object {
        private fun canonicalTag(languageTag: String): String {
            val locale = Locale.forLanguageTag(languageTag.trim())
            require(locale.language.isNotBlank()) { "Invalid language tag: $languageTag" }
            return locale.toLanguageTag()
        }
    }
}

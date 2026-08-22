package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.layout.DetailPage
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.presentation.LabeledValue
import net.mamby.androidkit.compose.presentation.SectionCard
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.localization.AppLocaleManager
import net.mamby.androidkit.localization.LocalizedFormatters
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

@Composable
fun LocalizationScreen() {
    PageScaffold(
        title = stringResource(R.string.localization_title),
    ) { contentPadding ->
        DetailPage(contentPadding = contentPadding) {
            LocalizationContent()
            repeat(LocalizationScrollExampleCount) { index ->
                SectionCard(
                    title = stringResource(R.string.scroll_section_title, index + 1),
                    supportingText = stringResource(R.string.scroll_section_description),
                ) {
                    Text(stringResource(R.string.scroll_section_body))
                }
            }
        }
    }
}

@Composable
internal fun LocalizationContent() {
    val context = LocalContext.current
    val resources = LocalResources.current
    val localeManager = remember(context) {
        AppLocaleManager(context, SupportedLanguageTags.toSet())
    }
    val selectedLanguageTag = localeManager.selectedLanguageTag()
    val locale = resources.configuration.locales[0]
    val dimensions = AndroidKitThemeTokens.dimensions

    SectionCard(
        title = stringResource(R.string.language_section),
        supportingText = stringResource(R.string.language_section_description),
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
        ) {
            LanguageChip(
                tag = null,
                label = stringResource(R.string.language_system),
                selectedTag = selectedLanguageTag,
                onSelected = localeManager::setApplicationLanguage,
            )
            SupportedLanguageTags.forEach { languageTag ->
                LanguageChip(
                    tag = languageTag,
                    label = nativeLanguageName(languageTag),
                    selectedTag = selectedLanguageTag,
                    onSelected = localeManager::setApplicationLanguage,
                )
            }
        }
    }
    SectionCard(
        title = stringResource(R.string.format_section),
        supportingText = stringResource(R.string.format_section_description),
    ) {
        LabeledValue(
            label = stringResource(R.string.format_date),
            value = LocalizedFormatters.date(LocalDate.of(2026, 8, 20), locale),
        )
        LabeledValue(
            label = stringResource(R.string.format_time),
            value = LocalizedFormatters.time(LocalTime.of(14, 30), locale),
        )
        LabeledValue(
            label = stringResource(R.string.format_number),
            value = LocalizedFormatters.number(1_234_567.89, locale),
        )
        LabeledValue(
            label = stringResource(R.string.format_currency),
            value = LocalizedFormatters.currency(42.50, "EUR", locale),
        )
        LabeledValue(
            label = stringResource(R.string.format_list),
            value = LocalizedFormatters.list(
                values = listOf(
                    stringResource(R.string.sample_coffee),
                    stringResource(R.string.sample_design),
                    stringResource(R.string.sample_code),
                ),
                locale = locale,
            ),
        )
    }
}

@Composable
private fun LanguageChip(
    tag: String?,
    label: String,
    selectedTag: String?,
    onSelected: (String?) -> Unit,
) {
    FilterChip(
        selected = selectedTag == tag,
        onClick = { onSelected(tag) },
        label = { Text(label) },
    )
}

private fun nativeLanguageName(languageTag: String): String =
    Locale.forLanguageTag(languageTag).let { locale ->
        locale.getDisplayLanguage(locale).replaceFirstChar { character ->
            if (character.isLowerCase()) character.titlecase() else character.toString()
        }
    }

private const val LocalizationScrollExampleCount = 5
private val SupportedLanguageTags: List<String> = listOf("en", "fr", "ar")

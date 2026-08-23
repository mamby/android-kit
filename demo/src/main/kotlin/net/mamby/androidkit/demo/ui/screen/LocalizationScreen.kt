package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.layout.PageScaffold
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
    val dimensions = AndroidKitThemeTokens.dimensions
    PageScaffold(title = stringResource(R.string.localization_title)) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.screenPadding),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        ) {
            item { LocalizationContent() }
            item { DemoScrollContent() }
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
    ) {
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
            listOf(
                stringResource(R.string.format_date) to
                    LocalizedFormatters.date(LocalDate.of(2026, 8, 20), locale),
                stringResource(R.string.format_time) to
                    LocalizedFormatters.time(LocalTime.of(14, 30), locale),
                stringResource(R.string.format_number) to
                    LocalizedFormatters.number(1_234_567.89, locale),
                stringResource(R.string.format_currency) to
                    LocalizedFormatters.currency(42.50, "EUR", locale),
                stringResource(R.string.format_list) to LocalizedFormatters.list(
                    values = listOf(
                        stringResource(R.string.sample_coffee),
                        stringResource(R.string.sample_design),
                        stringResource(R.string.sample_code),
                    ),
                    locale = locale,
                ),
            ).forEach { (label, value) ->
                ListItem(
                    headlineContent = { Text(value) },
                    overlineContent = { Text(label) },
                )
            }
        }
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

private val SupportedLanguageTags: List<String> = listOf("en", "fr", "ar")

package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.form.AndroidKitModalSheet
import net.mamby.androidkit.compose.layout.DetailPage
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.presentation.LabeledValue
import net.mamby.androidkit.compose.presentation.SectionCard
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.DemoThemeChoice
import net.mamby.androidkit.localization.AppLocaleManager
import net.mamby.androidkit.localization.LocalizedFormatters
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun SettingsScreen(
    themeChoice: DemoThemeChoice,
    onThemeChoice: (DemoThemeChoice) -> Unit,
) {
    var sheetVisible by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val resources = LocalResources.current
    val localeManager = remember(context) {
        AppLocaleManager(context, setOf("en", "fr", "ar"))
    }
    val selectedLanguageTag = localeManager.selectedLanguageTag()
    val locale = resources.configuration.locales[0]
    val dimensions = AndroidKitThemeTokens.dimensions
    PageScaffold(
        title = stringResource(R.string.settings_title),
        subtitle = stringResource(R.string.settings_subtitle),
    ) { contentPadding ->
        DetailPage(contentPadding = contentPadding) {
            SectionCard(
                title = stringResource(R.string.appearance_section),
                supportingText = stringResource(R.string.appearance_section_description),
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
                ) {
                    ThemeChip(
                        choice = DemoThemeChoice.Light,
                        label = stringResource(R.string.theme_light),
                        selected = themeChoice,
                        onSelected = onThemeChoice,
                    )
                    ThemeChip(
                        choice = DemoThemeChoice.Dark,
                        label = stringResource(R.string.theme_dark),
                        selected = themeChoice,
                        onSelected = onThemeChoice,
                    )
                    ThemeChip(
                        choice = DemoThemeChoice.Prism,
                        label = stringResource(R.string.theme_prism),
                        selected = themeChoice,
                        onSelected = onThemeChoice,
                    )
                }
                Text(stringResource(R.string.theme_prism_description))
            }
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
                    LanguageChip(
                        tag = "en",
                        label = stringResource(R.string.language_english),
                        selectedTag = selectedLanguageTag,
                        onSelected = localeManager::setApplicationLanguage,
                    )
                    LanguageChip(
                        tag = "fr",
                        label = stringResource(R.string.language_french),
                        selectedTag = selectedLanguageTag,
                        onSelected = localeManager::setApplicationLanguage,
                    )
                    LanguageChip(
                        tag = "ar",
                        label = stringResource(R.string.language_arabic),
                        selectedTag = selectedLanguageTag,
                        onSelected = localeManager::setApplicationLanguage,
                    )
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
            SectionCard(
                title = stringResource(R.string.sheet_section),
                supportingText = stringResource(R.string.sheet_section_description),
            ) {
                Button(onClick = { sheetVisible = true }) {
                    Text(stringResource(R.string.open_sheet))
                }
            }
            SectionCard(
                title = stringResource(R.string.about_section),
                supportingText = stringResource(R.string.about_body),
            ) {}
        }
    }

    if (sheetVisible) {
        AndroidKitModalSheet(
            title = stringResource(R.string.sheet_title),
            onDismissRequest = { sheetVisible = false },
        ) {
            Text(stringResource(R.string.sheet_body))
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

@Composable
private fun ThemeChip(
    choice: DemoThemeChoice,
    label: String,
    selected: DemoThemeChoice,
    onSelected: (DemoThemeChoice) -> Unit,
) {
    FilterChip(
        selected = selected == choice,
        onClick = { onSelected(choice) },
        label = { Text(label) },
    )
}

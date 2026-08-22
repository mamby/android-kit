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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.form.AndroidKitModalSheet
import net.mamby.androidkit.compose.form.SwitchField
import net.mamby.androidkit.compose.layout.DetailPage
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.presentation.LabeledValue
import net.mamby.androidkit.compose.presentation.SectionCard
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.DemoThemeChoice

@Composable
fun SettingsScreen(
    themeChoice: DemoThemeChoice,
    onThemeChoice: (DemoThemeChoice) -> Unit,
    floatingSurfacesTransparent: Boolean,
    onFloatingSurfacesTransparent: (Boolean) -> Unit,
) {
    var sheetVisible by rememberSaveable { mutableStateOf(false) }
    val dimensions = AndroidKitThemeTokens.dimensions
    PageScaffold(
        title = stringResource(R.string.settings_title),
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
                SwitchField(
                    title = stringResource(R.string.floating_surface_transparency),
                    supportingText = stringResource(
                        R.string.floating_surface_transparency_description,
                    ),
                    checked = floatingSurfacesTransparent,
                    onCheckedChange = onFloatingSurfacesTransparent,
                )
            }
            LocalizationContent()
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
            SectionCard(
                title = stringResource(R.string.settings_scroll_section),
                supportingText = stringResource(R.string.settings_scroll_description),
            ) {
                repeat(4) { index ->
                    LabeledValue(
                        label = stringResource(R.string.settings_scroll_item, index + 1),
                        value = stringResource(R.string.settings_scroll_value),
                    )
                }
            }
            ScrollTestContent()
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

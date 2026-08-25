package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlin.math.roundToInt
import net.mamby.androidkit.compose.form.AndroidKitSettingSection
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.presentation.AndroidKitCard
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.DemoThemeChoice
import net.mamby.androidkit.demo.ui.MaximumFloatingSurfaceOpacity
import net.mamby.androidkit.demo.ui.MinimumFloatingSurfaceOpacity

@Composable
fun SettingsScreen(
    themeChoice: DemoThemeChoice,
    onThemeChoice: (DemoThemeChoice) -> Unit,
    floatingSurfaceOpacity: Float,
    onFloatingSurfaceOpacityChange: (Float) -> Unit,
    onFloatingSurfaceOpacityChangeFinished: () -> Unit,
) {
    val dimensions = AndroidKitThemeTokens.dimensions
    AndroidKitPage(title = stringResource(R.string.settings_title)) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.screenPadding),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium),
        ) {
            item {
                AndroidKitCard(
                    modifier = Modifier.fillMaxWidth(),
                    header = {
                        DemoCardHeader(
                            title = stringResource(R.string.appearance_section),
                            supportingText = stringResource(
                                R.string.appearance_section_description,
                            ),
                        )
                    },
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
            }
            item {
                val opacityLabel = stringResource(R.string.floating_surface_opacity)
                val opacityValueLabel = stringResource(
                    R.string.floating_surface_opacity_value,
                    (floatingSurfaceOpacity * PercentageScale).roundToInt(),
                )
                AndroidKitSettingSection(
                    description = stringResource(
                        R.string.floating_surface_opacity_description,
                    ),
                ) {
                    slider(
                        label = opacityLabel,
                        value = floatingSurfaceOpacity,
                        onValueChange = onFloatingSurfaceOpacityChange,
                        valueRange = MinimumFloatingSurfaceOpacity..MaximumFloatingSurfaceOpacity,
                        steps = FloatingSurfaceOpacitySliderSteps,
                        onValueChangeFinished = onFloatingSurfaceOpacityChangeFinished,
                        valueLabel = opacityValueLabel,
                    )
                }
            }
            item {
                AndroidKitCard(
                    modifier = Modifier.fillMaxWidth(),
                    header = {
                        DemoCardHeader(
                            title = stringResource(R.string.about_section),
                            supportingText = stringResource(R.string.about_body),
                        )
                    },
                ) {}
            }
            item { DemoScrollContent() }
        }
    }
}

private const val FloatingSurfaceOpacitySliderSteps = 99
private const val PercentageScale = 100

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

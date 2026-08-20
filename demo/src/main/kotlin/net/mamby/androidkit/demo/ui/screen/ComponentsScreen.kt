package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.action.FloatingAddButton
import net.mamby.androidkit.compose.layout.AdaptiveGridPage
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.presentation.MetricCard
import net.mamby.androidkit.compose.presentation.PresentationKind
import net.mamby.androidkit.compose.presentation.SectionCard
import net.mamby.androidkit.compose.presentation.StatePresentation
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R

@Composable
fun ComponentsScreen() {
    var additions by rememberSaveable { mutableIntStateOf(0) }
    var presentationKind by rememberSaveable { mutableStateOf(PresentationKind.Empty) }
    PageScaffold(
        title = stringResource(R.string.components_title),
        subtitle = stringResource(R.string.components_subtitle),
        floatingActionButton = {
            FloatingAddButton(onClick = { additions += 1 })
        },
    ) { contentPadding ->
        AdaptiveGridPage(contentPadding = contentPadding) {
            item {
                SectionCard(
                    title = stringResource(R.string.components_buttons_title),
                    supportingText = stringResource(R.string.components_buttons_description),
                ) {
                    Button(onClick = { presentationKind = PresentationKind.Loading }) {
                        Text(stringResource(R.string.primary_action))
                    }
                    OutlinedButton(onClick = { presentationKind = PresentationKind.Empty }) {
                        Text(stringResource(R.string.secondary_action))
                    }
                    FilledTonalButton(onClick = { presentationKind = PresentationKind.Error }) {
                        Text(stringResource(R.string.tonal_action))
                    }
                }
            }
            item {
                SectionCard(
                    title = stringResource(R.string.components_states_title),
                    supportingText = stringResource(R.string.components_states_description),
                ) {
                    StatePresentation(
                        kind = presentationKind,
                        title = stringResource(R.string.empty_title),
                        message = stringResource(R.string.empty_message),
                        actionLabel = stringResource(R.string.action_retry),
                        onAction = { presentationKind = PresentationKind.Loading },
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionCard(title = stringResource(R.string.components_metrics_title)) {
                    val spacing = AndroidKitThemeTokens.dimensions.spaceMedium
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                    ) {
                        MetricCard(
                            value = (24 + additions).toString(),
                            label = stringResource(R.string.component_count_label),
                            modifier = Modifier.weight(1f),
                        )
                        MetricCard(
                            value = stringResource(R.string.theme_count),
                            label = stringResource(R.string.theme_count_label),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

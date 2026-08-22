package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.layout.AdaptiveGridPage
import net.mamby.androidkit.compose.layout.DetailPage
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.presentation.SectionCard
import net.mamby.androidkit.compose.theme.AndroidKitCardDefaults
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.navigation3.listDetailBackAction

private data class LayoutSample(
    val id: Int,
    val title: String,
    val body: String,
)

@Composable
private fun layoutSamples(): List<LayoutSample> {
    val baseSamples = listOf(
        stringResource(R.string.layout_dashboard) to stringResource(R.string.layout_dashboard_body),
        stringResource(R.string.layout_library) to stringResource(R.string.layout_library_body),
        stringResource(R.string.layout_settings) to stringResource(R.string.layout_settings_body),
    )
    return List(LayoutSampleCount) { index ->
        val (title, body) = baseSamples[index % baseSamples.size]
        LayoutSample(
            id = index,
            title = stringResource(R.string.layout_numbered_title, title, index + 1),
            body = body,
        )
    }
}

@Composable
fun LayoutsScreen(onSelected: (Int) -> Unit) {
    val samples = layoutSamples()
    val dimensions = AndroidKitThemeTokens.dimensions
    PageScaffold(
        title = stringResource(R.string.layouts_title),
    ) { contentPadding ->
        AdaptiveGridPage(contentPadding = contentPadding) {
            items(samples.size, key = { samples[it].id }) { index ->
                val sample = samples[index]
                Column(verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall)) {
                    Text(
                        text = sample.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Card(
                        onClick = { onSelected(sample.id) },
                        colors = AndroidKitCardDefaults.colors(),
                        border = AndroidKitCardDefaults.border(),
                    ) {
                        Text(
                            text = sample.body,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensions.spaceMedium),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            scrollTestContent()
        }
    }
}

@Composable
fun LayoutPlaceholder() {
    val dimensions = AndroidKitThemeTokens.dimensions
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensions.spaceExtraLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
        ) {
            Text(
                text = stringResource(R.string.detail_placeholder_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.detail_placeholder_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun LayoutDetailScreen(sampleId: Int, onBack: () -> Unit) {
    val sample = layoutSamples().firstOrNull { it.id == sampleId } ?: layoutSamples().first()
    val backAction = listDetailBackAction(onBack)
    PageScaffold(
        title = sample.title,
        onBack = backAction,
    ) { contentPadding ->
        DetailPage(contentPadding = contentPadding) {
            SectionCard(
                title = stringResource(R.string.detail_behavior_title),
                supportingText = stringResource(R.string.detail_behavior_body),
            ) {}
            SectionCard(
                title = stringResource(R.string.detail_tokens_title),
                supportingText = stringResource(R.string.detail_tokens_body),
            ) {}
            SectionCard(
                title = stringResource(R.string.detail_scrolling_title),
                supportingText = stringResource(R.string.detail_scrolling_body),
            ) {}
            SectionCard(
                title = stringResource(R.string.detail_accessibility_title),
                supportingText = stringResource(R.string.detail_accessibility_body),
            ) {}
            SectionCard(
                title = stringResource(R.string.detail_window_title),
                supportingText = stringResource(R.string.detail_window_body),
            ) {}
            SectionCard(
                title = stringResource(R.string.detail_navigation_title),
                supportingText = stringResource(R.string.detail_navigation_body),
            ) {}
            ScrollTestContent()
        }
    }
}

private const val LayoutSampleCount = 12

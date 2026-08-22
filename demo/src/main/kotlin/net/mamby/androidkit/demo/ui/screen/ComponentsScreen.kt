package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import net.mamby.androidkit.compose.layout.AdaptiveGridPage
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.theme.AndroidKitCardDefaults
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.ComponentCategory
import net.mamby.androidkit.demo.ui.ComponentId

@Composable
fun ComponentsScreen(onSelected: (ComponentId) -> Unit) {
    val dimensions = AndroidKitThemeTokens.dimensions
    PageScaffold { contentPadding ->
        AdaptiveGridPage(contentPadding = contentPadding) {
            item(
                key = R.string.home_title,
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Text(
                    text = stringResource(R.string.home_title),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            ComponentCategory.entries.forEach { category ->
                val components = ComponentId.entries.filter { it.category == category }
                item(
                    key = category.name,
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    Text(
                        text = stringResource(category.labelResource),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                items(
                    count = components.size,
                    key = { components[it].name },
                ) { index ->
                    val component = components[index]
                    Card(
                        onClick = { onSelected(component) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AndroidKitCardDefaults.colors(),
                        border = AndroidKitCardDefaults.border(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensions.spaceMedium),
                            verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = component.apiName,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                )
                            }
                            Text(
                                text = stringResource(component.descriptionResource),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

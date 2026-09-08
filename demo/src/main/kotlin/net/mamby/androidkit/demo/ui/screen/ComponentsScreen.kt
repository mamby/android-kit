package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.theme.AndroidKitCardDefaults
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.ComponentDemo
import net.mamby.androidkit.demo.ui.ComponentId
import net.mamby.androidkit.demo.ui.materialSymbol

@Composable
fun ComponentsScreen(onSelected: (ComponentDemo) -> Unit) {
    val dimensions = AndroidKitThemeTokens.dimensions
    AndroidKitPage { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("catalog_list")
                .padding(horizontal = dimensions.screenPadding),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
        ) {
            ComponentId.entries.forEach { component ->
                items(
                    items = listOf(ComponentDemo.entries.first { it.component == component }),
                    key = ComponentDemo::name,
                ) { demo ->
                    Card(
                        onClick = { onSelected(demo) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("component_demo_${demo.name.lowercase()}"),
                        colors = AndroidKitCardDefaults.colors(),
                        border = AndroidKitCardDefaults.border(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensions.spaceMedium),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = component.catalogName,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Icon(
                                imageVector = materialSymbol(R.drawable.ic_symbol_arrow_forward),
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
            item { DemoScrollContent() }
        }
    }
}

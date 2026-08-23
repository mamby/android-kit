package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.theme.AndroidKitCardDefaults
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.ComponentDemo
import net.mamby.androidkit.demo.ui.ComponentId

@Composable
fun ComponentsScreen(onSelected: (ComponentDemo) -> Unit) {
    val dimensions = AndroidKitThemeTokens.dimensions
    PageScaffold { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.screenPadding),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
        ) {
            ComponentId.entries.forEach { component ->
                item(key = component.name) {
                    Text(
                        text = component.apiName,
                        modifier = Modifier
                            .padding(top = dimensions.spaceMedium)
                            .semantics { heading() },
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                items(
                    items = ComponentDemo.entries.filter { it.component == component },
                    key = ComponentDemo::name,
                ) { demo ->
                    Card(
                        onClick = { onSelected(demo) },
                        modifier = Modifier.fillMaxWidth(),
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
                                text = stringResource(demo.titleResource),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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

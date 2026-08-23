package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.layout.PageScaffold
import net.mamby.androidkit.compose.presentation.AndroidKitCard
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R

@Composable
fun DummyNavigationScreen(index: Int) {
    val dimensions = AndroidKitThemeTokens.dimensions
    PageScaffold(title = stringResource(R.string.nav_demo, index)) { contentPadding ->
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
                            title = stringResource(R.string.nav_demo, index),
                            supportingText = stringResource(R.string.dummy_navigation_body),
                        )
                    },
                ) {
                    Text(stringResource(R.string.dummy_navigation_instruction))
                }
            }
            item { DemoScrollContent() }
        }
    }
}

package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R

@Composable
internal fun DemoScrollContent(modifier: Modifier = Modifier) {
    val dimensions = AndroidKitThemeTokens.dimensions
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = dimensions.spaceExtraLarge),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Text(
            text = stringResource(R.string.demo_scroll_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

package net.mamby.androidkit.demo.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun DemoCardHeader(
    title: String,
    supportingText: String? = null,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
    )
    supportingText?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

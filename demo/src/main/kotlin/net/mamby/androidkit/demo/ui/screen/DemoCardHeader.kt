package net.mamby.androidkit.demo.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal fun DemoCardHeader(
    title: String,
    supportingText: String? = null,
) {
    DemoCardHeaderTitle(title)
    supportingText?.let { DemoCardHeaderSupportingContent(it) }
}

@Composable
internal fun DemoCardHeaderTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
internal fun DemoCardHeaderSupportingContent(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

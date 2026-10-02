package net.mamby.androidkit.compose.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.mamby.androidkit.compose.form.AndroidKitBottomSheet
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitCardStyle
import net.mamby.androidkit.compose.theme.AndroidKitComponentTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

/** Host policy supplies a new [id] for each eligible presentation. Callbacks do not confirm payment. */
public class AndroidKitSupportPrompt(
    public val id: String,
    public val onDonate: () -> Unit,
    public val onDismiss: () -> Unit,
    public val enabled: Boolean = true,
) {
    init { require(id.isNotBlank()) { "Support prompt ID must not be blank." } }
}

@Composable
internal fun SupportPromptCard(enabled: Boolean, style: AndroidKitCardStyle, onLearnMore: () -> Unit, onDismiss: () -> Unit) {
    AndroidKitComponentTheme {
        val tokens = AndroidKitThemeTokens.componentTokens.supportPrompt
        val strings = AndroidKitThemeTokens.strings
        AndroidKitCard(
            modifier = Modifier.fillMaxWidth(), style = style,
            title = strings.supportPromptTitle, supportingText = strings.supportPromptDescription,
        ) {
            Icon(AndroidKitIcons.Support, contentDescription = null)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(tokens.actionSpacing)) {
                TextButton(onClick = onLearnMore, enabled = enabled) { Text(strings.supportPromptLearnMore) }
                TextButton(onClick = onDismiss) { Text(strings.supportPromptNotNow) }
            }
        }

    }
}

@Composable
internal fun SupportPromptSheet(enabled: Boolean, onDonate: () -> Unit, onDismiss: () -> Unit) {
    AndroidKitComponentTheme {
        val tokens = AndroidKitThemeTokens.componentTokens.supportPrompt
        val strings = AndroidKitThemeTokens.strings
        AndroidKitBottomSheet(visible = true, title = strings.supportPromptTitle, onDismiss = onDismiss, fitContent = true) {
            Text(strings.supportPromptDescription, style = tokens.descriptionTextStyle)
            Spacer(Modifier.height(tokens.donateSpacing))
            Button(onClick = onDonate, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text(strings.supportPromptDonate) }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(strings.supportPromptNotNow) }
        }

    }
}

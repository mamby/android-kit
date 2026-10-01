package net.mamby.androidkit.compose.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import net.mamby.androidkit.compose.theme.AndroidKitSettingSectionStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@Composable
internal fun SectionCardEntryContent(
    label: String,
    supportingText: String?,
    icon: ImageVector?,
    modifier: Modifier,
    style: AndroidKitSettingSectionStyle,
    fillTextWidth: Boolean = false,
    trailingContent: @Composable () -> Unit,
): Unit {
    val tokens = AndroidKitThemeTokens.componentTokens.sectionCard
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(tokens.entryContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(tokens.supportingTextSpacing),
        ) {
            Text(
                text = label,
                modifier = if (fillTextWidth) Modifier.fillMaxWidth() else Modifier,
                style = style.entryLabelTextStyle,
            )
            supportingText?.let {
                Text(
                    text = it,
                    modifier = if (fillTextWidth) Modifier.fillMaxWidth() else Modifier,
                    style = style.supportingTextStyle,
                    color = style.secondaryContentColor,
                )
            }
        }
        trailingContent()
    }
}

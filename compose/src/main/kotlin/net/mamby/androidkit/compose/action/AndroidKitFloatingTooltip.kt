package net.mamby.androidkit.compose.action

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import net.mamby.androidkit.compose.theme.AndroidKitDefaults
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface

/** Host-owned action data; Kit owns its rendering and placement. */
public data class AndroidKitFloatingTooltipAction(
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

/**
 * Sealed floating tooltip content for a Material TooltipBox.
 * The anchor owns visibility and placement. Kit owns shape, typography, action rendering,
 * and the localized Close control. Shared floating-surface tokens supply shadow and transparency.
 * Action callbacks own their work and dismissal; pass onDismiss to show the Close control.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun TooltipScope.AndroidKitFloatingTooltip(
    text: String,
    modifier: Modifier = Modifier,
    action: AndroidKitFloatingTooltipAction? = null,
    onDismiss: (() -> Unit)? = null,
): Unit {
    val shape = AndroidKitDefaults.shapes.extraLarge
    val dimensions = AndroidKitThemeTokens.dimensions
    val strings = AndroidKitThemeTokens.strings
    val hasActions = action != null || onDismiss != null
    FloatingSurface(shape = shape,
        modifier = modifier.widthIn(max = TooltipDefaults.richTooltipMaxWidth)) {
        Column(
            // Buttons already include vertical space around their labels and a full touch target.
            modifier = Modifier.padding(
                start = dimensions.spaceMedium,
                top = dimensions.spaceMedium,
                end = dimensions.spaceMedium,
                bottom = if (hasActions) dimensions.spaceExtraSmall else dimensions.spaceMedium,
            ),
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
        ) {
            Text(text, style = AndroidKitThemeTokens.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            if (hasActions) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spaceSmall),
                ) {
                    action?.let {
                        TextButton(enabled = it.enabled, onClick = it.onClick) {
                            Text(it.label, style = AndroidKitThemeTokens.typography.labelLarge)
                        }
                    }
                    onDismiss?.let {
                        TextButton(onClick = it) {
                            Text(strings.close, style = AndroidKitThemeTokens.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

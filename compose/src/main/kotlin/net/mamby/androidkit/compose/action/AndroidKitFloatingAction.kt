package net.mamby.androidkit.compose.action

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.AndroidKitFloatingActionButtonStyle
import net.mamby.androidkit.compose.theme.AndroidKitFloatingActionBarStyle

/** Data for floating chrome. AndroidKit exclusively renders its controls. */
public sealed interface AndroidKitFloatingAction {
    public class Button private constructor(
        internal val vector: ImageVector?,
        internal val painter: Painter?,
        public val label: String,
        public val onClick: () -> Unit,
        public val enabled: Boolean,
        public val modifier: Modifier,
        public val style: AndroidKitFloatingActionButtonStyle?,
        public val tooltip: String?,
    ) : AndroidKitFloatingAction {
        public constructor(icon: ImageVector, label: String, onClick: () -> Unit,
            enabled: Boolean = true, modifier: Modifier = Modifier,
            style: AndroidKitFloatingActionButtonStyle? = null,
            tooltip: String? = null,
        ) : this(icon, null, label, onClick, enabled, modifier, style, tooltip)

        public constructor(icon: Painter, label: String, onClick: () -> Unit,
            enabled: Boolean = true, modifier: Modifier = Modifier,
            style: AndroidKitFloatingActionButtonStyle? = null,
            tooltip: String? = null,
        ) : this(null, icon, label, onClick, enabled, modifier, style, tooltip)
    }

    public class Bar(
        public val modifier: Modifier = Modifier,
        public val style: AndroidKitFloatingActionBarStyle? = null,
        internal val content: AndroidKitFloatingActionBarScope.() -> Unit,
    ) : AndroidKitFloatingAction
}

@Composable
internal fun RenderFloatingAction(action: AndroidKitFloatingAction?) {
    when (action) {
        null -> Unit
        is AndroidKitFloatingAction.Button -> AndroidKitFloatingActionButton(action)
        is AndroidKitFloatingAction.Bar -> AndroidKitFloatingActionBar(
            modifier = action.modifier,
            style = action.style ?: AndroidKitThemeTokens.floatingActionBarStyle,
            content = action.content,
        )
    }
}

@Composable
internal fun FloatingActionIcon(action: AndroidKitFloatingAction.Button) {
    val modifier = Modifier.size(AndroidKitThemeTokens.dimensions.floatingActionIconSize)
    if (action.vector != null) {
        Icon(action.vector, action.label, modifier)
    } else {
        Icon(requireNotNull(action.painter), action.label, modifier)
    }
}

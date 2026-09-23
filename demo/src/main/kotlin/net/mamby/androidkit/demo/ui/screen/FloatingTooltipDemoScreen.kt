package net.mamby.androidkit.demo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.mamby.androidkit.compose.action.AndroidKitFloatingTooltip
import net.mamby.androidkit.compose.action.AndroidKitFloatingTooltipAction
import net.mamby.androidkit.compose.layout.AndroidKitPage
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.demo.R
import net.mamby.androidkit.demo.ui.ComponentId
import net.mamby.androidkit.navigation3.listDetailBackAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FloatingTooltipDemoScreen(onBack: () -> Unit) {
    var actions by rememberSaveable { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val dimensions = AndroidKitThemeTokens.dimensions
    AndroidKitPage(title = ComponentId.AndroidKitFloatingTooltip.apiName,
        onBack = listDetailBackAction(onBack)) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = dimensions.screenPadding),
            verticalArrangement = Arrangement.spacedBy(dimensions.spaceMedium)) {
            Text(stringResource(R.string.action_count, actions))
            for (interactive in listOf(false, true)) {
                val state = rememberTooltipState(isPersistent = interactive)
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    state = state,
                    focusable = interactive,
                    tooltip = {
                        AndroidKitFloatingTooltip(
                            text = stringResource(R.string.floating_tooltip_demo_message),
                            action = if (interactive) AndroidKitFloatingTooltipAction(
                                label = stringResource(R.string.action_confirm),
                                onClick = { actions++; state.dismiss() },
                            ) else null,
                            onDismiss = if (interactive) ({ state.dismiss() }) else null,
                        )
                    },
                ) {
                    Button(onClick = { scope.launch { state.show() } }) {
                        Text(stringResource(if (interactive) R.string.variation_interactive else R.string.variation_text_only))
                    }
                }
            }
        }
    }
}

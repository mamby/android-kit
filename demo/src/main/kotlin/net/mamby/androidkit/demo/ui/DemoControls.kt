package net.mamby.androidkit.demo.ui

import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import net.mamby.androidkit.demo.R

internal enum class DemoToggle(@StringRes val label: Int, val defaultValue: Boolean = false) {
    FabEnabled(R.string.component_enabled, true),
    TopStart(R.string.variation_top_start),
    TopCenter(R.string.variation_top_center),
    TopEnd(R.string.variation_top_end),
    BottomStart(R.string.variation_bottom_start),
    BottomCenter(R.string.variation_bottom_center),
    BottomEnd(R.string.variation_bottom_end, true),
    PageTitle(R.string.variation_with_title, true),
    PageActions(R.string.variation_back_title_actions),
    PageImmersive(R.string.variation_immersive_mode),
    PageFab(R.string.variation_with_floating_action_button),
}

internal val fabPositions = mapOf(
    DemoToggle.TopStart to Alignment.TopStart,
    DemoToggle.TopCenter to Alignment.TopCenter,
    DemoToggle.TopEnd to Alignment.TopEnd,
    DemoToggle.BottomStart to Alignment.BottomStart,
    DemoToggle.BottomCenter to Alignment.BottomCenter,
    DemoToggle.BottomEnd to Alignment.BottomEnd,
)

internal enum class DemoPageAction(@StringRes val label: Int, val icon: Int) {
    Save(R.string.action_save, R.drawable.ic_symbol_save),
    Share(R.string.action_share, R.drawable.ic_symbol_share),
    Edit(R.string.action_edit, R.drawable.ic_symbol_edit),
    Retry(R.string.action_retry, R.drawable.ic_symbol_refresh),
    Delete(R.string.action_delete, R.drawable.ic_symbol_delete),
    Confirm(R.string.action_confirm, R.drawable.ic_symbol_check),
}

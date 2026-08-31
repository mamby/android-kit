package net.mamby.androidkit.demo.ui

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import net.mamby.androidkit.demo.R

@Serializable
enum class ComponentId(
    val apiName: String,
) {
    AndroidKitPage("AndroidKitPage"),
    AndroidKitFloatingActionButton("AndroidKitFloatingActionButton"),
    AndroidKitCard("AndroidKitCard"),
    AndroidKitSettingSection("AndroidKitSettingSection"),
    AndroidKitBottomSheet("AndroidKitBottomSheet"),
    AndroidKitFloatingActionBar("AndroidKitFloatingActionBar"),
    AndroidKitFloatingDropdownMenu("AndroidKitFloatingDropdownMenu"),
    AndroidKitFloatingNavigation("AndroidKitFloatingNavigation"),
    ;

    val catalogName: String
        get() = apiName.removePrefix("AndroidKit")
}

@Serializable
enum class ComponentDemo(
    val component: ComponentId,
    @StringRes val titleResource: Int,
) {
    AndroidKitPageBasic(ComponentId.AndroidKitPage, R.string.variation_basic),
    AndroidKitPageTitle(ComponentId.AndroidKitPage, R.string.variation_with_title),
    AndroidKitPageTitleActions(
        ComponentId.AndroidKitPage,
        R.string.variation_back_title_actions,
    ),
    AndroidKitPageImmersiveMode(
        ComponentId.AndroidKitPage,
        R.string.variation_immersive_mode,
    ),
    AndroidKitPageFloatingActionButton(
        ComponentId.AndroidKitPage,
        R.string.variation_with_floating_action_button,
    ),

    AndroidKitFloatingActionButtonTopStart(
        ComponentId.AndroidKitFloatingActionButton,
        R.string.variation_top_start,
    ),
    AndroidKitFloatingActionButtonTopCenter(
        ComponentId.AndroidKitFloatingActionButton,
        R.string.variation_top_center,
    ),
    AndroidKitFloatingActionButtonTopEnd(
        ComponentId.AndroidKitFloatingActionButton,
        R.string.variation_top_end,
    ),
    AndroidKitFloatingActionButtonBottomStart(
        ComponentId.AndroidKitFloatingActionButton,
        R.string.variation_bottom_start,
    ),
    AndroidKitFloatingActionButtonBottomCenter(
        ComponentId.AndroidKitFloatingActionButton,
        R.string.variation_bottom_center,
    ),
    AndroidKitFloatingActionButtonBottomEnd(
        ComponentId.AndroidKitFloatingActionButton,
        R.string.variation_bottom_end,
    ),

    AndroidKitCardBasic(ComponentId.AndroidKitCard, R.string.variation_basic),
    AndroidKitCardSupportingText(ComponentId.AndroidKitCard, R.string.variation_supporting_text),
    AndroidKitCardRichContent(ComponentId.AndroidKitCard, R.string.variation_rich_content),
    AndroidKitCardOverflow(ComponentId.AndroidKitCard, R.string.variation_overflow),

    AndroidKitSettingSectionButton(
        ComponentId.AndroidKitSettingSection,
        R.string.variation_button_entry,
    ),
    AndroidKitSettingSectionToggle(
        ComponentId.AndroidKitSettingSection,
        R.string.variation_toggle_entry,
    ),
    AndroidKitSettingSectionGrouped(
        ComponentId.AndroidKitSettingSection,
        R.string.variation_grouped_entries,
    ),

    AndroidKitBottomSheetStandard(
        ComponentId.AndroidKitBottomSheet,
        R.string.variation_standard,
    ),
    AndroidKitBottomSheetBackNavigation(
        ComponentId.AndroidKitBottomSheet,
        R.string.variation_back_navigation,
    ),
    AndroidKitBottomSheetChromelessFitContent(
        ComponentId.AndroidKitBottomSheet,
        R.string.variation_chromeless_fit_content,
    ),
    AndroidKitBottomSheetContentManaged(
        ComponentId.AndroidKitBottomSheet,
        R.string.variation_content_managed,
    ),

    AndroidKitFloatingActionBarIcons(
        ComponentId.AndroidKitFloatingActionBar,
        R.string.variation_icons_only,
    ),
    AndroidKitFloatingActionBarIconsAndLabels(
        ComponentId.AndroidKitFloatingActionBar,
        R.string.variation_icons_labels,
    ),
    AndroidKitFloatingActionBarText(
        ComponentId.AndroidKitFloatingActionBar,
        R.string.variation_text_only,
    ),

    AndroidKitFloatingDropdownMenuText(
        ComponentId.AndroidKitFloatingDropdownMenu,
        R.string.variation_text_items,
    ),
    AndroidKitFloatingDropdownMenuIcons(
        ComponentId.AndroidKitFloatingDropdownMenu,
        R.string.variation_icon_items,
    ),

    AndroidKitFloatingNavigationInteractive(
        ComponentId.AndroidKitFloatingNavigation,
        R.string.variation_interactive,
    ),
}

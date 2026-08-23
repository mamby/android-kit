package net.mamby.androidkit.demo.ui

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import net.mamby.androidkit.demo.R

@Serializable
enum class ComponentId(
    val apiName: String,
) {
    PageScaffold("PageScaffold"),
    FloatingTitleBar("FloatingTitleBar"),
    FloatingButton("FloatingButton"),
    SectionCard("SectionCard"),
    SettingsItem("SettingsItem"),
    AndroidKitModalSheet("AndroidKitModalSheet"),
    FloatingActionBar("FloatingActionBar"),
    FloatingDropdownMenu("FloatingDropdownMenu"),
    FloatingNavigation("FloatingNavigation"),
}

@Serializable
enum class ComponentDemo(
    val component: ComponentId,
    @StringRes val titleResource: Int,
) {
    PageScaffoldBasic(ComponentId.PageScaffold, R.string.variation_basic),
    PageScaffoldTitle(ComponentId.PageScaffold, R.string.variation_with_title),
    PageScaffoldFloatingButton(
        ComponentId.PageScaffold,
        R.string.variation_with_floating_button,
    ),

    FloatingTitleBarBackOnly(ComponentId.FloatingTitleBar, R.string.variation_back_only),
    FloatingTitleBarBackTitle(ComponentId.FloatingTitleBar, R.string.variation_back_title),
    FloatingTitleBarBackTitleActions(
        ComponentId.FloatingTitleBar,
        R.string.variation_back_title_actions,
    ),
    FloatingTitleBarImmersiveMode(ComponentId.FloatingTitleBar, R.string.variation_immersive_mode),

    FloatingButtonTopStart(ComponentId.FloatingButton, R.string.variation_top_start),
    FloatingButtonTopCenter(ComponentId.FloatingButton, R.string.variation_top_center),
    FloatingButtonTopEnd(ComponentId.FloatingButton, R.string.variation_top_end),
    FloatingButtonBottomStart(ComponentId.FloatingButton, R.string.variation_bottom_start),
    FloatingButtonBottomCenter(ComponentId.FloatingButton, R.string.variation_bottom_center),
    FloatingButtonBottomEnd(ComponentId.FloatingButton, R.string.variation_bottom_end),

    SectionCardBasic(ComponentId.SectionCard, R.string.variation_basic),
    SectionCardSupportingText(ComponentId.SectionCard, R.string.variation_supporting_text),
    SectionCardRichContent(ComponentId.SectionCard, R.string.variation_rich_content),

    SettingsItemBasic(ComponentId.SettingsItem, R.string.variation_basic),
    SettingsItemSupportingText(ComponentId.SettingsItem, R.string.variation_supporting_text),
    SettingsItemAccessories(ComponentId.SettingsItem, R.string.variation_leading_trailing),

    ModalSheetTitleless(ComponentId.AndroidKitModalSheet, R.string.variation_titleless),
    ModalSheetTitled(ComponentId.AndroidKitModalSheet, R.string.variation_with_title),
    ModalSheetBackAndActions(
        ComponentId.AndroidKitModalSheet,
        R.string.variation_back_title_actions,
    ),

    FloatingActionBarIcons(ComponentId.FloatingActionBar, R.string.variation_icons_only),
    FloatingActionBarIconsAndLabels(
        ComponentId.FloatingActionBar,
        R.string.variation_icons_labels,
    ),
    FloatingActionBarText(ComponentId.FloatingActionBar, R.string.variation_text_only),
    FloatingActionBarWithFlyout(ComponentId.FloatingActionBar, R.string.variation_with_flyout),

    FloatingDropdownMenuText(ComponentId.FloatingDropdownMenu, R.string.variation_text_items),
    FloatingDropdownMenuIcons(ComponentId.FloatingDropdownMenu, R.string.variation_icon_items),

    FloatingNavigationLabels(ComponentId.FloatingNavigation, R.string.variation_label_toggle),
    FloatingNavigationOverflow(ComponentId.FloatingNavigation, R.string.variation_overflow),
}

package net.mamby.androidkit.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Color overrides only. Unspecified values inherit the active Kit theme. */
@Immutable
public data class AndroidKitFloatingSurfaceColors(
    public val containerColor: Color = Color.Unspecified,
    public val contentColor: Color = Color.Unspecified,
    public val borderColor: Color = Color.Unspecified,
    public val shadowColor: Color = Color.Unspecified,
    public val disabledContainerColor: Color = Color.Unspecified,
    public val disabledContentColor: Color = Color.Unspecified,
    public val disabledBorderColor: Color = Color.Unspecified,
)

@Immutable
public data class AndroidKitPageColors(
    public val containerColor: Color = Color.Unspecified,
    public val contentProtectionColor: Color = Color.Unspecified,
)

@Immutable
public data class AndroidKitPageTitleBarColors(
    public val titleSurfaceColors: AndroidKitFloatingSurfaceColors = AndroidKitFloatingSurfaceColors(),
    public val buttonSurfaceColors: AndroidKitFloatingSurfaceColors = AndroidKitFloatingSurfaceColors(),
    public val flyoutColors: AndroidKitActionFlyoutColors = AndroidKitActionFlyoutColors(),
)

@Immutable
public data class AndroidKitFloatingActionButtonColors(
    public val surfaceColors: AndroidKitFloatingSurfaceColors = AndroidKitFloatingSurfaceColors(),
)

@Immutable
public data class AndroidKitCardColors(
    public val containerColor: Color = Color.Unspecified,
    public val contentColor: Color = Color.Unspecified,
    public val borderColor: Color = Color.Unspecified,
    public val supportingTextColor: Color = Color.Unspecified,
)

@Immutable
public data class AndroidKitSectionCardColors(
    public val containerColor: Color = Color.Unspecified,
    public val contentColor: Color = Color.Unspecified,
    public val borderColor: Color = Color.Unspecified,
    public val dividerColor: Color = Color.Unspecified,
    public val secondaryContentColor: Color = Color.Unspecified,
)

@Immutable
public data class AndroidKitFloatingActionBarColors(
    public val surfaceColors: AndroidKitFloatingSurfaceColors = AndroidKitFloatingSurfaceColors(),
    public val flyoutColors: AndroidKitActionFlyoutColors = AndroidKitActionFlyoutColors(),
)

@Immutable
public data class AndroidKitFloatingToolbarColors(
    public val surfaceColors: AndroidKitFloatingSurfaceColors = AndroidKitFloatingSurfaceColors(),
    public val flyoutColors: AndroidKitActionFlyoutColors = AndroidKitActionFlyoutColors(),
    public val separatorColor: Color = Color.Unspecified,
)

@Immutable
public data class AndroidKitActionFlyoutColors(
    public val surfaceColors: AndroidKitFloatingSurfaceColors = AndroidKitFloatingSurfaceColors(),
)

@Immutable
public data class AndroidKitAdaptiveNavigationItemColors(
    public val selectedIconColor: Color = Color.Unspecified,
    public val selectedTextColor: Color = Color.Unspecified,
    public val selectedIndicatorColor: Color = Color.Unspecified,
    public val unselectedIconColor: Color = Color.Unspecified,
    public val unselectedTextColor: Color = Color.Unspecified,
    public val disabledIconColor: Color = Color.Unspecified,
    public val disabledTextColor: Color = Color.Unspecified,
    public val drawerSelectedContainerColor: Color = Color.Unspecified,
    public val drawerUnselectedContainerColor: Color = Color.Unspecified,
    public val drawerSelectedBadgeColor: Color = Color.Unspecified,
    public val drawerUnselectedBadgeColor: Color = Color.Unspecified,
)

@Immutable
public data class AndroidKitFloatingNavigationColors(
    public val containerColor: Color = Color.Unspecified,
    public val navigationBarContainerColor: Color = Color.Unspecified,
    public val navigationRailContainerColor: Color = Color.Unspecified,
    public val navigationDrawerContainerColor: Color = Color.Unspecified,
    public val compactSurfaceColors: AndroidKitFloatingSurfaceColors = AndroidKitFloatingSurfaceColors(),
    public val compactContainerColor: Color = Color.Unspecified,
    public val selectedContainerColor: Color = Color.Unspecified,
    public val selectedContentColor: Color = Color.Unspecified,
    public val unselectedContentColor: Color = Color.Unspecified,
    public val overflowFlyoutColors: AndroidKitActionFlyoutColors = AndroidKitActionFlyoutColors(),
    public val adaptiveItemColors: AndroidKitAdaptiveNavigationItemColors = AndroidKitAdaptiveNavigationItemColors(),
)

@Immutable
public data class AndroidKitBottomSheetColors(
    public val containerColor: Color = Color.Unspecified,
    public val contentColor: Color = Color.Unspecified,
    public val dragHandleColor: Color = Color.Unspecified,
    public val scrimColor: Color = Color.Unspecified,
    public val chromeContainerColor: Color = Color.Unspecified,
    public val flyoutColors: AndroidKitActionFlyoutColors = AndroidKitActionFlyoutColors(),
)

/** Component-specific colors; geometry and typography remain Kit-owned. */
@Immutable
public data class AndroidKitComponentColors(
    public val page: AndroidKitPageColors = AndroidKitPageColors(),
    public val pageTitleBar: AndroidKitPageTitleBarColors = AndroidKitPageTitleBarColors(),
    public val card: AndroidKitCardColors = AndroidKitCardColors(),
    public val sectionCard: AndroidKitSectionCardColors = AndroidKitSectionCardColors(),
    public val bottomSheet: AndroidKitBottomSheetColors = AndroidKitBottomSheetColors(),
    public val floatingActionButton: AndroidKitFloatingActionButtonColors = AndroidKitFloatingActionButtonColors(),
    public val floatingActionBar: AndroidKitFloatingActionBarColors = AndroidKitFloatingActionBarColors(),
    public val floatingToolbar: AndroidKitFloatingToolbarColors = AndroidKitFloatingToolbarColors(),
    public val actionFlyout: AndroidKitActionFlyoutColors = AndroidKitActionFlyoutColors(),
    public val floatingNavigation: AndroidKitFloatingNavigationColors = AndroidKitFloatingNavigationColors(),
)

internal fun Color.orDefault(fallback: Color): Color =
    if (this == Color.Unspecified) fallback else this

internal fun AndroidKitFloatingSurfaceStyle.withColors(colors: AndroidKitFloatingSurfaceColors): AndroidKitFloatingSurfaceStyle = copy(
    containerColor = colors.containerColor.orDefault(containerColor),
    contentColor = colors.contentColor.orDefault(contentColor),
    borderColor = colors.borderColor.orDefault(borderColor),
    shadowColor = colors.shadowColor.orDefault(shadowColor),
    disabledContainerColor = colors.disabledContainerColor.orDefault(disabledContainerColor),
    disabledContentColor = colors.disabledContentColor.orDefault(disabledContentColor),
    disabledBorderColor = colors.disabledBorderColor.orDefault(disabledBorderColor),
)

internal fun AndroidKitFloatingSurfaceStyle.toColors(): AndroidKitFloatingSurfaceColors = AndroidKitFloatingSurfaceColors(
    containerColor = containerColor,
    contentColor = contentColor,
    borderColor = borderColor,
    shadowColor = shadowColor,
    disabledContainerColor = disabledContainerColor,
    disabledContentColor = disabledContentColor,
    disabledBorderColor = disabledBorderColor,
)

internal fun AndroidKitPageStyle.withColors(colors: AndroidKitPageColors): AndroidKitPageStyle = copy(
    containerColor = colors.containerColor.orDefault(containerColor),
    contentProtectionColor = colors.contentProtectionColor.orDefault(contentProtectionColor),
)

internal fun AndroidKitPageStyle.toColors(): AndroidKitPageColors = AndroidKitPageColors(
    containerColor = containerColor,
    contentProtectionColor = contentProtectionColor,
)

internal fun AndroidKitPageTitleBarStyle.withColors(colors: AndroidKitPageTitleBarColors, shared: AndroidKitFloatingSurfaceStyle): AndroidKitPageTitleBarStyle = copy(
    titleSurfaceStyle = if (colors.titleSurfaceColors == AndroidKitFloatingSurfaceColors()) titleSurfaceStyle else (titleSurfaceStyle ?: shared).withColors(colors.titleSurfaceColors),
    buttonSurfaceStyle = if (colors.buttonSurfaceColors == AndroidKitFloatingSurfaceColors()) buttonSurfaceStyle else (buttonSurfaceStyle ?: shared).withColors(colors.buttonSurfaceColors),
    flyoutStyle = if (colors.flyoutColors == AndroidKitActionFlyoutColors()) flyoutStyle else (flyoutStyle ?: AndroidKitActionFlyoutStyle(shape = AndroidKitDefaults.shapes.extraLarge)).withColors(colors.flyoutColors, shared),
)

internal fun AndroidKitPageTitleBarStyle.toColors(): AndroidKitPageTitleBarColors = AndroidKitPageTitleBarColors(
    titleSurfaceColors = titleSurfaceStyle?.toColors() ?: AndroidKitFloatingSurfaceColors(),
    buttonSurfaceColors = buttonSurfaceStyle?.toColors() ?: AndroidKitFloatingSurfaceColors(),
    flyoutColors = flyoutStyle?.toColors() ?: AndroidKitActionFlyoutColors(),
)

internal fun AndroidKitFloatingActionButtonStyle.withColors(colors: AndroidKitFloatingActionButtonColors, shared: AndroidKitFloatingSurfaceStyle): AndroidKitFloatingActionButtonStyle = copy(
    surfaceStyle = if (colors.surfaceColors == AndroidKitFloatingSurfaceColors()) surfaceStyle else (surfaceStyle ?: shared).withColors(colors.surfaceColors),
)

internal fun AndroidKitFloatingActionButtonStyle.toColors(): AndroidKitFloatingActionButtonColors = AndroidKitFloatingActionButtonColors(
    surfaceColors = surfaceStyle?.toColors() ?: AndroidKitFloatingSurfaceColors(),
)

internal fun AndroidKitCardStyle.withColors(colors: AndroidKitCardColors): AndroidKitCardStyle = copy(
    containerColor = colors.containerColor.orDefault(containerColor),
    contentColor = colors.contentColor.orDefault(contentColor),
    borderColor = colors.borderColor.orDefault(borderColor),
    supportingTextColor = colors.supportingTextColor.orDefault(supportingTextColor),
)

internal fun AndroidKitCardStyle.toColors(): AndroidKitCardColors = AndroidKitCardColors(
    containerColor = containerColor,
    contentColor = contentColor,
    borderColor = borderColor,
    supportingTextColor = supportingTextColor,
)

internal fun AndroidKitSettingSectionStyle.withColors(colors: AndroidKitSectionCardColors): AndroidKitSettingSectionStyle = copy(
    containerColor = colors.containerColor.orDefault(containerColor),
    contentColor = colors.contentColor.orDefault(contentColor),
    borderColor = colors.borderColor.orDefault(borderColor),
    dividerColor = colors.dividerColor.orDefault(dividerColor),
    secondaryContentColor = colors.secondaryContentColor.orDefault(secondaryContentColor),
)

internal fun AndroidKitSettingSectionStyle.toColors(): AndroidKitSectionCardColors = AndroidKitSectionCardColors(
    containerColor = containerColor,
    contentColor = contentColor,
    borderColor = borderColor,
    dividerColor = dividerColor,
    secondaryContentColor = secondaryContentColor,
)

internal fun AndroidKitFloatingActionBarStyle.withColors(colors: AndroidKitFloatingActionBarColors, shared: AndroidKitFloatingSurfaceStyle): AndroidKitFloatingActionBarStyle = copy(
    surfaceStyle = if (colors.surfaceColors == AndroidKitFloatingSurfaceColors()) surfaceStyle else (surfaceStyle ?: shared).withColors(colors.surfaceColors),
    flyoutStyle = if (colors.flyoutColors == AndroidKitActionFlyoutColors()) flyoutStyle else (flyoutStyle ?: AndroidKitActionFlyoutStyle(shape = AndroidKitDefaults.shapes.extraLarge)).withColors(colors.flyoutColors, shared),
)

internal fun AndroidKitFloatingActionBarStyle.toColors(): AndroidKitFloatingActionBarColors = AndroidKitFloatingActionBarColors(
    surfaceColors = surfaceStyle?.toColors() ?: AndroidKitFloatingSurfaceColors(),
    flyoutColors = flyoutStyle?.toColors() ?: AndroidKitActionFlyoutColors(),
)

internal fun AndroidKitFloatingToolbarStyle.withColors(colors: AndroidKitFloatingToolbarColors, shared: AndroidKitFloatingSurfaceStyle): AndroidKitFloatingToolbarStyle = copy(
    surfaceStyle = if (colors.surfaceColors == AndroidKitFloatingSurfaceColors()) surfaceStyle else (surfaceStyle ?: shared).withColors(colors.surfaceColors),
    flyoutStyle = if (colors.flyoutColors == AndroidKitActionFlyoutColors()) flyoutStyle else (flyoutStyle ?: AndroidKitActionFlyoutStyle(shape = AndroidKitDefaults.shapes.extraLarge)).withColors(colors.flyoutColors, shared),
    separatorColor = colors.separatorColor.orDefault(separatorColor),
)

internal fun AndroidKitFloatingToolbarStyle.toColors(): AndroidKitFloatingToolbarColors = AndroidKitFloatingToolbarColors(
    surfaceColors = surfaceStyle?.toColors() ?: AndroidKitFloatingSurfaceColors(),
    flyoutColors = flyoutStyle?.toColors() ?: AndroidKitActionFlyoutColors(),
    separatorColor = separatorColor,
)

internal fun AndroidKitActionFlyoutStyle.withColors(colors: AndroidKitActionFlyoutColors, shared: AndroidKitFloatingSurfaceStyle): AndroidKitActionFlyoutStyle = copy(
    surfaceStyle = if (colors.surfaceColors == AndroidKitFloatingSurfaceColors()) surfaceStyle else (surfaceStyle ?: shared).withColors(colors.surfaceColors),
)

internal fun AndroidKitActionFlyoutStyle.toColors(): AndroidKitActionFlyoutColors = AndroidKitActionFlyoutColors(
    surfaceColors = surfaceStyle?.toColors() ?: AndroidKitFloatingSurfaceColors(),
)

internal fun AndroidKitAdaptiveNavigationItemStyle.withColors(colors: AndroidKitAdaptiveNavigationItemColors): AndroidKitAdaptiveNavigationItemStyle = copy(
    selectedIconColor = colors.selectedIconColor.orDefault(selectedIconColor),
    selectedTextColor = colors.selectedTextColor.orDefault(selectedTextColor),
    selectedIndicatorColor = colors.selectedIndicatorColor.orDefault(selectedIndicatorColor),
    unselectedIconColor = colors.unselectedIconColor.orDefault(unselectedIconColor),
    unselectedTextColor = colors.unselectedTextColor.orDefault(unselectedTextColor),
    disabledIconColor = colors.disabledIconColor.orDefault(disabledIconColor),
    disabledTextColor = colors.disabledTextColor.orDefault(disabledTextColor),
    drawerSelectedContainerColor = colors.drawerSelectedContainerColor.orDefault(drawerSelectedContainerColor),
    drawerUnselectedContainerColor = colors.drawerUnselectedContainerColor.orDefault(drawerUnselectedContainerColor),
    drawerSelectedBadgeColor = colors.drawerSelectedBadgeColor.orDefault(drawerSelectedBadgeColor),
    drawerUnselectedBadgeColor = colors.drawerUnselectedBadgeColor.orDefault(drawerUnselectedBadgeColor),
)

internal fun AndroidKitAdaptiveNavigationItemStyle.toColors(): AndroidKitAdaptiveNavigationItemColors = AndroidKitAdaptiveNavigationItemColors(
    selectedIconColor = selectedIconColor,
    selectedTextColor = selectedTextColor,
    selectedIndicatorColor = selectedIndicatorColor,
    unselectedIconColor = unselectedIconColor,
    unselectedTextColor = unselectedTextColor,
    disabledIconColor = disabledIconColor,
    disabledTextColor = disabledTextColor,
    drawerSelectedContainerColor = drawerSelectedContainerColor,
    drawerUnselectedContainerColor = drawerUnselectedContainerColor,
    drawerSelectedBadgeColor = drawerSelectedBadgeColor,
    drawerUnselectedBadgeColor = drawerUnselectedBadgeColor,
)

internal fun AndroidKitFloatingNavigationStyle.withColors(colors: AndroidKitFloatingNavigationColors, shared: AndroidKitFloatingSurfaceStyle): AndroidKitFloatingNavigationStyle = copy(
    containerColor = colors.containerColor.orDefault(containerColor),
    navigationBarContainerColor = colors.navigationBarContainerColor.orDefault(navigationBarContainerColor),
    navigationRailContainerColor = colors.navigationRailContainerColor.orDefault(navigationRailContainerColor),
    navigationDrawerContainerColor = colors.navigationDrawerContainerColor.orDefault(navigationDrawerContainerColor),
    compactSurfaceStyle = if (colors.compactSurfaceColors == AndroidKitFloatingSurfaceColors()) compactSurfaceStyle else (compactSurfaceStyle ?: shared).withColors(colors.compactSurfaceColors),
    compactContainerColor = colors.compactContainerColor.orDefault(compactContainerColor),
    selectedContainerColor = colors.selectedContainerColor.orDefault(selectedContainerColor),
    selectedContentColor = colors.selectedContentColor.orDefault(selectedContentColor),
    unselectedContentColor = colors.unselectedContentColor.orDefault(unselectedContentColor),
    overflowFlyoutStyle = if (colors.overflowFlyoutColors == AndroidKitActionFlyoutColors()) overflowFlyoutStyle else (overflowFlyoutStyle ?: AndroidKitActionFlyoutStyle(shape = AndroidKitDefaults.shapes.extraLarge)).withColors(colors.overflowFlyoutColors, shared),
    adaptiveItemStyle = if (colors.adaptiveItemColors == AndroidKitAdaptiveNavigationItemColors()) adaptiveItemStyle else (adaptiveItemStyle ?: AndroidKitAdaptiveNavigationItemStyle()).withColors(colors.adaptiveItemColors),
)

internal fun AndroidKitFloatingNavigationStyle.toColors(): AndroidKitFloatingNavigationColors = AndroidKitFloatingNavigationColors(
    containerColor = containerColor,
    navigationBarContainerColor = navigationBarContainerColor,
    navigationRailContainerColor = navigationRailContainerColor,
    navigationDrawerContainerColor = navigationDrawerContainerColor,
    compactSurfaceColors = compactSurfaceStyle?.toColors() ?: AndroidKitFloatingSurfaceColors(),
    compactContainerColor = compactContainerColor,
    selectedContainerColor = selectedContainerColor,
    selectedContentColor = selectedContentColor,
    unselectedContentColor = unselectedContentColor,
    overflowFlyoutColors = overflowFlyoutStyle?.toColors() ?: AndroidKitActionFlyoutColors(),
    adaptiveItemColors = adaptiveItemStyle?.toColors() ?: AndroidKitAdaptiveNavigationItemColors(),
)

internal fun AndroidKitBottomSheetStyle.withColors(colors: AndroidKitBottomSheetColors, shared: AndroidKitFloatingSurfaceStyle): AndroidKitBottomSheetStyle = copy(
    containerColor = colors.containerColor.orDefault(containerColor),
    contentColor = colors.contentColor.orDefault(contentColor),
    dragHandleColor = colors.dragHandleColor.orDefault(dragHandleColor),
    scrimColor = colors.scrimColor.orDefault(scrimColor),
    chromeContainerColor = colors.chromeContainerColor.orDefault(chromeContainerColor),
    flyoutStyle = if (colors.flyoutColors == AndroidKitActionFlyoutColors()) flyoutStyle else (flyoutStyle ?: AndroidKitActionFlyoutStyle(shape = AndroidKitDefaults.shapes.extraLarge)).withColors(colors.flyoutColors, shared),
)

internal fun AndroidKitBottomSheetStyle.toColors(): AndroidKitBottomSheetColors = AndroidKitBottomSheetColors(
    containerColor = containerColor,
    contentColor = contentColor,
    dragHandleColor = dragHandleColor,
    scrimColor = scrimColor,
    chromeContainerColor = chromeContainerColor,
    flyoutColors = flyoutStyle?.toColors() ?: AndroidKitActionFlyoutColors(),
)

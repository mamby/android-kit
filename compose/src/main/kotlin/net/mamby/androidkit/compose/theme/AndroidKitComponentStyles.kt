package net.mamby.androidkit.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

@Immutable
internal data class AndroidKitPageStyle(
    val containerColor: Color,
    val contentProtectionColor: Color = Color.Unspecified,
)

@Immutable
internal data class AndroidKitPageTitleBarStyle(
    val titleSurfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    val buttonSurfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    val flyoutStyle: AndroidKitActionFlyoutStyle? = null,
    val titleShape: Shape,
    val buttonShape: Shape,
    val titleTextStyle: TextStyle,
)

@Immutable
internal data class AndroidKitFloatingActionButtonStyle(
    val surfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    val shape: Shape,
    val visualSize: Dp,
)

@Immutable
internal data class AndroidKitCardStyle(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val borderWidth: Dp,
    val shape: Shape,
    val titleTextStyle: TextStyle? = null,
    val supportingTextStyle: TextStyle? = null,
    val supportingTextColor: Color = Color.Unspecified,
)

@Immutable
internal data class AndroidKitSettingSectionStyle(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val borderWidth: Dp,
    val dividerColor: Color,
    val secondaryContentColor: Color,
    val shape: Shape,
    val sectionLabelTextStyle: TextStyle,
    val descriptionTextStyle: TextStyle,
    val entryLabelTextStyle: TextStyle,
    val supportingTextStyle: TextStyle,
    val valueLabelTextStyle: TextStyle,
)

@Immutable
internal data class AndroidKitFloatingActionBarStyle(
    val surfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    val flyoutStyle: AndroidKitActionFlyoutStyle? = null,
    val shape: Shape,
    val itemShape: Shape,
    val labelTextStyle: TextStyle,
)

@Immutable
internal data class AndroidKitFloatingToolbarStyle(
    val surfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    val flyoutStyle: AndroidKitActionFlyoutStyle? = null,
    val separatorColor: Color,
    val shape: Shape,
    val itemShape: Shape,
    val labelTextStyle: TextStyle,
    val iconSize: Dp,
)

@Immutable
internal data class AndroidKitActionFlyoutStyle(
    val surfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    val shape: Shape,
)

@Immutable
internal data class AndroidKitAdaptiveNavigationItemStyle(
    val selectedIconColor: Color = Color.Unspecified,
    val selectedTextColor: Color = Color.Unspecified,
    val selectedIndicatorColor: Color = Color.Unspecified,
    val unselectedIconColor: Color = Color.Unspecified,
    val unselectedTextColor: Color = Color.Unspecified,
    val disabledIconColor: Color = Color.Unspecified,
    val disabledTextColor: Color = Color.Unspecified,
    val drawerSelectedContainerColor: Color = Color.Unspecified,
    val drawerUnselectedContainerColor: Color = Color.Unspecified,
    val drawerSelectedBadgeColor: Color = Color.Unspecified,
    val drawerUnselectedBadgeColor: Color = Color.Unspecified,
)

@Immutable
internal data class AndroidKitFloatingNavigationStyle(
    val containerColor: Color,
    val navigationBarContainerColor: Color,
    val navigationRailContainerColor: Color,
    val navigationDrawerContainerColor: Color,
    val compactSurfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    val compactContainerColor: Color,
    val selectedContainerColor: Color,
    val selectedContentColor: Color,
    val unselectedContentColor: Color,
    val barShape: Shape,
    val itemShape: Shape,
    val labelTextStyle: TextStyle,
    val overflowItemTextStyle: TextStyle,
    val overflowFlyoutStyle: AndroidKitActionFlyoutStyle? = null,
    val adaptiveItemStyle: AndroidKitAdaptiveNavigationItemStyle? = null,
)

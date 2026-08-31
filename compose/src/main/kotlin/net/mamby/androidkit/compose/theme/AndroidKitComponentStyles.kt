package net.mamby.androidkit.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

@Immutable
public data class AndroidKitPageStyle(
    public val containerColor: Color,
    public val contentProtectionColor: Color = Color.Unspecified,
)

@Immutable
public data class AndroidKitPageTitleBarStyle(
    public val titleSurfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    public val buttonSurfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    public val dropdownMenuStyle: AndroidKitFloatingDropdownMenuStyle? = null,
    public val titleShape: Shape,
    public val buttonShape: Shape,
    public val titleTextStyle: TextStyle,
)

@Immutable
public data class AndroidKitFloatingActionButtonStyle(
    public val surfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    public val shape: Shape,
    public val visualSize: Dp,
)

@Immutable
public data class AndroidKitCardStyle(
    public val containerColor: Color,
    public val contentColor: Color,
    public val borderColor: Color,
    public val borderWidth: Dp,
    public val shape: Shape,
)

@Immutable
public data class AndroidKitSettingSectionStyle(
    public val containerColor: Color,
    public val contentColor: Color,
    public val borderColor: Color,
    public val borderWidth: Dp,
    public val dividerColor: Color,
    public val secondaryContentColor: Color,
    public val shape: Shape,
    public val sectionLabelTextStyle: TextStyle,
    public val descriptionTextStyle: TextStyle,
    public val entryLabelTextStyle: TextStyle,
    public val supportingTextStyle: TextStyle,
    public val valueLabelTextStyle: TextStyle,
)

@Immutable
public data class AndroidKitFloatingActionBarStyle(
    public val surfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    public val dropdownMenuStyle: AndroidKitFloatingDropdownMenuStyle? = null,
    public val shape: Shape,
    public val itemShape: Shape,
    public val labelTextStyle: TextStyle,
)

@Immutable
public data class AndroidKitFloatingDropdownMenuStyle(
    public val surfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    public val shape: Shape,
)

@Immutable
public data class AndroidKitAdaptiveNavigationItemStyle(
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
public data class AndroidKitFloatingNavigationStyle(
    public val containerColor: Color,
    public val navigationBarContainerColor: Color,
    public val navigationRailContainerColor: Color,
    public val navigationDrawerContainerColor: Color,
    public val compactSurfaceStyle: AndroidKitFloatingSurfaceStyle? = null,
    public val compactContainerColor: Color,
    public val selectedContainerColor: Color,
    public val selectedContentColor: Color,
    public val unselectedContentColor: Color,
    public val barShape: Shape,
    public val itemShape: Shape,
    public val labelTextStyle: TextStyle,
    public val overflowItemTextStyle: TextStyle,
    public val overflowSheetStyle: AndroidKitBottomSheetStyle? = null,
    public val adaptiveItemStyle: AndroidKitAdaptiveNavigationItemStyle? = null,
)

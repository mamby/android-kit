package net.mamby.androidkit.compose.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Component-owned semantic defaults. The palette, type/shape/spacing scales, minimum touch
 * target and floating-surface policy are shared foundations; sibling component styles are not.
 * Keep these internal: isolation is a Kit implementation detail, not a host styling API.
 */
@Immutable
internal class AndroidKitComponentTokens(
    colors: ColorScheme,
    typography: Typography,
    shapes: Shapes,
    dimensions: AndroidKitDimensions,
) {
    val card = AndroidKitCardTokens(dimensions, typography, colors, shapes)
    val sectionCard = AndroidKitSectionCardTokens(dimensions)
    val sectionCardSlider = AndroidKitSectionCardSliderTokens(dimensions)
    val page = AndroidKitPageTokens(dimensions)
    val listPage = AndroidKitListPageTokens(dimensions)
    val lockPage = AndroidKitLockPageTokens(dimensions)
    val supportPrompt = AndroidKitSupportPromptTokens(dimensions, typography, colors, shapes)
    val tooltip = AndroidKitTooltipTokens(dimensions, typography)
    val actionFlyout = AndroidKitActionFlyoutTokens(dimensions, typography)
    val toolbar = AndroidKitToolbarTokens(dimensions, shapes)
    val actionBar = AndroidKitActionBarTokens(dimensions, colors, shapes)
    val pageTitleBar = AndroidKitPageTitleBarTokens(dimensions, colors, shapes)
    val bottomSheet = AndroidKitBottomSheetTokens(dimensions, typography, colors, shapes)
    val searchBox = AndroidKitSearchBoxTokens(dimensions, typography)
    val searchPage = AndroidKitSearchPageTokens(dimensions, typography, colors, shapes)
    val settingsPage = AndroidKitSettingsPageTokens(dimensions)
    val appLockTimeout = AndroidKitAppLockTimeoutTokens(dimensions, typography)
    val settingsPicker = AndroidKitSettingsPickerTokens(dimensions, colors, shapes)
    val contextMenu = AndroidKitContextMenuTokens(dimensions, shapes, colors)
    val navigation = AndroidKitNavigationTokens(shapes)
}

@Immutable
internal class AndroidKitCardTokens(
    dimensions: AndroidKitDimensions,
    typography: Typography,
    colors: ColorScheme,
    shapes: Shapes,
) {
    val contentPadding: Dp = dimensions.spaceMedium
    val contentSpacing: Dp = dimensions.spaceSmall
    val headerActionSpacing: Dp = dimensions.spaceSmall
    val headerTextSpacing: Dp = dimensions.spaceExtraSmall
    val titleTextStyle: TextStyle = typography.titleMedium
    val supportingTextStyle: TextStyle = typography.bodyMedium
    val supportingTextColor: Color = colors.onSurfaceVariant
    val borderWidth: Dp = 1.dp
    val flyoutStyle: AndroidKitActionFlyoutStyle = AndroidKitActionFlyoutStyle(shape = shapes.extraLarge)
}

@Immutable
internal class AndroidKitSectionCardTokens(
    dimensions: AndroidKitDimensions,
) {
    val multilineSpacing: Dp = dimensions.spaceExtraSmall
    val supportingTextSpacing: Dp = dimensions.spaceExtraSmall
    val entryContentSpacing: Dp = dimensions.spaceMedium
    val borderWidth: Dp = 1.dp
    val trailingIconSize: Dp = 20.dp
}

@Immutable
internal class AndroidKitSectionCardSliderTokens(
    dimensions: AndroidKitDimensions,
) {
    val trackSpacing: Dp = dimensions.spaceExtraSmall
    val supportingTextSpacing: Dp = dimensions.spaceExtraSmall
    val labelContentSpacing: Dp = dimensions.spaceMedium
    val opacityThumbSize: Dp = dimensions.spaceLarge
}

@Immutable
internal class AndroidKitPageTokens(
    dimensions: AndroidKitDimensions,
) {
    val floatingActionMargin: Dp = dimensions.spaceMedium
    val titleContentSpacing: Dp = dimensions.spaceMedium
}

@Immutable
internal class AndroidKitListPageTokens(
    dimensions: AndroidKitDimensions,
) {
    val floatingActionMargin: Dp = dimensions.spaceMedium
    val itemSpacing: Dp = dimensions.spaceMedium
    val horizontalPadding: Dp = dimensions.screenPadding
}

@Immutable
internal class AndroidKitLockPageTokens(
    dimensions: AndroidKitDimensions,
) {
    val horizontalPadding: Dp = dimensions.screenPadding
    val verticalPadding: Dp = dimensions.spaceMedium
    val contentSpacing: Dp = dimensions.spaceMedium
}

@Immutable
internal class AndroidKitSupportPromptTokens(
    dimensions: AndroidKitDimensions,
    typography: Typography,
    colors: ColorScheme,
    shapes: Shapes,
) {
    val actionSpacing: Dp = dimensions.spaceSmall
    val donateSpacing: Dp = dimensions.spaceMedium
    val descriptionTextStyle: TextStyle = typography.bodyMedium
    val cardStyle: AndroidKitCardStyle = AndroidKitCardStyle(
        containerColor = colors.surface,
        contentColor = colors.onSurface,
        borderColor = colors.outlineVariant,
        borderWidth = 1.dp,
        shape = shapes.extraLarge,
    )
}

@Immutable
internal class AndroidKitTooltipTokens(
    dimensions: AndroidKitDimensions,
    typography: Typography,
) {
    val contentPadding: Dp = dimensions.spaceMedium
    val actionBottomPadding: Dp = dimensions.spaceExtraSmall
    val contentSpacing: Dp = dimensions.spaceSmall
    val actionHorizontalSpacing: Dp = dimensions.spaceSmall
    val actionVerticalSpacing: Dp = dimensions.spaceSmall
    val shape: Shape = AndroidKitDefaults.shapes.extraLarge
    val textStyle: TextStyle = typography.bodyMedium
    val actionTextStyle: TextStyle = typography.labelLarge
}

@Immutable
internal class AndroidKitActionFlyoutTokens(
    dimensions: AndroidKitDimensions,
    typography: Typography,
) {
    val itemStartPadding: Dp = dimensions.spaceMedium
    val itemEndPadding: Dp = dimensions.spaceLarge
    val separatorHorizontalPadding: Dp = dimensions.spaceMedium
    val separatorVerticalPadding: Dp = dimensions.spaceExtraSmall
    val itemTextStyle: TextStyle = typography.labelLarge
}

@Immutable
internal class AndroidKitToolbarTokens(
    dimensions: AndroidKitDimensions,
    shapes: Shapes,
) {
    val horizontalPadding: Dp = dimensions.spaceSmall
    val verticalPadding: Dp = dimensions.spaceExtraSmall
    val itemSpacing: Dp = dimensions.spaceSmall
    val iconSize: Dp = 18.dp
    val flyoutStyle: AndroidKitActionFlyoutStyle = AndroidKitActionFlyoutStyle(shape = shapes.extraLarge)
}

@Immutable
internal class AndroidKitActionBarTokens(
    dimensions: AndroidKitDimensions,
    colors: ColorScheme,
    shapes: Shapes,
) {
    val horizontalPadding: Dp = dimensions.spaceSmall
    val verticalPadding: Dp = dimensions.spaceExtraSmall
    val itemSpacing: Dp = dimensions.spaceSmall
    val separatorColor: Color = colors.outlineVariant
    val flyoutStyle: AndroidKitActionFlyoutStyle = AndroidKitActionFlyoutStyle(shape = shapes.extraLarge)
}

@Immutable
internal class AndroidKitPageTitleBarTokens(
    dimensions: AndroidKitDimensions,
    colors: ColorScheme,
    shapes: Shapes,
) {
    val iconSize: Dp = 20.dp
    val horizontalPadding: Dp = dimensions.spaceSmall
    val actionSpacing: Dp = dimensions.spaceSmall
    val controlVerticalPadding: Dp = dimensions.spaceSmall
    val titleHorizontalPadding: Dp = dimensions.spaceMedium
    val navigationTitleSpacing: Dp = dimensions.spaceExtraSmall
    val titleActionsSpacing: Dp = dimensions.spaceExtraSmall
    val separatorColor: Color = colors.outlineVariant
    val actionTextStyle: TextStyle = AndroidKitDefaults.typography.labelSmall
    val flyoutStyle: AndroidKitActionFlyoutStyle = AndroidKitActionFlyoutStyle(shape = shapes.extraLarge)
}

@Immutable
internal class AndroidKitBottomSheetTokens(
    dimensions: AndroidKitDimensions,
    typography: Typography,
    colors: ColorScheme,
    shapes: Shapes,
) {
    val floatingActionMargin: Dp = dimensions.spaceMedium
    val searchContentSpacing: Dp = dimensions.spaceSmall
    val headerVerticalPadding: Dp = dimensions.spaceExtraSmall
    val trailingActionsSpacing: Dp = dimensions.spaceSmall
    val actionRowPadding: Dp = dimensions.spaceSmall
    val actionSpacing: Dp = dimensions.spaceSmall
    val actionContentPadding: Dp = dimensions.spaceSmall
    val titleTextStyle: TextStyle = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    val separatorColor: Color = colors.outlineVariant
    val actionTextStyle: TextStyle = AndroidKitDefaults.typography.labelSmall
    val flyoutStyle: AndroidKitActionFlyoutStyle = AndroidKitActionFlyoutStyle(shape = shapes.extraLarge)
}

@Immutable
internal class AndroidKitSearchBoxTokens(
    dimensions: AndroidKitDimensions,
    typography: Typography,
) {
    val statusHorizontalPadding: Dp = dimensions.spaceSmall
    val statusTextPadding: Dp = dimensions.spaceMedium
    val iconSize: Dp = 20.dp
    val shape: Shape = AndroidKitDefaults.shapes.extraLarge
    val inputTextStyle: TextStyle = typography.bodyLarge
}

@Immutable
internal class AndroidKitSearchPageTokens(
    dimensions: AndroidKitDimensions,
    typography: Typography,
    colors: ColorScheme,
    shapes: Shapes,
) {
    val horizontalPadding: Dp = dimensions.screenPadding
    val bottomPadding: Dp = dimensions.spaceMedium
    val recentRowSpacing: Dp = dimensions.spaceSmall
    val resultSpacing: Dp = 20.dp
    val headingBottomSpacing: Dp = 20.dp
    val emptyContentSpacing: Dp = dimensions.spaceLarge
    val emptyIconContainerSize: Dp = 80.dp
    val emptyIconSize: Dp = 24.dp
    val headingIconSize: Dp = 18.dp
    val recentIconSize: Dp = 18.dp
    val headingHorizontalPadding: Dp = dimensions.spaceMedium
    val headingVerticalPadding: Dp = dimensions.spaceExtraSmall
    val clearButtonEndPadding: Dp = dimensions.spaceExtraSmall
    val recentStartPadding: Dp = dimensions.spaceMedium
    val recentTextEndPadding: Dp = dimensions.spaceMedium
    val recentIconSpacing: Dp = dimensions.spaceSmall
    val emptyMessagePadding: Dp = dimensions.spaceMedium
    val emptyContentColor: Color = colors.onSurface
    val secondaryContentColor: Color = colors.onSurfaceVariant
    val recentContentColor: Color = colors.onSurface
    val headingTextStyle: TextStyle = AndroidKitDefaults.typography.labelLarge.copy(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
    )
    val emptyMessageTextStyle: TextStyle = AndroidKitDefaults.typography.bodyMedium
    val emptyTextStyle: TextStyle = typography.bodyLarge
    val recentTextStyle: TextStyle = typography.bodyLarge
    val headingShape: Shape = shapes.extraLarge
    val recentCardStyle: AndroidKitCardStyle = AndroidKitCardStyle(
        containerColor = colors.surface,
        contentColor = colors.onSurface,
        borderColor = colors.outlineVariant,
        borderWidth = 1.dp,
        shape = shapes.extraLarge,
    )
}

@Immutable
internal class AndroidKitSettingsPageTokens(
    dimensions: AndroidKitDimensions,
) {
    val horizontalPadding: Dp = dimensions.screenPadding
    val bottomPadding: Dp = dimensions.spaceMedium
}

@Immutable
internal class AndroidKitAppLockTimeoutTokens(
    dimensions: AndroidKitDimensions,
    typography: Typography,
) {
    val rowVerticalPadding: Dp = 16.dp
    val rowContentSpacing: Dp = dimensions.spaceMedium
    val labelTextStyle: TextStyle = typography.bodyLarge
}

@Immutable
internal class AndroidKitSettingsPickerTokens(
    dimensions: AndroidKitDimensions,
    colors: ColorScheme,
    shapes: Shapes,
) {
    val rowSpacing: Dp = dimensions.spaceExtraSmall
    val rowContentSpacing: Dp = dimensions.spaceMedium
    val contentColor: Color = colors.onSurface
    val itemShape: Shape = shapes.extraLarge
    val labelTextStyle: TextStyle = AndroidKitDefaults.typography.bodyLarge
    val selectedContentColor: Color = colors.onSecondaryContainer
    val selectedContainerColor: Color = colors.secondaryContainer
}

@Immutable
internal class AndroidKitContextMenuTokens(
    dimensions: AndroidKitDimensions,
    shapes: Shapes,
    colors: ColorScheme,
) {
    val selectedContainerColor: Color = colors.secondaryContainer
    val verticalPadding: Dp = dimensions.spaceSmall
    val flyoutStyle: AndroidKitActionFlyoutStyle = AndroidKitActionFlyoutStyle(shape = shapes.extraLarge)
}

@Immutable
internal class AndroidKitNavigationTokens(
    shapes: Shapes,
) {
    val flyoutStyle: AndroidKitActionFlyoutStyle = AndroidKitActionFlyoutStyle(shape = shapes.extraLarge)
}

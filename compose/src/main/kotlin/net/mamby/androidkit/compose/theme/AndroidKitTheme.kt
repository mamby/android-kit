package net.mamby.androidkit.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
public data class AndroidKitThemeDefinition(
    public val colorScheme: ColorScheme,
    public val isDark: Boolean,
    public val typography: Typography = AndroidKitDefaults.typography,
    public val shapes: Shapes = AndroidKitDefaults.shapes,
    public val dimensions: AndroidKitDimensions = AndroidKitDimensions(),
    public val bottomSheetStyle: AndroidKitBottomSheetStyle = AndroidKitBottomSheetStyle(
        containerColor = colorScheme.surface,
        contentColor = colorScheme.onSurface,
        dragHandleColor = colorScheme.outline,
    ),
)

@Immutable
public data class AndroidKitBottomSheetStyle(
    public val containerColor: Color,
    public val contentColor: Color,
    public val dragHandleColor: Color,
    public val scrimColor: Color = Color(0x66000000),
)

@Immutable
public data class AndroidKitFloatingSurfaceStyle(
    public val opacity: Float = 0.8f,
) {
    init {
        require(opacity in 0f..1f) { "Floating surface opacity must be between 0 and 1." }
    }
}

@Immutable
public data class AndroidKitDimensions(
    public val spaceExtraSmall: Dp = 4.dp,
    public val spaceSmall: Dp = 8.dp,
    public val spaceMedium: Dp = 16.dp,
    public val spaceLarge: Dp = 24.dp,
    public val spaceExtraLarge: Dp = 32.dp,
    public val screenPadding: Dp = 20.dp,
    public val contentMaxWidth: Dp = 1_200.dp,
    public val minimumTouchTarget: Dp = 48.dp,
    public val settingSectionEntryVerticalPadding: Dp = 12.dp,
    public val floatingNavigationMargin: Dp = 8.dp,
    public val floatingNavigationMaxWidth: Dp = 560.dp,
    public val floatingNavigationIconSize: Dp = 24.dp,
    public val floatingNavigationIndicatorSize: Dp = 40.dp,
    public val floatingActionBarIconSize: Dp = 18.dp,
    public val floatingTitleBarButtonSize: Dp = 44.dp,
    public val floatingTitleBarVerticalPadding: Dp = spaceSmall,
    public val floatingActionButtonSize: Dp = 56.dp,
    public val floatingActionIconSize: Dp = 20.dp,
    public val floatingSurfaceBorderWidth: Dp = 1.dp,
    public val floatingSurfaceShadowRadius: Dp = 6.dp,
    public val floatingSurfaceShadowOffsetY: Dp = 5.dp,
    public val floatingSurfaceButtonShadowRadius: Dp = 3.dp,
    public val floatingSurfaceButtonShadowOffsetY: Dp = 2.5.dp,
    public val floatingDropdownShadowElevation: Dp = 1.dp,
    public val floatingTitleBarHeight: Dp = 0.dp,
    public val floatingTitleMinimumWidth: Dp = 48.dp,
    public val bottomSheetCornerRadius: Dp = 24.dp,
    public val bottomSheetHorizontalPadding: Dp = 12.dp,
    public val bottomSheetTopPadding: Dp = 10.dp,
    public val bottomSheetBottomPadding: Dp = 20.dp,
    public val bottomSheetDragHandleWidth: Dp = 44.dp,
    public val bottomSheetDragHandleHeight: Dp = 3.dp,
    public val bottomSheetDragHandleRadius: Dp = 1.5.dp,
    public val bottomSheetDragHandleBottomSpacing: Dp = 12.dp,
    public val bottomSheetBackTitleSpacing: Dp = 6.dp,
    public val bottomSheetHeaderCloseSpacing: Dp = 12.dp,
    public val bottomSheetChromeContentSpacing: Dp = 18.dp,
    public val bottomSheetIconButtonSize: Dp = minimumTouchTarget,
    public val bottomSheetIconSize: Dp = 26.dp,
    @Deprecated("Floating titles now use a surface capsule instead of a text shadow.")
    public val floatingTitleTextShadowRadius: Dp = 2.dp,
    @Deprecated("Content protection now uses a background gradient instead of blur.")
    public val contentProtectionBlurRadius: Dp = 0.dp,
    public val contentProtectionFadeLength: Dp = 4.dp,
    @Deprecated("Flyouts no longer expand page content protection.")
    public val navigationFlyoutProtectionHeight: Dp = 0.dp,
)

@Immutable
public data class AndroidKitStrings(
    public val back: String,
    public val add: String,
    public val close: String,
    public val more: String,
    public val retry: String,
    public val cancel: String,
    public val confirm: String,
    public val save: String,
    public val hideTitleBar: String = "Hide title bar",
    public val showTitleBar: String = "Show title bar",
) {
    public companion object {
        public val English: AndroidKitStrings = AndroidKitStrings(
            back = "Back",
            add = "Add",
            close = "Close",
            more = "More",
            retry = "Retry",
            cancel = "Cancel",
            confirm = "Confirm",
            save = "Save",
            hideTitleBar = "Hide title bar",
            showTitleBar = "Show title bar",
        )
    }
}

public object AndroidKitThemes {
    public val Light: AndroidKitThemeDefinition = AndroidKitThemeDefinition(
        colorScheme = lightColorScheme(
            primary = Color(0xFF5555C7),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE3E0FF),
            onPrimaryContainer = Color(0xFF17124F),
            secondary = Color(0xFF62616F),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE8E5F3),
            onSecondaryContainer = Color(0xFF1E1D29),
            tertiary = Color(0xFF9A4967),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFD9E4),
            onTertiaryContainer = Color(0xFF3E001E),
            background = Color(0xFFF7F7F7),
            onBackground = Color(0xFF171717),
            surface = Color.White,
            onSurface = Color(0xFF171717),
            surfaceVariant = Color(0xFFF5F5F5),
            onSurfaceVariant = Color(0xFF525252),
            outline = Color(0xFF737373),
            outlineVariant = Color(0xFFE2E2E2),
            error = Color(0xFFBA1A1A),
            onError = Color.White,
        ),
        isDark = false,
        bottomSheetStyle = AndroidKitBottomSheetStyle(
            containerColor = Color.White,
            contentColor = Color(0xFF171A21),
            dragHandleColor = Color(0xFFC1C7D2),
        ),
    )

    public val Dark: AndroidKitThemeDefinition = AndroidKitThemeDefinition(
        colorScheme = darkColorScheme(
            primary = Color(0xFFC4C1FF),
            onPrimary = Color(0xFF272175),
            primaryContainer = Color(0xFF3E3A8D),
            onPrimaryContainer = Color(0xFFE3E0FF),
            secondary = Color(0xFFCBC8D7),
            onSecondary = Color(0xFF33313F),
            secondaryContainer = Color(0xFF494754),
            onSecondaryContainer = Color(0xFFE8E5F3),
            tertiary = Color(0xFFFFB0CA),
            onTertiary = Color(0xFF5E1138),
            tertiaryContainer = Color(0xFF7B2F4F),
            onTertiaryContainer = Color(0xFFFFD9E4),
            background = Color(0xFF131217),
            onBackground = Color(0xFFE6E1E7),
            surface = Color(0xFF131217),
            onSurface = Color(0xFFE6E1E7),
            surfaceVariant = Color(0xFF48454F),
            onSurfaceVariant = Color(0xFFCAC4D0),
            outline = Color(0xFF938F99),
            outlineVariant = Color(0xFF48454F),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
        ),
        isDark = true,
        bottomSheetStyle = AndroidKitBottomSheetStyle(
            containerColor = Color(0xFF090B11),
            contentColor = Color(0xFFF5F7FB),
            dragHandleColor = Color(0xFF93A0B7),
        ),
    )
}

public object AndroidKitDefaults {
    public val typography: Typography = Typography(
        displaySmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 36.sp,
            lineHeight = 44.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        ),
    )

    public val shapes: Shapes = Shapes()
}

private const val MissingAndroidKitThemeMessage =
    "Android Kit components must be wrapped in AndroidKitTheme."

private fun missingAndroidKitTheme(): Nothing = error(MissingAndroidKitThemeMessage)

private val LocalAndroidKitThemeDefinition =
    staticCompositionLocalOf<AndroidKitThemeDefinition> { missingAndroidKitTheme() }
private val LocalAndroidKitStrings =
    staticCompositionLocalOf<AndroidKitStrings> { missingAndroidKitTheme() }
private val LocalFloatingSurfaceStyle =
    staticCompositionLocalOf<AndroidKitFloatingSurfaceStyle> { missingAndroidKitTheme() }

/**
 * Theme values supplied by the nearest [AndroidKitTheme].
 *
 * Access outside an [AndroidKitTheme] fails immediately instead of silently using fallback styles.
 */
public object AndroidKitThemeTokens {
    public val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAndroidKitThemeDefinition.current.colorScheme

    public val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalAndroidKitThemeDefinition.current.typography

    public val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAndroidKitThemeDefinition.current.shapes

    public val dimensions: AndroidKitDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalAndroidKitThemeDefinition.current.dimensions

    public val strings: AndroidKitStrings
        @Composable
        @ReadOnlyComposable
        get() = LocalAndroidKitStrings.current

    public val floatingSurfaceStyle: AndroidKitFloatingSurfaceStyle
        @Composable
        @ReadOnlyComposable
        get() = LocalFloatingSurfaceStyle.current

    public val bottomSheetStyle: AndroidKitBottomSheetStyle
        @Composable
        @ReadOnlyComposable
        get() = LocalAndroidKitThemeDefinition.current.bottomSheetStyle
}

/**
 * Required theme boundary for Android Kit Compose components.
 *
 * Supply a custom [AndroidKitThemeDefinition] to customize component colors, typography, shapes,
 * dimensions, and component-specific styles consistently.
 */
@Composable
public fun AndroidKitTheme(
    definition: AndroidKitThemeDefinition = if (isSystemInDarkTheme()) {
        AndroidKitThemes.Dark
    } else {
        AndroidKitThemes.Light
    },
    strings: AndroidKitStrings = AndroidKitStrings.English,
    floatingSurfaceStyle: AndroidKitFloatingSurfaceStyle = AndroidKitFloatingSurfaceStyle(),
    content: @Composable () -> Unit,
): Unit {
    CompositionLocalProvider(
        LocalAndroidKitThemeDefinition provides definition,
        LocalAndroidKitStrings provides strings,
        LocalFloatingSurfaceStyle provides floatingSurfaceStyle,
    ) {
        MaterialTheme(
            colorScheme = definition.colorScheme,
            typography = definition.typography,
            shapes = definition.shapes,
            content = content,
        )
    }
}

package net.mamby.androidkit.compose.form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitBottomSheetStyle
import net.mamby.androidkit.compose.theme.AndroidKitDimensions
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.floatingSurfaceAlphaForLevel

public enum class AndroidKitBottomSheetScrollMode {
    VerticalScroll,
    ContentManaged,
}

public object AndroidKitBottomSheetDefaults {
    public const val DefaultMaxHeightFraction: Float = 0.85f
    public const val MaximumHeightFraction: Float = 0.90f
}

/**
 * Displays modal sheet content below persistent sheet chrome.
 *
 * Sheet chrome and a [floatingAction] are overlaid so scrollable content can draw behind them.
 * When a floating action is present, the managed content padding includes its measured clearance.
 * [AndroidKitBottomSheetScrollMode.ContentManaged] callers should apply that padding to their
 * scrollable content so its final item clears the floating action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AndroidKitBottomSheet(
    visible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    style: AndroidKitBottomSheetStyle = AndroidKitThemeTokens.bottomSheetStyle,
    maxHeightFraction: Float = AndroidKitBottomSheetDefaults.DefaultMaxHeightFraction,
    scrollMode: AndroidKitBottomSheetScrollMode = AndroidKitBottomSheetScrollMode.VerticalScroll,
    fitContent: Boolean = false,
    showChrome: Boolean = true,
    gesturesEnabled: Boolean = true,
    dismissGesturesEnabled: Boolean = true,
    closeContentDescription: String? = null,
    backContentDescription: String? = null,
    onBack: (() -> Unit)? = null,
    sheetMaxWidth: Dp = Dp.Unspecified,
    sheetContentPadding: PaddingValues = PaddingValues(
        start = AndroidKitThemeTokens.dimensions.bottomSheetHorizontalPadding,
        top = AndroidKitThemeTokens.dimensions.bottomSheetTopPadding,
        end = AndroidKitThemeTokens.dimensions.bottomSheetHorizontalPadding,
    ),
    contentBottomPadding: Dp = AndroidKitThemeTokens.dimensions.bottomSheetBottomPadding,
    chromeContentSpacing: Dp = AndroidKitThemeTokens.dimensions.bottomSheetChromeContentSpacing,
    dragHandle: (@Composable ColumnScope.() -> Unit)? = {
        val dimensions = AndroidKitThemeTokens.dimensions
        BottomSheetDragHandle(dimensions = dimensions, color = style.dragHandleColor)
        Spacer(modifier = Modifier.height(dimensions.bottomSheetDragHandleBottomSpacing))
    },
    header: (@Composable (onDismiss: () -> Unit) -> Unit)? = null,
    contentWindowInsets: WindowInsets = WindowInsets.safeDrawing.only(
        WindowInsetsSides.Top + WindowInsetsSides.Bottom,
    ),
    skipPartiallyExpanded: Boolean = true,
    dismissOnBackPress: Boolean = onBack == null,
    floatingAction: @Composable () -> Unit = {},
    floatingActionAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    floatingActionMargin: Dp = AndroidKitThemeTokens.dimensions.spaceMedium,
    content: @Composable ColumnScope.(managedContentPadding: PaddingValues) -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val floatingSurfaceOpacity = floatingSurfaceAlphaForLevel(
        AndroidKitThemeTokens.floatingSurfaceOpacityLevel,
    )
    val strings = AndroidKitThemeTokens.strings
    var renderSheet by remember { mutableStateOf(visible) }

    LaunchedEffect(visible) {
        if (visible) renderSheet = true
    }

    if (!renderSheet) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
    )
    val scope = rememberCoroutineScope()
    val verticalScrollState = rememberScrollState()
    val contentScrollHandoff = remember { BottomSheetContentScrollHandoff() }
    val cappedHeightFraction = maxHeightFraction.coerceIn(
        minimumValue = 0f,
        maximumValue = AndroidKitBottomSheetDefaults.MaximumHeightFraction,
    )
    val safeDrawingTopPadding = WindowInsets.safeDrawing
        .asPaddingValues()
        .calculateTopPadding()
    val requestedTopPadding = contentWindowInsets
        .asPaddingValues()
        .calculateTopPadding()
    val stableContentWindowInsets = contentWindowInsets.only(
        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
    )
    val sheetDismissGesturesEnabled = gesturesEnabled && dismissGesturesEnabled
    val chromeContainerColor = (style.chromeContainerColor.takeIf {
        it != Color.Unspecified
    } ?: style.containerColor).copy(alpha = floatingSurfaceOpacity)

    fun dismissWithAnimation() {
        if (!gesturesEnabled) return

        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                renderSheet = false
                onDismiss()
            }
        }
    }

    LaunchedEffect(visible, sheetState) {
        if (!visible) {
            sheetState.hide()
            renderSheet = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissWithAnimation,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth,
        sheetGesturesEnabled = sheetDismissGesturesEnabled,
        shape = style.shape ?: RoundedCornerShape(
            topStart = dimensions.bottomSheetCornerRadius,
            topEnd = dimensions.bottomSheetCornerRadius,
        ),
        containerColor = style.containerColor,
        contentColor = style.contentColor,
        tonalElevation = style.tonalElevation,
        scrimColor = style.scrimColor,
        dragHandle = null,
        // Material consumes top insets from the live sheet offset. Keeping top clearance out of
        // that animated path prevents sheet measurement and its expanded anchor feeding back.
        contentWindowInsets = { stableContentWindowInsets },
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = dismissOnBackPress,
        ),
    ) {
        BackHandler(enabled = visible && onBack != null) {
            onBack?.invoke()
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val topClearance = maxOf(safeDrawingTopPadding, requestedTopPadding)
            val safeSheetHeight = (maxHeight - topClearance).coerceAtLeast(0.dp)
            val maxSheetHeight = minOf(maxHeight * cappedHeightFraction, safeSheetHeight)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
                    .padding(sheetContentPadding),
            ) {
                dragHandle?.invoke(this)

                BottomSheetContentLayout(
                    modifier = (if (fitContent) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    })
                        .nestedScroll(contentScrollHandoff)
                        .pointerInput(contentScrollHandoff) {
                            awaitEachGesture {
                                awaitFirstDown(
                                    requireUnconsumed = false,
                                    pass = PointerEventPass.Initial,
                                )
                                contentScrollHandoff.onGestureStarted()
                            }
                        },
                    showChrome = showChrome,
                    chromeContentSpacing = chromeContentSpacing,
                    contentBottomPadding = contentBottomPadding,
                    floatingAction = floatingAction,
                    floatingActionAlignment = floatingActionAlignment,
                    floatingActionMargin = floatingActionMargin,
                    chrome = {
                        if (header != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(chromeContainerColor),
                            ) {
                                header(::dismissWithAnimation)
                            }
                        } else {
                            BottomSheetChrome(
                                title = title,
                                style = style,
                                dimensions = dimensions,
                                backContentDescription = backContentDescription ?: strings.back,
                                onBack = onBack,
                                closeContentDescription = closeContentDescription ?: strings.close,
                                onClose = ::dismissWithAnimation,
                                containerColor = chromeContainerColor,
                            )
                        }
                    },
                ) { appliedContentPadding, managedContentPadding ->
                    when (scrollMode) {
                        AndroidKitBottomSheetScrollMode.VerticalScroll -> Column(
                            modifier = (if (fitContent) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier.fillMaxSize()
                            }).verticalScroll(verticalScrollState),
                        ) {
                            Spacer(
                                modifier = Modifier.height(
                                    appliedContentPadding.calculateTopPadding(),
                                ),
                            )
                            content(managedContentPadding)
                            Spacer(
                                modifier = Modifier.height(
                                    appliedContentPadding.calculateBottomPadding() +
                                        managedContentPadding.calculateBottomPadding(),
                                ),
                            )
                        }

                        AndroidKitBottomSheetScrollMode.ContentManaged -> Column(
                            modifier = (if (fitContent) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier.fillMaxSize()
                            }).padding(appliedContentPadding),
                        ) {
                            content(managedContentPadding)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomSheetContentLayout(
    showChrome: Boolean,
    chromeContentSpacing: Dp,
    contentBottomPadding: Dp,
    floatingAction: @Composable () -> Unit,
    floatingActionAlignment: Alignment.Horizontal,
    floatingActionMargin: Dp,
    chrome: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (
        appliedContentPadding: PaddingValues,
        managedContentPadding: PaddingValues,
    ) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val floatingActionMarginPx = floatingActionMargin.roundToPx()
        val chromePlaceables = if (showChrome) {
            subcompose(BottomSheetContentSlot.Chrome, chrome).map { measurable ->
                measurable.measure(constraints.copy(minHeight = 0))
            }
        } else {
            emptyList()
        }
        val chromeHeight = chromePlaceables.maxOfOrNull { it.height } ?: 0
        val floatingActionConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
            maxWidth = (constraints.maxWidth - floatingActionMarginPx * 2).coerceAtLeast(0),
            maxHeight = (constraints.maxHeight - floatingActionMarginPx * 2).coerceAtLeast(0),
        )
        val floatingActionPlaceables = subcompose(BottomSheetContentSlot.FloatingAction) {
            Box(contentAlignment = Alignment.Center) {
                floatingAction()
            }
        }.map { measurable -> measurable.measure(floatingActionConstraints) }
        val floatingActionHeight = floatingActionPlaceables.maxOfOrNull { it.height } ?: 0
        val floatingActionClearance = if (floatingActionHeight == 0) {
            0.dp
        } else {
            floatingActionHeight.toDp() + floatingActionMargin
        }
        val hasFloatingAction = floatingActionHeight > 0
        val appliedContentPadding = PaddingValues(
            top = if (showChrome) chromeHeight.toDp() + chromeContentSpacing else 0.dp,
            bottom = if (hasFloatingAction) 0.dp else contentBottomPadding,
        )
        val managedContentPadding = PaddingValues(
            bottom = if (hasFloatingAction) {
                contentBottomPadding + floatingActionClearance
            } else {
                0.dp
            },
        )
        val contentPlaceables = subcompose(BottomSheetContentSlot.Content) {
            content(appliedContentPadding, managedContentPadding)
        }.map { measurable -> measurable.measure(constraints) }
        val width = maxOf(
            chromePlaceables.maxOfOrNull { it.width } ?: 0,
            contentPlaceables.maxOfOrNull { it.width } ?: 0,
            floatingActionPlaceables.maxOfOrNull { it.width } ?: 0,
        ).coerceIn(constraints.minWidth, constraints.maxWidth)
        val floatingActionMinimumHeight = if (floatingActionHeight == 0) {
            0
        } else {
            floatingActionHeight + floatingActionMarginPx
        }
        val height = maxOf(
            chromeHeight,
            contentPlaceables.maxOfOrNull { it.height } ?: 0,
            floatingActionMinimumHeight,
        ).coerceIn(constraints.minHeight, constraints.maxHeight)
        val availableFloatingActionWidth =
            (width - floatingActionMarginPx * 2).coerceAtLeast(0)

        layout(width, height) {
            contentPlaceables.forEach { it.placeRelative(0, 0) }
            chromePlaceables.forEach { placeable ->
                placeable.placeRelative(x = (width - placeable.width) / 2, y = 0)
            }
            floatingActionPlaceables.forEach { placeable ->
                placeable.place(
                    x = floatingActionMarginPx + floatingActionAlignment.align(
                        size = placeable.width,
                        space = availableFloatingActionWidth,
                        layoutDirection = layoutDirection,
                    ),
                    y = (height - floatingActionMarginPx - placeable.height).coerceAtLeast(0),
                )
            }
        }
    }
}

private enum class BottomSheetContentSlot {
    Chrome,
    Content,
    FloatingAction,
}

private class BottomSheetContentScrollHandoff : NestedScrollConnection {
    private var contentConsumedDownwardPull = false

    fun onGestureStarted() {
        contentConsumedDownwardPull = false
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (source != NestedScrollSource.UserInput) return Offset.Zero

        contentConsumedDownwardPull =
            contentConsumedDownwardPull || consumed.y > 0f

        return if (contentConsumedDownwardPull && available.y > 0f) {
            Offset(x = 0f, y = available.y)
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity,
    ): Velocity = if (contentConsumedDownwardPull && available.y > 0f) {
        Velocity(x = 0f, y = available.y)
    } else {
        Velocity.Zero
    }
}

@Composable
private fun ColumnScope.BottomSheetDragHandle(
    dimensions: AndroidKitDimensions,
    color: Color,
) {
    Box(
        modifier = Modifier
            .width(dimensions.bottomSheetDragHandleWidth)
            .height(dimensions.bottomSheetDragHandleHeight)
            .clip(RoundedCornerShape(dimensions.bottomSheetDragHandleRadius))
            .background(color)
            .align(Alignment.CenterHorizontally),
    )
}

@Composable
private fun BottomSheetChrome(
    title: String,
    style: AndroidKitBottomSheetStyle,
    dimensions: AndroidKitDimensions,
    backContentDescription: String,
    onBack: (() -> Unit)?,
    closeContentDescription: String,
    onClose: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .padding(vertical = dimensions.spaceExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            BottomSheetIconButton(
                icon = AndroidKitIcons.ArrowBack,
                contentDescription = backContentDescription,
                tint = style.contentColor,
                dimensions = dimensions,
                onClick = onBack,
            )
            Spacer(modifier = Modifier.width(dimensions.bottomSheetBackTitleSpacing))
        }

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                color = style.contentColor,
                style = style.titleTextStyle
                    ?: AndroidKitThemeTokens.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(dimensions.bottomSheetHeaderCloseSpacing))
        BottomSheetIconButton(
            icon = AndroidKitIcons.Close,
            contentDescription = closeContentDescription,
            tint = style.contentColor,
            dimensions = dimensions,
            onClick = onClose,
        )
    }
}

@Composable
private fun BottomSheetIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    dimensions: AndroidKitDimensions,
    onClick: () -> Unit,
) {
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides dimensions.bottomSheetIconButtonSize,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(dimensions.bottomSheetIconButtonSize),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(dimensions.bottomSheetIconSize),
                tint = tint,
            )
        }
    }
}

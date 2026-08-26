package net.mamby.androidkit.compose.form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.mamby.androidkit.compose.theme.AndroidKitBottomSheetStyle
import net.mamby.androidkit.compose.theme.AndroidKitDimensions
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

public enum class AndroidKitBottomSheetScrollMode {
    VerticalScroll,
    ContentManaged,
}

public object AndroidKitBottomSheetDefaults {
    public const val DefaultMaxHeightFraction: Float = 0.85f
    public const val MaximumHeightFraction: Float = 0.90f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AndroidKitBottomSheet(
    visible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    maxHeightFraction: Float = AndroidKitBottomSheetDefaults.DefaultMaxHeightFraction,
    scrollMode: AndroidKitBottomSheetScrollMode = AndroidKitBottomSheetScrollMode.VerticalScroll,
    fitContent: Boolean = false,
    showChrome: Boolean = true,
    gesturesEnabled: Boolean = true,
    dismissGesturesEnabled: Boolean = true,
    closeContentDescription: String? = null,
    backContentDescription: String? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
): Unit {
    val dimensions = AndroidKitThemeTokens.dimensions
    val style = AndroidKitThemeTokens.bottomSheetStyle
    val floatingSurfaceStyle = AndroidKitThemeTokens.floatingSurfaceStyle
    val strings = AndroidKitThemeTokens.strings
    var renderSheet by remember { mutableStateOf(visible) }

    LaunchedEffect(visible) {
        if (visible) renderSheet = true
    }

    if (!renderSheet) return

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()
    val verticalScrollState = rememberScrollState()
    val cappedHeightFraction = maxHeightFraction.coerceIn(
        minimumValue = 0f,
        maximumValue = AndroidKitBottomSheetDefaults.MaximumHeightFraction,
    )
    val safeDrawingTopPadding = WindowInsets.safeDrawing
        .asPaddingValues()
        .calculateTopPadding()
    val sheetDismissGesturesEnabled = gesturesEnabled &&
        dismissGesturesEnabled &&
        (scrollMode != AndroidKitBottomSheetScrollMode.VerticalScroll ||
            verticalScrollState.value == 0)

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
        sheetMaxWidth = Dp.Unspecified,
        sheetGesturesEnabled = sheetDismissGesturesEnabled,
        shape = RoundedCornerShape(
            topStart = dimensions.bottomSheetCornerRadius,
            topEnd = dimensions.bottomSheetCornerRadius,
        ),
        containerColor = style.containerColor.copy(alpha = floatingSurfaceStyle.opacity),
        contentColor = style.contentColor,
        tonalElevation = 0.dp,
        scrimColor = style.scrimColor,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0.dp) },
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = onBack == null,
        ),
    ) {
        BackHandler(enabled = visible && onBack != null) {
            onBack?.invoke()
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val safeSheetHeight = (maxHeight - safeDrawingTopPadding).coerceAtLeast(0.dp)
            val maxSheetHeight = minOf(maxHeight * cappedHeightFraction, safeSheetHeight)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
                    .padding(
                        start = dimensions.bottomSheetHorizontalPadding,
                        top = dimensions.bottomSheetTopPadding,
                        end = dimensions.bottomSheetHorizontalPadding,
                    ),
            ) {
                BottomSheetDragHandle(
                    dimensions = dimensions,
                    color = style.dragHandleColor,
                )
                Spacer(modifier = Modifier.height(dimensions.bottomSheetDragHandleBottomSpacing))

                if (showChrome) {
                    BottomSheetChrome(
                        title = title,
                        style = style,
                        dimensions = dimensions,
                        backContentDescription = backContentDescription ?: strings.back,
                        onBack = onBack,
                        closeContentDescription = closeContentDescription ?: strings.close,
                        onClose = ::dismissWithAnimation,
                    )
                    Spacer(modifier = Modifier.height(dimensions.bottomSheetChromeContentSpacing))
                }

                when (scrollMode) {
                    AndroidKitBottomSheetScrollMode.VerticalScroll -> Column(
                        modifier = (if (fitContent) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        }).verticalScroll(verticalScrollState),
                    ) {
                        content()
                        Spacer(modifier = Modifier.height(dimensions.bottomSheetBottomPadding))
                    }

                    AndroidKitBottomSheetScrollMode.ContentManaged -> Column(
                        modifier = if (fitContent) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        },
                        content = content,
                    )
                }
            }
        }
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
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            BottomSheetIconButton(
                icon = BottomSheetBackIcon,
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
                style = AndroidKitThemeTokens.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(dimensions.bottomSheetHeaderCloseSpacing))
        BottomSheetIconButton(
            icon = BottomSheetCloseIcon,
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

private val BottomSheetBackIcon: ImageVector = ImageVector.Builder(
    name = "AndroidKitBottomSheetBack",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
    autoMirror = true,
).apply {
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.4f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ) {
        moveTo(20f, 12f)
        lineTo(4f, 12f)
        moveTo(11.5f, 4.5f)
        lineTo(4f, 12f)
        lineTo(11.5f, 19.5f)
    }
}.build()

private val BottomSheetCloseIcon: ImageVector = ImageVector.Builder(
    name = "AndroidKitBottomSheetClose",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ) {
        moveTo(6.75f, 6.75f)
        lineTo(17.25f, 17.25f)
        moveTo(17.25f, 6.75f)
        lineTo(6.75f, 17.25f)
    }
}.build()

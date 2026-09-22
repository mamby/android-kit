package net.mamby.androidkit.compose.form

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.AndroidKitDefaults
import net.mamby.androidkit.compose.theme.FloatingSurface
import net.mamby.androidkit.compose.theme.floatingSurfaceVisuals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Controlled search input. Hosts own placement, system/IME insets and query persistence.
 * Use AndroidKitFloatingAction.Search for measured placement in Kit pages and sheets.
 * Voice input appends live dictation using the device speech service without submitting.
 * Shape, typography, icons and control rendering are Kit-owned; shared theme tokens supply colors.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AndroidKitFloatingSearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    voiceInputEnabled: Boolean = true,
): Unit {
    val strings = AndroidKitThemeTokens.strings
    val dimensions = AndroidKitThemeTokens.dimensions
    val surfaceStyle = AndroidKitThemeTokens.floatingSurfaceStyle
    val visuals = floatingSurfaceVisuals(surfaceStyle)
    val shape = AndroidKitDefaults.shapes.extraLarge
    val keyboard = LocalSoftwareKeyboardController.current
    val imeVisible = WindowInsets.isImeVisible
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var fieldFocused by remember { mutableStateOf(false) }
    var previousImeVisible by remember { mutableStateOf(imeVisible) }
    LaunchedEffect(imeVisible) {
        if (previousImeVisible && !imeVisible && fieldFocused) focusManager.clearFocus()
        previousImeVisible = imeVisible
    }
    var fieldValue by remember { mutableStateOf(TextFieldValue(query, TextRange(query.length))) }
    val displayedValue = if (fieldValue.text == query) fieldValue
        else TextFieldValue(query, TextRange(query.length))
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val factory = LocalSearchSpeechInputFactory.current
    val currentEnabled by rememberUpdatedState(enabled && voiceInputEnabled)
    val currentQuery by rememberUpdatedState(query)
    val currentOnQueryChange by rememberUpdatedState(onQueryChange)
    var permissionPending by remember { mutableStateOf(false) }
    val tooltipState = rememberTooltipState(isPersistent = true)
    var errorPresentation by remember { mutableIntStateOf(0) }
    var focusError by remember { mutableStateOf(false) }
    var settingsLaunchFailed by remember { mutableStateOf(false) }
    val dictation = remember(context, lifecycleOwner, factory) {
        SearchDictation(
            createInput = { factory(context) },
            canPublish = {
                currentEnabled && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            },
            onQueryChange = { currentOnQueryChange(it) },
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val requested = permissionPending
        permissionPending = false
        if (requested && currentEnabled) {
            if (granted) dictation.start(currentQuery) else dictation.permissionDenied()
        }
    }
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            dictation.clearError()
        }
    }
    DisposableEffect(dictation, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                permissionPending = false
                dictation.cancel()
                tooltipState.dismiss()
            } else if (event == Lifecycle.Event.ON_RESUME && dictation.error == DictationError.Permission &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                dictation.clearError()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            permissionPending = false
            lifecycleOwner.lifecycle.removeObserver(observer)
            dictation.cancel()
        }
    }
    SideEffect {
        if (fieldValue.text != query) fieldValue = displayedValue
        if (!currentEnabled) {
            permissionPending = false
            dictation.cancel()
        } else dictation.hostQueryChanged(query)
    }
    BackHandler(enabled = dictation.active) { dictation.cancel() }

    val speechStatus = when (dictation.phase) {
        DictationPhase.Starting -> strings.voiceStarting
        DictationPhase.Listening -> strings.voiceListening
        DictationPhase.Finishing -> strings.voiceFinishing
        DictationPhase.Idle -> null
    }
    val visibleError = dictation.error.takeIf { enabled && voiceInputEnabled }
    val errorMessage = when (visibleError) {
        DictationError.Permission -> strings.voicePermission
        DictationError.NoSpeech -> strings.voiceNoSpeech
        DictationError.Unavailable -> strings.voiceSearchUnavailable
        null -> null
    }
    val accessibilityManager = LocalAccessibilityManager.current
    val errorTimeout = accessibilityManager?.calculateRecommendedTimeoutMillis(
        originalTimeoutMillis = SearchErrorDurationMillis,
        containsIcons = true,
        containsText = true,
        containsControls = true,
    ) ?: SearchErrorDurationMillis
    LaunchedEffect(visibleError, errorPresentation, errorTimeout) {
        if (visibleError != null) {
            val showing = launch { tooltipState.show() }
            try { delay(errorTimeout) } finally {
                tooltipState.dismiss()
                showing.cancel()
            }
        } else {
            tooltipState.dismiss()
            focusError = false
            settingsLaunchFailed = false
        }
    }
    val showErrorIndicator = visibleError == DictationError.Permission ||
        (visibleError != null && tooltipState.isVisible)
    TooltipBox(
        modifier = modifier.fillMaxWidth(),
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        state = tooltipState,
        enableUserInput = false,
        focusable = focusError,
        tooltip = {
            if (errorMessage != null) {
                RichTooltip(
                    action = {
                        FlowRow {
                            if (visibleError == DictationError.Permission) {
                                TextButton(onClick = {
                                    try {
                                        settingsLauncher.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null)))
                                        tooltipState.dismiss()
                                    } catch (_: ActivityNotFoundException) {
                                        settingsLaunchFailed = true
                                    } catch (_: SecurityException) {
                                        settingsLaunchFailed = true
                                    }
                                }) { Text(strings.openAppSettings) }
                            }
                            TextButton(onClick = { tooltipState.dismiss() }) { Text(strings.close) }
                        }
                    },
                ) {
                    Text(if (settingsLaunchFailed) strings.voiceSearchUnavailable else errorMessage,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
            }
        },
    ) {
        FloatingSurface(
            shape = shape,
            style = surfaceStyle,
        ) {
            if (dictation.active) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = TextFieldDefaults.MinHeight)
                        .padding(horizontal = dimensions.spaceSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(dimensions.minimumTouchTarget), contentAlignment = Alignment.Center) {
                        Icon(AndroidKitIcons.Microphone, null,
                            Modifier.size(dimensions.floatingActionIconSize),
                            tint = AndroidKitThemeTokens.colorScheme.primary)
                    }
                    val defaultStroke = WavyProgressIndicatorDefaults.linearIndicatorStroke
                    val thinStroke = remember(defaultStroke) {
                        Stroke(width = defaultStroke.width * SpeechWaveStrokeScale, cap = defaultStroke.cap)
                    }
                    LinearWavyProgressIndicator(
                        modifier = Modifier.weight(1f).padding(horizontal = dimensions.spaceMedium)
                            .semantics {
                                contentDescription = strings.voiceSearch
                                stateDescription = speechStatus.orEmpty()
                                liveRegion = LiveRegionMode.Polite
                            },
                        color = AndroidKitThemeTokens.colorScheme.primary,
                        stroke = thinStroke,
                        trackStroke = thinStroke,
                    )
                    IconButton(
                        enabled = enabled && dictation.phase != DictationPhase.Finishing,
                        onClick = { dictation.stop() },
                    ) {
                        Icon(AndroidKitIcons.Close, strings.voiceStop,
                            Modifier.size(dimensions.floatingActionIconSize))
                    }
                }
            } else TextField(
                value = displayedValue,
                onValueChange = {
                    fieldValue = it
                    if (it.text != query) {
                        dictation.cancel()
                        if (dictation.error != DictationError.Permission) dictation.clearError()
                        tooltipState.dismiss()
                        permissionPending = false
                        onQueryChange(it.text)
                    }
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    .onFocusChanged { fieldFocused = it.isFocused }
                    .semantics {
                        contentDescription = strings.search
                        if (showErrorIndicator && errorMessage != null) error(errorMessage)
                    },
                enabled = enabled,
                minLines = 1,
                maxLines = 3,
                textStyle = AndroidKitThemeTokens.typography.bodyLarge,
                shape = shape,
                placeholder = { Text(strings.search) },
                leadingIcon = {
                    if (showErrorIndicator) {
                        IconButton(onClick = {
                            focusError = true
                            settingsLaunchFailed = false
                            errorPresentation++
                        }) {
                            Icon(AndroidKitIcons.Info, strings.voiceInputError,
                                Modifier.size(dimensions.floatingActionIconSize), tint = AndroidKitThemeTokens.colorScheme.error)
                        }
                    } else {
                        Icon(AndroidKitIcons.Search, null, Modifier.size(dimensions.floatingActionIconSize))
                    }
                },
                trailingIcon = {
                    Row {
                        if (query.isNotEmpty()) {
                            IconButton(
                                enabled = enabled,
                                onClick = {
                                    dictation.cancel()
                                    if (dictation.error != DictationError.Permission) dictation.clearError()
                                    tooltipState.dismiss()
                                    permissionPending = false
                                    onQueryChange("")
                                    focusRequester.requestFocus()
                                    keyboard?.show()
                                },
                            ) {
                                Icon(AndroidKitIcons.Close, strings.clearSearch,
                                    Modifier.size(dimensions.floatingActionIconSize))
                            }
                        }
                        if (voiceInputEnabled) {
                            IconButton(
                                enabled = enabled && !permissionPending,
                                onClick = {
                                    if (!permissionPending) {
                                        errorPresentation++
                                        focusError = false
                                        settingsLaunchFailed = false
                                        dictation.clearError()
                                        keyboard?.hide()
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                                            PackageManager.PERMISSION_GRANTED) {
                                            dictation.start(query)
                                        } else {
                                            permissionPending = true
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    AndroidKitIcons.Microphone,
                                    strings.voiceSearch,
                                    Modifier.size(dimensions.floatingActionIconSize))
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (enabled && query.isNotBlank()) {
                        dictation.cancel()
                        permissionPending = false
                        keyboard?.hide()
                        onSearch(query)
                    }
                }),
                colors = TextFieldDefaults.colors(
                    cursorColor = if (imeVisible) AndroidKitThemeTokens.colorScheme.primary else Color.Transparent,
                    focusedTextColor = visuals.contentColor,
                    unfocusedTextColor = visuals.contentColor,
                    disabledTextColor = visuals.disabledContentColor,
                    focusedLeadingIconColor = visuals.contentColor,
                    unfocusedLeadingIconColor = visuals.contentColor,
                    disabledLeadingIconColor = visuals.disabledContentColor,
                    focusedTrailingIconColor = visuals.contentColor,
                    unfocusedTrailingIconColor = visuals.contentColor,
                    disabledTrailingIconColor = visuals.disabledContentColor,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
        }
    }
}

// Reading time for a short actionable error; extended by the accessibility timeout setting.
private const val SearchErrorDurationMillis: Long = 6_000L

// Keep the speech wave lighter than the standard loading indicator.
private const val SpeechWaveStrokeScale: Float = 0.5f

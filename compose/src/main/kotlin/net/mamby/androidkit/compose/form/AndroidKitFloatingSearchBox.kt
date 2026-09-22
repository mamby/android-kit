package net.mamby.androidkit.compose.form

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface
import net.mamby.androidkit.compose.theme.floatingSurfaceVisuals

/**
 * Controlled search input. Hosts own placement, system/IME insets and query persistence.
 * Use AndroidKitFloatingAction.Search for measured placement in Kit pages and sheets.
 * Voice input appends live dictation using the device speech service without submitting.
 * Shape, typography, icons and control rendering are Kit-owned; shared theme tokens supply colors.
 */
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
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
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
    DisposableEffect(dictation, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                permissionPending = false
                dictation.cancel()
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

    Column(modifier = modifier.fillMaxWidth()) {
        FloatingSurface(
            shape = CircleShape,
            style = surfaceStyle,
        ) {
            TextField(
                value = displayedValue,
                onValueChange = {
                    fieldValue = it
                    if (it.text != query) {
                        dictation.cancel()
                        dictation.clearError()
                        permissionPending = false
                        onQueryChange(it.text)
                    }
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    .semantics { contentDescription = strings.search },
                enabled = enabled,
                singleLine = true,
                textStyle = AndroidKitThemeTokens.typography.bodyLarge,
                shape = CircleShape,
                placeholder = { Text(strings.search) },
                leadingIcon = {
                    if (dictation.active) {
                        CircularProgressIndicator(modifier = Modifier.size(dimensions.floatingActionIconSize))
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
                                    dictation.clearError()
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
                                enabled = enabled && !permissionPending && dictation.phase != DictationPhase.Finishing,
                                onClick = {
                                    if (dictation.active) {
                                        dictation.stop()
                                    } else if (!permissionPending) {
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
                                    if (dictation.active) AndroidKitIcons.Stop else AndroidKitIcons.Microphone,
                                    if (dictation.active) strings.voiceStop else strings.voiceSearch,
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
        val status = when (dictation.phase) {
            DictationPhase.Starting -> strings.voiceStarting
            DictationPhase.Listening -> strings.voiceListening
            DictationPhase.Finishing -> strings.voiceFinishing
            DictationPhase.Idle -> when (dictation.error) {
                DictationError.Permission -> strings.voicePermission
                DictationError.NoSpeech -> strings.voiceNoSpeech
                DictationError.Unavailable -> strings.voiceSearchUnavailable
                null -> null
            }
        }
        if (status != null) {
            Text(
                text = status,
                color = if (dictation.error != null) AndroidKitThemeTokens.colorScheme.error
                    else AndroidKitThemeTokens.colorScheme.onSurfaceVariant,
                style = AndroidKitThemeTokens.typography.bodySmall,
                modifier = Modifier.padding(horizontal = dimensions.spaceMedium,
                    vertical = dimensions.spaceExtraSmall)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

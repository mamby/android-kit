package net.mamby.androidkit.compose.form

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitFloatingSearchBoxStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.FloatingSurface
import net.mamby.androidkit.compose.theme.floatingSurfaceVisuals

/**
 * Controlled search input. Hosts own placement, system/IME insets and query persistence.
 * Use AndroidKitFloatingAction.Search for measured placement in Kit pages and sheets.
 * Voice input opens the device recognizer; returned text replaces the query without submitting.
 */
@Composable
public fun AndroidKitFloatingSearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    voiceInputEnabled: Boolean = true,
    style: AndroidKitFloatingSearchBoxStyle = AndroidKitFloatingSearchBoxStyle(),
): Unit {
    val strings = AndroidKitThemeTokens.strings
    val dimensions = AndroidKitThemeTokens.dimensions
    val surfaceStyle = style.surfaceStyle ?: AndroidKitThemeTokens.floatingSurfaceStyle
    val visuals = floatingSurfaceVisuals(surfaceStyle)
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var voicePending by rememberSaveable { mutableStateOf(false) }
    var voiceError by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        voicePending = false
        if (result.resultCode == Activity.RESULT_OK && enabled && voiceInputEnabled) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()?.takeIf { it.isNotBlank() }?.let(onQueryChange)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        FloatingSurface(
            shape = style.shape,
            style = surfaceStyle,
        ) {
            TextField(
                value = query,
                onValueChange = {
                    voiceError = false
                    onQueryChange(it)
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    .semantics { contentDescription = strings.search },
                enabled = enabled,
                singleLine = true,
                textStyle = style.textStyle ?: AndroidKitThemeTokens.typography.bodyLarge,
                shape = style.shape,
                placeholder = { Text(strings.search) },
                leadingIcon = {
                    Icon(AndroidKitIcons.Search, null, Modifier.size(dimensions.floatingActionIconSize))
                },
                trailingIcon = {
                    Row {
                        if (query.isNotEmpty()) {
                            IconButton(
                                enabled = enabled,
                                onClick = {
                                    voiceError = false
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
                                enabled = enabled && !voicePending,
                                onClick = {
                                    if (!voicePending) {
                                        voiceError = false
                                        voicePending = true
                                        keyboard?.hide()
                                        try {
                                            launcher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            })
                                        } catch (_: ActivityNotFoundException) {
                                            voicePending = false
                                            voiceError = true
                                        } catch (_: SecurityException) {
                                            voicePending = false
                                            voiceError = true
                                        }
                                    }
                                },
                            ) {
                                Icon(AndroidKitIcons.Microphone, strings.voiceSearch,
                                    Modifier.size(dimensions.floatingActionIconSize))
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (enabled && query.isNotBlank()) {
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
        if (voiceError) {
            Text(
                text = strings.voiceSearchUnavailable,
                color = AndroidKitThemeTokens.colorScheme.error,
                style = AndroidKitThemeTokens.typography.bodySmall,
                modifier = Modifier.padding(horizontal = dimensions.spaceMedium,
                    vertical = dimensions.spaceExtraSmall)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

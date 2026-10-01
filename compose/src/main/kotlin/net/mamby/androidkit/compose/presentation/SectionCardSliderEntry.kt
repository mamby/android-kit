package net.mamby.androidkit.compose.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.ripple
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import net.mamby.androidkit.compose.theme.AndroidKitSettingSectionStyle
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SectionCardSliderEntry(
    entry: AndroidKitSectionCardEntry.Slider,
    style: AndroidKitSettingSectionStyle,
    colorsOverride: SliderColors?,
    isOpacitySlider: Boolean,
): Unit {
    val tokens = AndroidKitThemeTokens.componentTokens.sectionCardSlider
    val dimensions = AndroidKitThemeTokens.dimensions
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.trackSpacing),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(tokens.labelContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            entry.icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(tokens.supportingTextSpacing),
            ) {
                Text(
                    text = entry.label,
                    style = style.entryLabelTextStyle,
                )
                entry.supportingText?.let {
                    Text(
                        text = it,
                        style = style.supportingTextStyle,
                        color = style.secondaryContentColor,
                    )
                }
            }
            entry.valueLabel?.let {
                Text(
                    text = it,
                    style = style.valueLabelTextStyle,
                    color = style.secondaryContentColor,
                )
            }
        }
        val colors = colorsOverride ?: if (isOpacitySlider) {
            SliderDefaults.colors(thumbColor = AndroidKitThemeTokens.colorScheme.onPrimary)
        } else {
            SliderDefaults.colors()
        }
        val interactionSource = remember { MutableInteractionSource() }
        val sliderState = rememberSliderState(
            value = entry.value,
            steps = entry.steps,
            trackRange = entry.valueRange,
        )
        SideEffect {
            sliderState.value = entry.value
        }
        Slider(
            state = sliderState,
            onValueChange = entry.onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = entry.label },
            onValueChangeFinished = {
                entry.onValueChangeFinished?.invoke()
            },
            enabled = entry.enabled,
            colors = colors,
            interactionSource = interactionSource,
            thumb = {
                if (isOpacitySlider) {
                    Box(
                        Modifier
                            .size(tokens.opacityThumbSize)
                            .dropShadow(
                                shape = CircleShape,
                                shadow = Shadow(
                                    radius = dimensions.spaceExtraSmall,
                                    spread = 0.dp,
                                    offset = DpOffset.Zero,
                                    color = if (entry.enabled) {
                                        AndroidKitThemeTokens.colorScheme.outlineVariant
                                    } else {
                                        Color.Transparent
                                    },
                                ),
                            )
                            .clip(CircleShape)
                            .background(
                                color = if (entry.enabled) colors.thumbColor else colors.disabledThumbColor,
                                shape = CircleShape,
                            )
                            .indication(interactionSource, ripple()),
                    )
                } else {
                    SliderDefaults.Thumb(
                        interactionSource = interactionSource,
                        colors = colors,
                        enabled = entry.enabled,
                    )
                }
            },
            track = { sliderState ->
                if (isOpacitySlider) {
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        colors = colors,
                        enabled = entry.enabled,
                        drawStopIndicator = null,
                        drawTick = { _, _ -> },
                        thumbTrackGapSize = 0.dp,
                    )
                } else {
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        colors = colors,
                        enabled = entry.enabled,
                    )
                }
            },
        )
        if (entry.minimumLabel != null && entry.maximumLabel != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(entry.minimumLabel, style = style.supportingTextStyle, color = style.secondaryContentColor)
                Text(entry.maximumLabel, style = style.supportingTextStyle, color = style.secondaryContentColor)
            }
        }
    }
}

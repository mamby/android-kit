package net.mamby.androidkit.testing

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.LayoutDirection
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCard
import net.mamby.androidkit.compose.presentation.AndroidKitSectionCardEntry as Entry
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import net.mamby.androidkit.compose.theme.AndroidKitThemes
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** In-memory pixel comparison against an independent presentation using sealed Kit geometry. */
@RunWith(Parameterized::class)
class SectionCardPixelParityTest(
    private val dark: Boolean,
    private val direction: LayoutDirection,
    private val fontScale: Float,
) {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun contactValuesAndNotesMatchExistingPresentation() {
        var reference by mutableStateOf(true)
        var notes by mutableStateOf(false)
        val values = listOf("alex@example.org", "42 Example Street\nApartment 5\nExample City")
        val noteText = "Bring the revised plan.\n\nAsk about accessibility."
        rule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(fontScale)) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.LayoutDirection(direction)) {
                    val palette = if (dark) AndroidKitThemes.Dark else AndroidKitThemes.Light
                    // Hosts now share Kit geometry; the independent reference uses its defaults.
                    TestKitTheme(palette) {
                        Box(Modifier.width(320.dp).background(AndroidKitThemeTokens.colorScheme.background)
                            .testTag("section")) {
                            val title = if (notes) "Notes" else "Contact values"
                            if (reference) {
                                ExistingContactSection(title, values, noteText.takeIf { notes })
                            } else {
                                AndroidKitSectionCard(
                                    title = title,
                                    entries = if (notes) listOf(Entry.Multiline("notes", noteText)) else {
                                        values.map { Entry.Action(it, it, "Open value", {}) }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
        rule.waitUntil { rule.activity.hasWindowFocus() }
        for (showNotes in listOf(false, true)) {
            rule.runOnIdle { reference = true; notes = showNotes }
            val expected = rule.onNodeWithTag("section").captureToImage().toPixelMap()
            rule.runOnIdle { reference = false }
            val actual = rule.onNodeWithTag("section").captureToImage().toPixelMap()
            assertEquals("width, notes=$showNotes", expected.width, actual.width)
            assertEquals("height, notes=$showNotes", expected.height, actual.height)
            var differences = 0
            for (y in 0 until expected.height) {
                for (x in 0 until expected.width) {
                    if (expected[x, y] != actual[x, y]) differences++
                }
            }
            assertEquals("different pixels, notes=$showNotes", 0, differences)
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "dark={0}, direction={1}, fontScale={2}")
        fun configurations(): List<Array<Any>> = listOf(false, true).flatMap { dark ->
            listOf(LayoutDirection.Ltr, LayoutDirection.Rtl).flatMap { direction ->
                listOf(1f, 2f).map { scale -> arrayOf(dark, direction, scale) }
            }
        }
    }
}

/** Independent contact-section reference using the sealed Kit geometry.
 * Keep independent of the new component so typography, padding and affordance changes are caught.
 * Only fictional data is rendered and no screenshots are saved.
 */
@Composable
private fun ExistingContactSection(title: String, values: List<String>, notes: String?) {
    val style = AndroidKitThemeTokens.settingSectionStyle
    val dimensions = AndroidKitThemeTokens.dimensions
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.settingSectionSpacing)) {
        Text(
            title,
            modifier = Modifier.padding(horizontal = dimensions.sectionCardHorizontalPadding).semantics { heading() },
            style = style.sectionLabelTextStyle,
            color = style.secondaryContentColor,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = style.shape,
            colors = CardDefaults.cardColors(containerColor = style.containerColor, contentColor = style.contentColor),
            border = BorderStroke(style.borderWidth, style.borderColor),
        ) {
            if (notes != null) {
                Text(notes, Modifier.padding(horizontal = dimensions.sectionCardHorizontalPadding, vertical = dimensions.settingSectionEntryVerticalPadding), style = style.entryLabelTextStyle)
            } else {
                values.forEachIndexed { index, value ->
                    if (index > 0) {
                        HorizontalDivider(Modifier.padding(horizontal = dimensions.sectionCardHorizontalPadding), color = style.dividerColor)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = "Open value") {}
                            .heightIn(min = dimensions.minimumTouchTarget)
                            .padding(horizontal = dimensions.sectionCardHorizontalPadding, vertical = dimensions.settingSectionEntryVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(value, Modifier.fillMaxWidth(), style = style.entryLabelTextStyle)
                    }
                }
            }
        }
    }
}

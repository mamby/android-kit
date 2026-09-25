package net.mamby.androidkit.testing

import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import net.mamby.androidkit.compose.action.AndroidKitFloatingActionBar
import net.mamby.androidkit.compose.action.AndroidKitFloatingToolbarIconAndLabelLayout
import net.mamby.androidkit.compose.action.AndroidKitFloatingToolbarScope
import net.mamby.androidkit.compose.action.AndroidKitPageActionToolbar
import net.mamby.androidkit.compose.icon.AndroidKitIcons
import net.mamby.androidkit.compose.theme.AndroidKitTheme
import net.mamby.androidkit.compose.theme.AndroidKitThemeTokens
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ActionControlSpacingTest(
    private val pageActions: Boolean,
    private val direction: LayoutDirection,
    private val interactiveSize: Int,
    private val visualHeight: Int,
) {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun labeledLeadingIconAndTrailingMoreHaveEqualInsets() {
        var iconWidthPx = 0f
        var labelGapPx = 0f
        var moreLabel = ""
        rule.setContent {
            AndroidKitTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides direction,
                    LocalMinimumInteractiveComponentSize provides interactiveSize.dp,
                ) {
                    val dimensions = AndroidKitThemeTokens.dimensions
                    val iconSize = if (pageActions) {
                        dimensions.floatingActionIconSize
                    } else {
                        dimensions.floatingActionBarIconSize
                    }
                    with(LocalDensity.current) {
                        iconWidthPx = iconSize.roundToPx().toFloat()
                        labelGapPx = dimensions.spaceExtraSmall.roundToPx().toFloat()
                    }
                    moreLabel = AndroidKitThemeTokens.strings.more
                    val actions: AndroidKitFloatingToolbarScope.() -> Unit = {
                        iconAndLabel(
                            onClick = {},
                            icon = AndroidKitIcons.Check,
                            label = "Save",
                            layout = AndroidKitFloatingToolbarIconAndLabelLayout.Horizontal,
                        )
                        flyout {
                            item(label = "Other action", onClick = {})
                        }
                    }
                    if (pageActions) {
                        AndroidKitPageActionToolbar(
                            visualHeight = visualHeight.dp,
                            modifier = Modifier.testTag("actions"),
                            style = AndroidKitThemeTokens.floatingToolbarStyle.copy(iconSize = iconSize),
                            content = actions,
                        )
                    } else {
                        AndroidKitFloatingActionBar(
                            modifier = Modifier.testTag("actions"),
                            content = actions,
                        )
                    }
                }
            }
        }

        val bar = rule.onNodeWithTag("actions").fetchSemanticsNode().boundsInRoot
        val label = rule.onNodeWithText("Save", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        val moreIcon = rule.onNodeWithContentDescription(moreLabel, useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        val leadingInset = if (direction == LayoutDirection.Ltr) {
            label.left - labelGapPx - iconWidthPx - bar.left
        } else {
            bar.right - (label.right + labelGapPx + iconWidthPx)
        }
        val trailingInset = if (direction == LayoutDirection.Ltr) {
            bar.right - moreIcon.right
        } else {
            moreIcon.left - bar.left
        }
        // Layout rounding can allocate the odd pixel to either side of a centered icon.
        assertEquals("Save and More must have the same edge inset", trailingInset, leadingInset, 1f)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "page={0}, direction={1}, target={2}, height={3}")
        fun configurations(): List<Array<Any>> = buildList {
            for (direction in LayoutDirection.entries) {
                add(arrayOf(false, direction, 48, 0))
                add(arrayOf(false, direction, 64, 0))
                add(arrayOf(true, direction, 48, 44))
                add(arrayOf(true, direction, 48, 60))
            }
        }
    }
}

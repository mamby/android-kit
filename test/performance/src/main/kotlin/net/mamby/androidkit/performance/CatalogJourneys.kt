package net.mamby.androidkit.performance

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.onElement
import androidx.test.uiautomator.onElementOrNull
import androidx.test.uiautomator.simpleViewResourceName
import androidx.test.uiautomator.uiAutomator
import androidx.test.uiautomator.waitForStable

internal const val DemoPackageName = "net.mamby.androidkit.demo"

private const val CatalogListTag = "catalog_list"
private const val FirstCatalogEntryTag = "component_demo_androidkitpagebasic"
private const val StandardBottomSheetEntryTag =
    "component_demo_androidkitbottomsheetstandard"
private const val OpenBottomSheetTag = "open_bottom_sheet"
private const val BottomSheetTag = "bottom_sheet"

internal fun MacrobenchmarkScope.launchCatalog() {
    pressHome()
    startActivityAndWait()
    returnToCatalog()
}

internal fun MacrobenchmarkScope.waitForCatalog() {
    uiAutomator {
        onElement { simpleViewResourceName() == CatalogListTag }
    }
}

internal fun MacrobenchmarkScope.scrollCatalog() {
    uiAutomator {
        val catalog = onElement { simpleViewResourceName() == CatalogListTag }
        catalog.setGestureMarginPercentage(CatalogGestureMarginFraction)
        catalog.scrollToTaggedElement(Direction.UP, FirstCatalogEntryTag)
        catalog.scrollToTaggedElement(Direction.DOWN, StandardBottomSheetEntryTag)
        catalog.scrollToTaggedElement(Direction.UP, FirstCatalogEntryTag)
    }
}

internal fun MacrobenchmarkScope.openAndDismissBottomSheet() {
    uiAutomator {
        val catalog = onElement { simpleViewResourceName() == CatalogListTag }
        catalog.setGestureMarginPercentage(CatalogGestureMarginFraction)
        catalog.scrollToTaggedElement(Direction.UP, FirstCatalogEntryTag)
        catalog.scrollToTaggedElement(Direction.DOWN, StandardBottomSheetEntryTag)
        catalog.scroll(Direction.DOWN, CatalogScrollStepFraction)
        catalog.onElement {
            simpleViewResourceName() == StandardBottomSheetEntryTag
        }
            .click()

        onElement { simpleViewResourceName() == OpenBottomSheetTag }.click()
        onElement { simpleViewResourceName() == BottomSheetTag }
            .onElement { isClickable }
            .click()
        activeWindowRoot().waitForStable(requireStableScreenshot = false)
    }

    device.pressBack()
    waitForCatalog()
}

private fun UiObject2.scrollToTaggedElement(
    direction: Direction,
    tag: String,
): UiObject2 {
    var target = onElementOrNull(timeoutMs = CatalogElementProbeTimeoutMillis) {
        simpleViewResourceName() == tag
    }
    var remainingSteps = MaximumCatalogScrollSteps
    while (target == null && remainingSteps > 0) {
        scroll(direction, CatalogScrollStepFraction)
        target = onElementOrNull(timeoutMs = CatalogElementProbeTimeoutMillis) {
            simpleViewResourceName() == tag
        }
        remainingSteps--
    }
    return target ?: onElement(timeoutMs = CatalogElementProbeTimeoutMillis) {
        simpleViewResourceName() == tag
    }
}

private fun MacrobenchmarkScope.returnToCatalog() {
    repeat(MaximumBackNavigationAttempts) {
        if (catalogIsVisible()) return
        device.pressBack()
    }
    waitForCatalog()
}

private fun catalogIsVisible(): Boolean {
    var visible = false
    uiAutomator {
        visible = onElementOrNull(timeoutMs = ElementProbeTimeoutMillis) {
            simpleViewResourceName() == CatalogListTag
        } != null
    }
    return visible
}

private const val MaximumBackNavigationAttempts = 3
private const val ElementProbeTimeoutMillis = 500L
private const val MaximumCatalogScrollSteps = 30
private const val CatalogScrollStepFraction = 0.45f
private const val CatalogGestureMarginFraction = 0.2f
private const val CatalogElementProbeTimeoutMillis = 500L

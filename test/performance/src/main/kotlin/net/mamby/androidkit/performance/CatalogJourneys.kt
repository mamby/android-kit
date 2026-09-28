package net.mamby.androidkit.performance

import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Trace
import android.view.accessibility.AccessibilityWindowInfo
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Condition
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
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
private const val SearchPageEntryTag = "component_demo_androidkitsearchpageinteractive"

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

internal fun MacrobenchmarkScope.openSearchPage() {
    device.wakeUp()
    launchCatalog()
    uiAutomator {
        val catalog = onElement { simpleViewResourceName() == CatalogListTag }
        catalog.setGestureMarginPercentage(CatalogGestureMarginFraction)
        catalog.scrollToTaggedElement(Direction.DOWN, SearchPageEntryTag).click()
        onElement { packageName == DemoPackageName && isEditable }
    }
    waitForKeyboard(visible = true)
    device.pressBack()
    waitForKeyboard(visible = false)
}

internal fun MacrobenchmarkScope.toggleSearchKeyboard() {
    Trace.beginSection("SearchKeyboardShow")
    try {
        uiAutomator { onElement { packageName == DemoPackageName && isEditable }.click() }
        waitForKeyboard(visible = true)
    } finally {
        Trace.endSection()
    }
    Trace.beginSection("SearchKeyboardHide")
    try {
        device.pressBack()
        waitForKeyboard(visible = false)
    } finally {
        Trace.endSection()
    }
}

private fun MacrobenchmarkScope.waitForKeyboard(visible: Boolean) {
    val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
    automation.serviceInfo = automation.serviceInfo.apply {
        flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
    }
    check(device.wait(object : Condition<UiDevice, Boolean> {
        override fun apply(device: UiDevice): Boolean = automation.windows.any {
            it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD
        } == visible
    }, KeyboardTransitionTimeoutMillis) == true) {
        "Keyboard did not become ${if (visible) "visible" else "hidden"}"
    }
    uiAutomator {
        onElement { packageName == DemoPackageName && isEditable }
            .waitForStable(requireStableScreenshot = false)
    }
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
private const val KeyboardTransitionTimeoutMillis = 5_000L

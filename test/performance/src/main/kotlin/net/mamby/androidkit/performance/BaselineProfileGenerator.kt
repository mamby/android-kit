package net.mamby.androidkit.performance

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startup() = baselineProfileRule.collect(
        packageName = DemoPackageName,
        includeInStartupProfile = true,
    ) {
        launchCatalog()
    }

    @Test
    fun criticalUserJourneys() = baselineProfileRule.collect(
        packageName = DemoPackageName,
    ) {
        launchCatalog()
        scrollCatalog()
        openAndDismissBottomSheet()
    }
}

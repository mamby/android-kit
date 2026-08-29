package net.mamby.androidkit.performance

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MemoryUsageMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class InteractionBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun catalogScrolling() = benchmarkRule.measureRepeated(
        packageName = DemoPackageName,
        metrics = InteractionMetrics,
        compilationMode = CompilationMode.Partial(),
        iterations = InteractionIterations,
        setupBlock = { launchCatalog() },
        measureBlock = { scrollCatalog() },
    )

    @Test
    fun navigationAndBottomSheet() = benchmarkRule.measureRepeated(
        packageName = DemoPackageName,
        metrics = InteractionMetrics,
        compilationMode = CompilationMode.Partial(),
        iterations = InteractionIterations,
        setupBlock = { launchCatalog() },
        measureBlock = { openAndDismissBottomSheet() },
    )
}

@OptIn(ExperimentalMetricApi::class)
private val InteractionMetrics = listOf(
    FrameTimingMetric(),
    MemoryUsageMetric(MemoryUsageMetric.Mode.Max),
)

private const val InteractionIterations = 5

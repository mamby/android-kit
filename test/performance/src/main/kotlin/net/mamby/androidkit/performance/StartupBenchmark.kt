package net.mamby.androidkit.performance

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartupWithoutCompilation() = benchmarkStartup(CompilationMode.None())

    @Test
    fun coldStartupWithBaselineProfile() = benchmarkStartup(CompilationMode.Partial())

    private fun benchmarkStartup(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = DemoPackageName,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = StartupIterations,
            setupBlock = { pressHome() },
        ) {
            startActivityAndWait()
            waitForCatalog()
        }
    }
}

private const val StartupIterations = 10

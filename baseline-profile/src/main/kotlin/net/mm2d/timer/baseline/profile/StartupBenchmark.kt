/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.baseline.profile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupCompilationNone() = benchmark(CompilationMode.None())

    @Test
    fun startupCompilationBaselineProfiles() =
        benchmark(
            CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require,
            ),
        )

    private fun benchmark(
        compilationMode: CompilationMode,
    ) {
        val packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: DEFAULT_PACKAGE_NAME
        benchmarkRule.measureRepeated(
            packageName = packageName,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            iterations = 10,
            startupMode = StartupMode.COLD,
            setupBlock = {
                pressHome()
            },
        ) {
            startActivityAndWait()
            device.wait(Until.hasObject(By.desc("Open settings")), TIMEOUT)
        }
    }

    private companion object {
        private const val DEFAULT_PACKAGE_NAME = "net.mm2d.timer"
        private const val TIMEOUT = 5_000L
    }
}

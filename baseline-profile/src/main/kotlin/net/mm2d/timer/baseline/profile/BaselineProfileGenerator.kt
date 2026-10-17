/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.baseline.profile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.SearchCondition
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        val packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: DEFAULT_PACKAGE_NAME
        baselineProfileRule.collect(
            packageName = packageName,
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()
            waitForObject(By.desc("Open settings"))

            exerciseStopwatchOnMain()
            openSettingsFromMain()
            exerciseLicenseFromSettings()
            exerciseColorSelectFromSettings()
            exerciseModeSwitchInSettings()
            exerciseDialogsInSettings()
            returnToMainFromSettings()
            exerciseTimerOnMain()
            restoreDefaultSettings()
        }
    }

    private fun MacrobenchmarkScope.exerciseStopwatchOnMain() {
        clickAndWait(By.desc("Start"), Until.hasObject(By.desc("Pause")))
        clickAndWait(By.desc("Pause"), Until.hasObject(By.desc("Start")))
        clickObject(By.desc("Reset"))
    }

    private fun MacrobenchmarkScope.openSettingsFromMain() {
        clickObject(By.desc("Open settings"))
        device.wait(Until.gone(By.desc("Open settings")), TIMEOUT)
        waitForObject(By.text(SETTINGS_ANCHOR_TEXT))
    }

    private fun MacrobenchmarkScope.exerciseLicenseFromSettings() {
        val overflowButton = device.findObjects(By.clazz("android.widget.Button"))
            .maxByOrNull { it.visibleBounds.centerX() }
            ?: return
        overflowButton.click()
        device.waitForIdle()
        clickObject(By.text("Open source licenses"))
        device.wait(Until.gone(By.text(SETTINGS_ANCHOR_TEXT)), TIMEOUT)
        waitForObject(By.text("Open source licenses"))
        device.pressBack()
        device.wait(Until.gone(By.text("Open source licenses")), TIMEOUT)
        waitForObject(By.text(SETTINGS_ANCHOR_TEXT))
    }

    private fun MacrobenchmarkScope.exerciseColorSelectFromSettings() {
        clickObject(By.text("Foreground color"))
        device.wait(Until.gone(By.text(SETTINGS_ANCHOR_TEXT)), TIMEOUT)
        waitForObject(By.text("Cancel"))
        device.findObject(By.text("HSV"))?.let {
            it.click()
            device.waitForIdle()
        }
        device.findObject(By.text("RGB"))?.let {
            it.click()
            device.waitForIdle()
        }
        clickObject(By.text("Cancel"))
        device.wait(Until.gone(By.text("Cancel")), TIMEOUT)
        waitForObject(By.text(SETTINGS_ANCHOR_TEXT))
    }

    private fun MacrobenchmarkScope.exerciseModeSwitchInSettings() {
        clickAndWait(By.text("clock"), Until.hasObject(By.text("Use 24-hour format")))
        clickAndWait(By.text("timer"), Until.hasObject(By.text("Display of \"hour\"")))
    }

    private fun MacrobenchmarkScope.exerciseDialogsInSettings() {
        scrollSettingsDown()
        clickAndWait(By.text("Font"), Until.hasObject(By.text("Roboto")))
        clickObject(By.text("Roboto"))
        device.wait(Until.gone(By.text("Cancel")), TIMEOUT)
        clickAndWait(By.text("Screen Orientation"), Until.hasObject(By.text("Cancel")))
        clickObject(By.text("Cancel"))
        device.wait(Until.gone(By.text("Cancel")), TIMEOUT)
    }

    private fun MacrobenchmarkScope.returnToMainFromSettings() {
        device.pressBack()
        device.wait(Until.gone(By.text("Screen Orientation")), TIMEOUT)
        waitForObject(By.desc("Open settings"))
    }

    private fun MacrobenchmarkScope.exerciseTimerOnMain() {
        clickAndWait(By.desc("Set timer"), Until.hasObject(By.text("3 min")))
        clickObject(By.text("3 min"))
        clickObject(By.text("1 min"))
        clickObject(By.text("OK"))
        device.wait(Until.gone(By.text("OK")), TIMEOUT)
        clickAndWait(By.desc("Start"), Until.hasObject(By.desc("Pause")))
        clickAndWait(By.desc("Pause"), Until.hasObject(By.desc("Start")))
    }

    private fun MacrobenchmarkScope.restoreDefaultSettings() {
        openSettingsFromMain()
        clickAndWait(By.text("clock"), Until.hasObject(By.text("Use 24-hour format")))
        device.pressBack()
        device.wait(Until.gone(By.text(SETTINGS_ANCHOR_TEXT)), TIMEOUT)
        waitForObject(By.desc("Open settings"))

        openSettingsFromMain()
        clickAndWait(By.text("stopwatch"), Until.hasObject(By.text("Display of \"hour\"")))
        scrollSettingsDown()
        clickAndWait(By.text("Font"), Until.hasObject(By.text("7seg-LED")))
        clickObject(By.text("7seg-LED"))
        device.wait(Until.gone(By.text("Cancel")), TIMEOUT)
        returnToMainFromSettings()
        waitForObject(By.desc("Reset"))
    }

    private fun MacrobenchmarkScope.scrollSettingsDown() {
        device.swipe(
            device.displayWidth / 2,
            device.displayHeight * 3 / 4,
            device.displayWidth / 2,
            device.displayHeight / 4,
            10,
        )
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.waitForObject(
        selector: BySelector,
    ) {
        checkNotNull(device.wait(Until.findObject(selector), TIMEOUT)) {
            "Object not found: $selector"
        }
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.clickObject(
        selector: BySelector,
    ) {
        val obj = checkNotNull(device.wait(Until.findObject(selector), TIMEOUT)) {
            "Clickable object not found: $selector"
        }
        obj.click()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.clickAndWait(
        selector: BySelector,
        condition: SearchCondition<Boolean>,
    ) {
        clickObject(selector)
        check(device.wait(condition, TIMEOUT)) {
            "Condition not met after clicking: $selector"
        }
        device.waitForIdle()
    }

    private companion object {
        private const val DEFAULT_PACKAGE_NAME = "net.mm2d.timer"
        private const val SETTINGS_ANCHOR_TEXT = "Set the color of the clock text"
        private const val TIMEOUT = 5_000L
    }
}

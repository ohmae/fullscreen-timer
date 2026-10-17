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
        clickAndWait(By.desc("Open settings"), Until.hasObject(By.text(SETTINGS_ANCHOR_TEXT)))
        device.wait(Until.gone(By.desc("Open settings")), TIMEOUT)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.exerciseLicenseFromSettings() {
        openOverflowMenuInSettings()
        clickAndWait(By.text("Open source licenses"), Until.gone(By.text(SETTINGS_ANCHOR_TEXT)))
        waitForObject(By.text("Open source licenses"))
        device.pressBack()
        device.wait(Until.gone(By.text("Open source licenses")), TIMEOUT)
        waitForObject(By.text(SETTINGS_ANCHOR_TEXT))
    }

    private fun MacrobenchmarkScope.openOverflowMenuInSettings() {
        repeat(MAX_CLICK_ATTEMPTS) {
            val overflowButton = device.findObjects(By.clazz("android.widget.Button"))
                .maxByOrNull { it.visibleBounds.centerX() }
                ?: return
            overflowButton.click()
            if (device.wait(Until.hasObject(By.text("Open source licenses")), RETRY_TIMEOUT)) {
                device.waitForIdle()
                return
            }
        }
        error("Failed to open overflow menu in Settings")
    }

    private fun MacrobenchmarkScope.exerciseColorSelectFromSettings() {
        clickAndWait(By.text("Foreground color"), Until.hasObject(By.text("Cancel")))
        device.wait(Until.gone(By.text(SETTINGS_ANCHOR_TEXT)), TIMEOUT)
        device.findObject(By.text("HSV"))?.let {
            it.click()
            device.waitForIdle()
        }
        device.findObject(By.text("RGB"))?.let {
            it.click()
            device.waitForIdle()
        }
        clickAndWait(By.text("Cancel"), Until.hasObject(By.text(SETTINGS_ANCHOR_TEXT)))
        device.wait(Until.gone(By.text("Cancel")), TIMEOUT)
    }

    private fun MacrobenchmarkScope.exerciseModeSwitchInSettings() {
        clickAndWait(By.text("clock"), Until.hasObject(By.text("Use 24-hour format")))
        clickAndWait(By.text("timer"), Until.hasObject(By.text("Display of \"hour\"")))
    }

    private fun MacrobenchmarkScope.exerciseDialogsInSettings() {
        ensureVisible(By.text("Font"))
        clickAndWait(By.text("Font"), Until.hasObject(By.text("Cancel")))
        clickAndWait(By.text("Roboto"), Until.gone(By.text("Cancel")))
        ensureVisible(By.text("Screen Orientation"))
        clickAndWait(By.text("Screen Orientation"), Until.hasObject(By.text("Cancel")))
        clickAndWait(By.text("Cancel"), Until.gone(By.text("Cancel")))
    }

    private fun MacrobenchmarkScope.returnToMainFromSettings() {
        device.pressBack()
        waitForObject(By.desc("Open settings"))
        device.wait(Until.gone(By.text(SETTINGS_ANCHOR_TEXT)), TIMEOUT)
    }

    private fun MacrobenchmarkScope.exerciseTimerOnMain() {
        clickAndWait(By.desc("Set timer"), Until.hasObject(By.text("3 min")))
        clickObject(By.text("3 min"))
        clickObject(By.text("1 min"))
        clickAndWait(By.text("OK"), Until.gone(By.text("OK")))
        clickAndWait(By.desc("Start"), Until.hasObject(By.desc("Pause")))
        clickAndWait(By.desc("Pause"), Until.hasObject(By.desc("Start")))
    }

    private fun MacrobenchmarkScope.restoreDefaultSettings() {
        openSettingsFromMain()
        clickAndWait(By.text("clock"), Until.hasObject(By.text("Use 24-hour format")))
        returnToMainFromSettings()

        openSettingsFromMain()
        clickAndWait(By.text("stopwatch"), Until.hasObject(By.text("Display of \"hour\"")))
        ensureVisible(By.text("Font"))
        clickAndWait(By.text("Font"), Until.hasObject(By.text("Cancel")))
        clickAndWait(By.text("7seg-LED"), Until.gone(By.text("Cancel")))
        returnToMainFromSettings()
        waitForObject(By.desc("Reset"))
    }

    private fun MacrobenchmarkScope.ensureVisible(
        selector: BySelector,
    ) {
        if (device.hasObject(selector)) return
        device.swipe(
            device.displayWidth / 2,
            device.displayHeight * 3 / 4,
            device.displayWidth / 2,
            device.displayHeight / 4,
            SWIPE_STEPS,
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
        repeat(MAX_CLICK_ATTEMPTS) {
            clickObject(selector)
            if (device.wait(condition, RETRY_TIMEOUT)) {
                device.waitForIdle()
                return
            }
        }
        error("Condition not met after clicking: $selector")
    }

    private companion object {
        private const val DEFAULT_PACKAGE_NAME = "net.mm2d.timer"
        private const val SETTINGS_ANCHOR_TEXT = "Set the color of the clock text"
        private const val TIMEOUT = 5_000L
        private const val RETRY_TIMEOUT = 2_500L
        private const val MAX_CLICK_ATTEMPTS = 3
        private const val SWIPE_STEPS = 30
    }
}

/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.delegate

import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.isInvisible
import androidx.fragment.app.FragmentActivity
import net.mm2d.timer.databinding.ActivityMainBinding
import net.mm2d.timer.settings.Mode

class ClockDelegate(
    private val activity: FragmentActivity,
    private val binding: ActivityMainBinding,
) : ModeDelegate {
    private val delegateViewModel: ClockViewModel by activity.viewModels()
    private var enabled: Boolean = false
    private val task = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            binding.clock.updateClock(now)
            val delay = SECOND_IN_MILLIS - now % SECOND_IN_MILLIS
            if (enabled) binding.clock.postDelayed(this, delay)
        }
    }

    init {
        delegateViewModel.uiStateLiveData.observe(activity) {
            onModeChanged(it.mode)
        }
    }

    override fun onClickButton1() = Unit
    override fun onClickButton2() = Unit
    override fun onClickTime() = Unit
    override fun onStop() = Unit

    private fun onModeChanged(mode: Mode) {
        val enable = mode == Mode.CLOCK
        if (enable == enabled) return
        enabled = enable
        binding.clock.removeCallbacks(task)
        if (!enable) return
        binding.button1.isInvisible = true
        binding.button2.isInvisible = true
        binding.clock.setDigit(small = true, third = false)
        task.run()
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        enabled = false
        binding.clock.removeCallbacks(task)
    }

    companion object {
        private const val SECOND_IN_MILLIS = 1000
    }
}

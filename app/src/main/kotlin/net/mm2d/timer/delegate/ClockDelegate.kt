/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.delegate

import android.content.Intent
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.isInvisible
import androidx.fragment.app.FragmentActivity
import net.mm2d.timer.databinding.ActivityMainBinding
import net.mm2d.timer.delegate.ClockViewModel.UiState
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.util.observe

class ClockDelegate(
    private val activity: FragmentActivity,
    private val binding: ActivityMainBinding,
) : ModeDelegate {
    private val delegateViewModel: ClockViewModel by activity.viewModels()
    private var isActive: Boolean = false
    override val mode: Mode = Mode.CLOCK

    private val task = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            binding.clock.updateClock(now)
            val delay = SECOND_IN_MILLIS - now % SECOND_IN_MILLIS
            if (isActive) binding.clock.postDelayed(this, delay)
        }
    }

    init {
        delegateViewModel.uiStateFlow.observe(activity) {
            onModeChanged(it)
        }
    }

    override fun handleIntent(
        intent: Intent,
    ) = Unit
    override fun onClickButton1() = Unit
    override fun onClickButton2() = Unit
    override fun onClickTime() = Unit
    override fun onStop() = Unit

    private fun onModeChanged(
        uiState: UiState,
    ) {
        val active = uiState.mode == this.mode
        if (active) {
            binding.clock.setDigit(hourFormat24 = uiState.hourFormat24, small = uiState.secondEnabled)
            if (isActive) binding.clock.updateClock(System.currentTimeMillis())
        }
        if (active == isActive) return
        isActive = active
        binding.clock.removeCallbacks(task)
        if (!active) return
        binding.button1.isInvisible = true
        binding.button2.isInvisible = true
        task.run()
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        isActive = false
        binding.clock.removeCallbacks(task)
    }

    companion object {
        private const val SECOND_IN_MILLIS = 1000
    }
}

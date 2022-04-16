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
import net.mm2d.timer.R
import net.mm2d.timer.databinding.ActivityMainBinding
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.StopwatchRunningState
import net.mm2d.timer.util.observeOnce

class StopwatchDelegate(
    private val activity: FragmentActivity,
    private val binding: ActivityMainBinding,
) : ModeDelegate {
    private val delegateViewModel: StopwatchViewModel by activity.viewModels()
    private var enabled: Boolean = false
    private var started: Boolean = false
    private var start: Long = 0L
    private var milestone: Long = 0L
    private var hourEnabled: Boolean = false
    private var max: Long = MAX_WITHOUT_HOUR
    private var restoreLatch: Boolean = false

    private val task = object : Runnable {
        override fun run() {
            val time = milestone + System.currentTimeMillis() - start
            if (time >= max) {
                stop()
                return
            }
            binding.clock.updateTime(time)
            val delay = TIMER_INTERVAL - time % TIMER_INTERVAL
            if (enabled && started) binding.clock.postDelayed(this, delay)
        }
    }

    init {
        delegateViewModel.uiStateLiveData.observe(activity) {
            onModeChanged(it.mode)
            setHourEnabled(it.hourEnabled)
            restore()
        }
    }

    private fun restore() {
        if (restoreLatch) return
        restoreLatch = true
        delegateViewModel.runningStateLiveData.observeOnce(activity) {
            if (!it.started) return@observeOnce
            delegateViewModel.updateState(StopwatchRunningState(started = false))
            if (!enabled) return@observeOnce
            prepareStart()
            start = it.start
            milestone = it.milestone
            task.run()
        }
    }

    override fun onClickButton1() {
        if (!enabled) return
        if (started) {
            stop()
        } else {
            start()
        }
        delegateViewModel.playSound()
    }

    override fun onClickButton2() {
        if (!enabled) return
        if (started) stop()
        milestone = 0L
        binding.clock.updateTime(milestone)
        delegateViewModel.playSound()
    }

    override fun onClickTime() = onClickButton1()

    override fun onStop() {
        if (!started) return
        delegateViewModel.updateState(
            StopwatchRunningState(
                started = started,
                start = start,
                milestone = milestone,
            )
        )
    }

    override fun onDestroy() {
        enabled = false
        binding.clock.removeCallbacks(task)
    }

    private fun prepareStart() {
        started = true
        binding.button1.setImageResource(R.drawable.ic_pause)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun start() {
        prepareStart()
        start = System.currentTimeMillis()
        task.run()
    }

    private fun stop() {
        started = false
        binding.button1.setImageResource(R.drawable.ic_start)
        milestone = (milestone + System.currentTimeMillis() - start).coerceAtMost(max)
        binding.clock.updateTime(milestone)
        binding.clock.removeCallbacks(task)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setHourEnabled(hourEnabled: Boolean) {
        this.hourEnabled = hourEnabled
        max = if (hourEnabled) MAX_WITH_HOUR else MAX_WITHOUT_HOUR
        if (!enabled) return
        binding.clock.setDigit(small = true, third = hourEnabled)
    }

    private fun onModeChanged(mode: Mode) {
        val enable = mode == Mode.STOPWATCH
        if (enable == enabled) return
        enabled = enable
        binding.clock.removeCallbacks(task)
        started = false
        if (!enable) return
        milestone = 0L
        binding.button1.isInvisible = false
        binding.button1.setImageResource(R.drawable.ic_start)
        binding.button2.isInvisible = false
        binding.button2.setImageResource(R.drawable.ic_reset)
        binding.clock.setDigit(small = true, third = hourEnabled)
        binding.clock.updateTime(0)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    companion object {
        private const val TIMER_INTERVAL = 10
        private const val MAX_WITH_HOUR = 10 * 3600_000L - 10
        private const val MAX_WITHOUT_HOUR = 100 * 60_000L - 10
    }
}

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
import net.mm2d.timer.dialog.TimeDialog
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.TimerRunningState
import net.mm2d.timer.util.observeOnce

class TimerDelegate(
    private val activity: FragmentActivity,
    private val binding: ActivityMainBinding,
) : ModeDelegate {
    private val delegateViewModel: TimerViewModel by activity.viewModels()
    private var enabled: Boolean = false
    private var started: Boolean = false
    private var start: Long = 0L
    private var milestone: Long = 0L
    private var hourEnabled: Boolean = false
    private var timerTime: Long = 0L
    private var restoreLatch: Boolean = false

    private val task = object : Runnable {
        override fun run() {
            val time = milestone - (System.currentTimeMillis() - start)
            if (time <= 0) {
                stop()
                delegateViewModel.playSound()
                return
            }
            binding.clock.updateTime(time)
            val delay = time % TIMER_INTERVAL
            if (enabled && started) binding.clock.postDelayed(this, delay)
        }
    }

    init {
        delegateViewModel.uiStateLiveData.observe(activity) {
            onModeChanged(it.mode)
            setHourEnabled(it.hourEnabled)
            setTimerTime(it.timerTime)
            restore()
        }
        TimeDialog.registerListener(activity, REQUEST_KEY) {
            delegateViewModel.updateTimerTime(it)
            if (timerTime == it) {
                setTimerTime(it)
            }
        }
    }

    private fun restore() {
        if (restoreLatch) return
        restoreLatch = true
        delegateViewModel.runningStateLiveData.observeOnce(activity) {
            if (!it.started) return@observeOnce
            delegateViewModel.updateState(TimerRunningState(started = false))
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
        if (started) {
            stop()
            delegateViewModel.playSound()
        }
        TimeDialog.show(activity, REQUEST_KEY, timerTime, hourEnabled)
    }

    override fun onClickTime() {
        if (milestone == 0L) {
            onClickButton2()
        } else {
            onClickButton1()
        }
    }

    override fun onStop() {
        if (!started) return
        delegateViewModel.updateState(
            TimerRunningState(
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
        milestone = (milestone - (System.currentTimeMillis() - start)).coerceAtLeast(0)
        binding.clock.updateTime(milestone)
        binding.clock.removeCallbacks(task)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setHourEnabled(hourEnabled: Boolean) {
        this.hourEnabled = hourEnabled
        if (!enabled) return
        binding.clock.setDigit(small = true, third = hourEnabled)
        binding.clock.updateTime(milestone)
    }

    private fun setTimerTime(time: Long) {
        if (!enabled) return
        if (started) stop()
        timerTime = time
        milestone = time
        binding.clock.updateTime(time)
    }

    private fun onModeChanged(mode: Mode) {
        val enable = mode == Mode.TIMER
        if (enable == enabled) return
        enabled = enable
        binding.clock.removeCallbacks(task)
        started = false
        if (!enable) return
        milestone = timerTime
        binding.button1.isInvisible = false
        binding.button2.setImageResource(R.drawable.ic_start)
        binding.button2.isInvisible = false
        binding.button2.setImageResource(R.drawable.ic_timer)
        binding.clock.setDigit(small = true, third = hourEnabled)
        binding.clock.updateTime(timerTime)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    companion object {
        private const val TIMER_INTERVAL = 10
        private const val PREFIX = "TimerDelegate:"
        private const val REQUEST_KEY = PREFIX + "Time"
    }
}

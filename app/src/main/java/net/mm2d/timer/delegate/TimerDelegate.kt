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
import net.mm2d.timer.R
import net.mm2d.timer.constant.Command
import net.mm2d.timer.constant.Command.SET
import net.mm2d.timer.constant.Command.SET_AND_START
import net.mm2d.timer.constant.Command.START
import net.mm2d.timer.constant.Command.STOP
import net.mm2d.timer.constant.Constants
import net.mm2d.timer.databinding.ActivityMainBinding
import net.mm2d.timer.dialog.TimeDialog
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.TimerRunningState
import net.mm2d.timer.util.doOnResume
import net.mm2d.timer.util.getLongExtraSafely
import net.mm2d.timer.util.observeOnce

class TimerDelegate(
    private val activity: FragmentActivity,
    private val binding: ActivityMainBinding,
) : ModeDelegate {
    private val delegateViewModel: TimerViewModel by activity.viewModels()
    private var isActive: Boolean = false
    private var started: Boolean = false
    private var start: Long = 0L
    private var milestone: Long = 0L
    private var hourEnabled: Boolean = false
    private var timerTime: Long = 0L
    private var restoreLatch: Boolean = false
    private var pendingIntent: Intent? = null
    override val mode: Mode = Mode.TIMER

    private val task = object : Runnable {
        override fun run() {
            val time = milestone - (System.currentTimeMillis() - start)
            if (time <= 0) {
                stop()
                delegateViewModel.playStopSound()
                return
            }
            binding.clock.updateTime(time)
            val delay = time % TIMER_INTERVAL
            if (isActive && started) binding.clock.postDelayed(this, delay)
        }
    }

    init {
        delegateViewModel.uiStateLiveData.observe(activity) {
            onModeChanged(it.mode)
            if (!isActive) return@observe
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
        if (restoreLatch) {
            handlePendingIntent()
            return
        }
        restoreLatch = true
        delegateViewModel.runningStateLiveData.observeOnce(activity) {
            restore(it)
            handlePendingIntent()
        }
    }

    private fun restore(state: TimerRunningState) {
        if (!state.started) return
        delegateViewModel.updateState(TimerRunningState(started = false))
        if (!isActive) return
        prepareStart()
        start = state.start
        milestone = state.milestone
        task.run()
    }

    override fun handleIntent(intent: Intent) {
        if (isActive) {
            handleIntentInner(intent)
        } else {
            pendingIntent = intent
        }
    }

    private fun handlePendingIntent() {
        activity.doOnResume {
            pendingIntent?.let { handleIntentInner(it) }
            pendingIntent = null
        }
    }

    private fun handleIntentInner(intent: Intent) {
        val command = Command.fromIntentExtra(intent) ?: return
        when (command) {
            START -> {
                if (started) return
                start()
            }
            STOP -> {
                if (!started) return
                stop()
            }
            SET -> {
                val time = intent.getLongExtraSafely(Constants.EXTRA_TIME) ?: return
                if (started) {
                    stop()
                }
                milestone = time
                binding.clock.updateTime(milestone)
            }
            SET_AND_START -> {
                val time = intent.getLongExtraSafely(Constants.EXTRA_TIME) ?: return
                if (!started) {
                    start()
                }
                milestone = time
                start = System.currentTimeMillis()
                binding.clock.updateTime(milestone)
            }
        }
    }

    override fun onClickButton1() {
        if (!isActive) return
        if (started) {
            stop()
        } else {
            start()
        }
        delegateViewModel.playSound()
    }

    override fun onClickButton2() {
        if (!isActive) return
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
        isActive = false
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
        if (!isActive) return
        binding.clock.setDigit(small = true, third = hourEnabled)
        binding.clock.updateTime(milestone)
    }

    private fun setTimerTime(time: Long) {
        if (!isActive) return
        if (started) stop()
        timerTime = time
        milestone = time
        binding.clock.updateTime(time)
    }

    private fun onModeChanged(mode: Mode) {
        val active = mode == this.mode
        if (active == isActive) return
        isActive = active
        binding.clock.removeCallbacks(task)
        started = false
        if (!active) return
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

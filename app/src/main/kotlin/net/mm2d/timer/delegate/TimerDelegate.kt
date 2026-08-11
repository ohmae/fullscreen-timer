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
import net.mm2d.timer.databinding.ActivityMainBinding
import net.mm2d.timer.dialog.TimeDialog
import net.mm2d.timer.main.MainCommand
import net.mm2d.timer.main.MainCommandParser
import net.mm2d.timer.main.TimeUpdate
import net.mm2d.timer.main.TimerController
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.TimerRunningState
import net.mm2d.timer.util.doOnResume
import net.mm2d.timer.util.observe
import net.mm2d.timer.util.observeOnce

class TimerDelegate(
    private val activity: FragmentActivity,
    private val binding: ActivityMainBinding,
) : ModeDelegate {
    private val delegateViewModel: TimerViewModel by activity.viewModels()
    private val controller = TimerController()
    private var isActive: Boolean = false
    private var hourEnabled: Boolean = false
    private var millisecondEnabled: Boolean = true
    private var timerTime: Long = 0L
    private var restoreLatch: Boolean = false
    private var pendingIntent: Intent? = null
    override val mode: Mode = Mode.TIMER

    private val task = object : Runnable {
        override fun run() {
            when (val update = controller.tick()) {
                is TimeUpdate.Running -> {
                    binding.clock.updateTime(update.timeMillis)
                    if (isActive && controller.started) {
                        binding.clock.postDelayed(this, update.nextDelayMillis)
                    }
                }

                is TimeUpdate.Finished -> {
                    showStoppedState(update.timeMillis)
                    delegateViewModel.playStopSound()
                }
            }
        }
    }

    init {
        delegateViewModel.uiStateFlow.observe(activity) {
            onModeChanged(it.mode)
            if (!isActive) return@observe
            setHourEnabled(it.hourEnabled, it.millisecondEnabled)
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
        delegateViewModel.runningStateFlow.observeOnce(activity) {
            restore(it)
            handlePendingIntent()
        }
    }

    private fun restore(
        state: TimerRunningState,
    ) {
        if (!state.started) return
        delegateViewModel.updateState(TimerRunningState(started = false))
        if (!isActive) return
        if (!controller.restore(state)) return
        showRunningState()
        task.run()
    }

    override fun handleIntent(
        intent: Intent,
    ) {
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

    private fun handleIntentInner(
        intent: Intent,
    ) {
        when (val command = MainCommandParser.parse(intent) ?: return) {
            MainCommand.Start -> {
                if (controller.started) return
                start()
            }

            MainCommand.Stop -> {
                if (!controller.started) return
                stop()
            }

            is MainCommand.Set -> {
                if (controller.started) stop()
                controller.setTime(command.timeMillis)
                binding.clock.updateTime(controller.timeMillis)
            }

            is MainCommand.SetAndStart -> {
                controller.setAndStart(command.timeMillis)
                showRunningState()
                binding.clock.removeCallbacks(task)
                task.run()
            }
        }
    }

    override fun onClickButton1() {
        if (!isActive) return
        if (controller.started) {
            stop()
            delegateViewModel.playSound()
        } else if (controller.timeMillis != 0L) {
            start()
            delegateViewModel.playSound()
        } else {
            TimeDialog.show(activity, REQUEST_KEY, timerTime, hourEnabled)
        }
    }

    override fun onClickButton2() {
        if (!isActive) return
        if (controller.started) {
            stop()
            delegateViewModel.playSound()
        }
        TimeDialog.show(activity, REQUEST_KEY, timerTime, hourEnabled)
    }

    override fun onClickTime() {
        if (controller.timeMillis == 0L) {
            onClickButton2()
        } else {
            onClickButton1()
        }
    }

    override fun onStop() {
        if (!controller.started) return
        delegateViewModel.updateState(controller.createRunningState())
    }

    override fun onDestroy() {
        isActive = false
        binding.clock.removeCallbacks(task)
    }

    private fun showRunningState() {
        binding.button1.setImageResource(R.drawable.ic_pause)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun start() {
        controller.start()
        showRunningState()
        task.run()
    }

    private fun stop() {
        showStoppedState(controller.stop())
    }

    private fun showStoppedState(
        timeMillis: Long,
    ) {
        binding.button1.setImageResource(R.drawable.ic_start)
        binding.clock.updateTime(timeMillis)
        binding.clock.removeCallbacks(task)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setHourEnabled(
        hourEnabled: Boolean,
        millisecondEnabled: Boolean,
    ) {
        this.hourEnabled = hourEnabled
        this.millisecondEnabled = millisecondEnabled
        if (!isActive) return
        binding.clock.setDigit(third = hourEnabled, small = millisecondEnabled)
        binding.clock.updateTime(controller.timeMillis)
    }

    private fun setTimerTime(
        time: Long,
    ) {
        if (!isActive) return
        if (controller.started) stop()
        timerTime = time
        controller.setTime(time)
        binding.clock.updateTime(time)
    }

    private fun onModeChanged(
        mode: Mode,
    ) {
        val active = mode == this.mode
        if (active == isActive) return
        isActive = active
        binding.clock.removeCallbacks(task)
        controller.deactivate()
        if (!active) return
        controller.setTime(timerTime)
        binding.button1.isInvisible = false
        binding.button2.setImageResource(R.drawable.ic_start)
        binding.button2.isInvisible = false
        binding.button2.setImageResource(R.drawable.ic_timer)
        binding.clock.setDigit(third = hourEnabled, small = millisecondEnabled)
        binding.clock.updateTime(timerTime)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    companion object {
        private const val PREFIX = "TimerDelegate:"
        private const val REQUEST_KEY = PREFIX + "Time"
    }
}

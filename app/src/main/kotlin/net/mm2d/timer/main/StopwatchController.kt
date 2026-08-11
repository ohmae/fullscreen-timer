/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.main

import net.mm2d.timer.settings.StopwatchRunningState
import javax.inject.Inject

class StopwatchController @Inject constructor(
    private val timeProvider: TimeProvider,
) {
    var started: Boolean = false
        private set

    var timeMillis: Long = 0L
        private set

    private var startTimeMillis: Long = 0L
    private var maximumTimeMillis: Long = MAX_WITHOUT_HOUR_MILLIS

    fun start() {
        started = true
        startTimeMillis = timeProvider.currentTimeMillis()
    }

    fun stop(): Long {
        if (started) {
            timeMillis = calculateTime().coerceAtMost(maximumTimeMillis)
        }
        started = false
        return timeMillis
    }

    fun reset(): Long {
        stop()
        timeMillis = 0L
        return timeMillis
    }

    fun setTime(
        timeMillis: Long,
    ) {
        this.timeMillis = timeMillis
    }

    fun setAndStart(
        timeMillis: Long,
    ) {
        setTime(timeMillis)
        start()
    }

    fun setHourEnabled(
        hourEnabled: Boolean,
    ) {
        maximumTimeMillis = if (hourEnabled) {
            MAX_WITH_HOUR_MILLIS
        } else {
            MAX_WITHOUT_HOUR_MILLIS
        }
    }

    fun tick(): TimeUpdate {
        val currentTimeMillis = calculateTime()
        if (currentTimeMillis >= maximumTimeMillis) {
            timeMillis = maximumTimeMillis
            started = false
            return TimeUpdate.Finished(timeMillis = maximumTimeMillis)
        }
        return TimeUpdate.Running(
            timeMillis = currentTimeMillis,
            nextDelayMillis = TIMER_INTERVAL_MILLIS - currentTimeMillis % TIMER_INTERVAL_MILLIS,
        )
    }

    fun restore(
        state: StopwatchRunningState,
    ): Boolean {
        if (!state.started) return false
        val restoredTimeMillis =
            state.milestone + timeProvider.currentTimeMillis() - state.start
        if (restoredTimeMillis > maximumTimeMillis) return false
        started = true
        startTimeMillis = state.start
        timeMillis = state.milestone
        return true
    }

    fun createRunningState(): StopwatchRunningState =
        StopwatchRunningState(
            started = started,
            start = startTimeMillis,
            milestone = timeMillis,
        )

    fun deactivate() {
        started = false
    }

    private fun calculateTime(): Long = timeMillis + timeProvider.currentTimeMillis() - startTimeMillis

    private companion object {
        const val TIMER_INTERVAL_MILLIS = 10L
        const val MAX_WITH_HOUR_MILLIS = 10 * 3_600_000L - TIMER_INTERVAL_MILLIS
        const val MAX_WITHOUT_HOUR_MILLIS = 100 * 60_000L - TIMER_INTERVAL_MILLIS
    }
}

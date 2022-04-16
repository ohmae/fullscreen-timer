/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.settings

data class StopwatchRunningState(
    val started: Boolean,
    val start: Long = 0L,
    val milestone: Long = 0L
)

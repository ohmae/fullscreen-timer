/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.settings

enum class Mode {
    CLOCK,
    STOPWATCH,
    TIMER,
    ;

    companion object {
        fun of(value: String?): Mode = values().find { it.name == value } ?: STOPWATCH
    }
}

/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.main

sealed interface MainCommand {
    data object Start : MainCommand

    data object Stop : MainCommand

    data class Set(
        val timeMillis: Long,
    ) : MainCommand

    data class SetAndStart(
        val timeMillis: Long,
    ) : MainCommand
}

/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.main

sealed interface TimeUpdate {
    val timeMillis: Long

    data class Running(
        override val timeMillis: Long,
        val nextDelayMillis: Long,
    ) : TimeUpdate

    data class Finished(
        override val timeMillis: Long,
    ) : TimeUpdate
}

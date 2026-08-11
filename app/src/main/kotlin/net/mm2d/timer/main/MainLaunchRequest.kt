/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.main

import android.content.Intent
import net.mm2d.timer.settings.Mode

data class MainLaunchRequest(
    val mode: Mode,
    val command: MainCommand?,
)

object MainLaunchRequestParser {
    fun parse(
        intent: Intent,
    ): MainLaunchRequest? =
        Mode.fromIntentExtra(intent)?.let {
            MainLaunchRequest(
                mode = it,
                command = MainCommandParser.parse(intent),
            )
        }
}

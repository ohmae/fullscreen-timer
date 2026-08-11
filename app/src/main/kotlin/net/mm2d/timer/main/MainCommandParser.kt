/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.main

import android.content.Intent
import net.mm2d.timer.constant.Command
import net.mm2d.timer.constant.Constants
import net.mm2d.timer.util.getLongExtraSafely

object MainCommandParser {
    fun parse(
        intent: Intent,
    ): MainCommand? =
        when (Command.fromIntentExtra(intent)) {
            Command.START -> MainCommand.Start
            Command.STOP -> MainCommand.Stop
            Command.SET -> intent.getTimeMillis()?.let(MainCommand::Set)
            Command.SET_AND_START -> intent.getTimeMillis()?.let(MainCommand::SetAndStart)
            null -> null
        }

    private fun Intent.getTimeMillis(): Long? = getLongExtraSafely(Constants.EXTRA_TIME)
}

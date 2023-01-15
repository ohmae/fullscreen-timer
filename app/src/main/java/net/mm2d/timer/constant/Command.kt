/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.constant

import android.content.Intent
import net.mm2d.timer.util.getStringExtraSafely

enum class Command {
    START,
    STOP,
    SET,
    SET_AND_START,
    ;

    companion object {
        private fun ofNullable(name: String?): Command? = values().find { it.name == name }

        fun fromIntentExtra(intent: Intent): Command? =
            intent.getStringExtraSafely(Constants.EXTRA_COMMAND)?.let { ofNullable(it) }
    }
}

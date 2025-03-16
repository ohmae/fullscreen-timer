/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.settings

import android.content.Intent
import net.mm2d.timer.constant.Constants
import net.mm2d.timer.util.getStringExtraSafely

enum class Mode {
    CLOCK,
    STOPWATCH,
    TIMER,
    ;

    companion object {
        private fun ofNullable(
            value: String?,
        ): Mode? = entries.find { it.name == value }

        fun of(
            value: String?,
        ): Mode = ofNullable(value) ?: STOPWATCH

        fun fromIntentExtra(
            intent: Intent,
        ): Mode? = intent.getStringExtraSafely(Constants.EXTRA_MODE)?.let { ofNullable(it) }
    }
}

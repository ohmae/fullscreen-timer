/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.delegate

import android.content.Intent
import net.mm2d.timer.settings.Mode

interface ModeDelegate {
    val mode: Mode
    fun handleIntent(intent: Intent)
    fun onClickButton1()
    fun onClickButton2()
    fun onClickTime()
    fun onStop()
    fun onDestroy()
}

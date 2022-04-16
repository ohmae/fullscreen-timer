/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.delegate

interface ModeDelegate {
    fun onClickButton1()
    fun onClickButton2()
    fun onClickTime()
    fun onStop()
    fun onDestroy()
}

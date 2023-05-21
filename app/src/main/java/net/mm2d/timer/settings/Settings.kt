/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.settings

data class Settings(
    val versionAtInstall: Int,
    val versionAtLastLaunched: Int,
    val versionBeforeUpdate: Int,
    val mode: Mode,
    val foregroundColor: Int,
    val backgroundColor: Int,
    val hourEnabled: Boolean,
    val timerTime: Long,
    val soundVolume: Int,
    val fullscreen: Boolean,
    val orientation: Orientation,
)

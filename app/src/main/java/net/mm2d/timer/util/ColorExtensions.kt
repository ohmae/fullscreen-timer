/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.util

fun Int.alpha(): Int =
    this.ushr(24) and 0xFF

fun Int.opaque(): Int =
    this or 0xFF.shl(24)

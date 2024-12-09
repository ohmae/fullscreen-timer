/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.util

import android.graphics.Color
import androidx.core.graphics.ColorUtils

fun Int.shouldUseDarkForeground(): Boolean = ColorUtils.calculateContrast(this, Color.WHITE) >= 3

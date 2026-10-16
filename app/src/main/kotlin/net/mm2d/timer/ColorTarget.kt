/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

@Serializable
enum class ColorTarget(
    @StringRes val titleRes: Int,
) {
    FOREGROUND(R.string.menu_title_foreground_color),
    BACKGROUND(R.string.menu_title_background_color),
}

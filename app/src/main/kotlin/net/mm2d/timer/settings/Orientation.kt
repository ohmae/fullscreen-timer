/*
 * Copyright (c) 2024 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.settings

import android.content.pm.ActivityInfo
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import net.mm2d.timer.R

enum class Orientation(
    val value: Int,
    @DrawableRes val icon: Int,
    @StringRes val description: Int,
) {
    UNSPECIFIED(
        value = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
        icon = R.drawable.ic_unspecified,
        description = R.string.menu_description_unspecified,
    ),
    PORTRAIT(
        value = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
        icon = R.drawable.ic_portrait,
        description = R.string.menu_description_portrait,
    ),
    LANDSCAPE(
        value = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
        icon = R.drawable.ic_landscape,
        description = R.string.menu_description_landscape,
    ),
    REVERSE_PORTRAIT(
        value = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
        icon = R.drawable.ic_reverse_portrait,
        description = R.string.menu_description_reverse_portrait,
    ),
    REVERSE_LANDSCAPE(
        value = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
        icon = R.drawable.ic_reverse_landscape,
        description = R.string.menu_description_reverse_landscape,
    ),
    ;

    companion object {
        fun of(
            value: Int?,
        ): Orientation =
            entries.firstOrNull { it.value == value }
                ?: UNSPECIFIED
    }
}

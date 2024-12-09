/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.util

import android.content.Intent

fun Intent.getStringExtraSafely(
    name: String,
): String? = runCatching { getStringExtra(name) }
    .getOrNull()

fun Intent.getLongExtraSafely(
    name: String,
): Long? = runCatching { if (hasExtra(name)) getLongExtra(name, 0L) else null }
    .getOrNull()

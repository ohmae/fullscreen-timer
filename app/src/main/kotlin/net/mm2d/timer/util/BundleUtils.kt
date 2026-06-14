/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.util

import android.os.Bundle
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun buildBundle(
    action: Bundle.() -> Unit,
): Bundle {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    return Bundle().apply(action)
}

fun longBundle(
    key: String,
    value: Long,
): Bundle = buildBundle { putLong(key, value) }

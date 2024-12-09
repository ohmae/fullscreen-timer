/*
 * Copyright (c) 2023 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleOwner

fun LifecycleOwner.doOnResume(
    block: () -> Unit,
) {
    if (lifecycle.currentState == State.RESUMED) {
        block()
        return
    }
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(
            owner: LifecycleOwner,
        ) {
            block()
            lifecycle.removeObserver(this)
        }
    })
}

/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class CustomTabsBinder : DefaultLifecycleObserver {
    override fun onStart(
        owner: LifecycleOwner,
    ) {
        CustomTabsHelper.bind()
    }

    override fun onStop(
        owner: LifecycleOwner,
    ) {
        CustomTabsHelper.unbind()
    }
}

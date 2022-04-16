/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.mm2d.timer.util.CustomTabsHelper

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CustomTabsHelper.initialize(this)
    }
}

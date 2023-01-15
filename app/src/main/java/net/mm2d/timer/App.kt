/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import net.mm2d.timer.util.CustomTabsHelper

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        CustomTabsHelper.initialize(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.e("XXXX", "onActivityCreated: ${activity.componentName.className}")
            }

            override fun onActivityStarted(activity: Activity) {
                Log.e("XXXX", "onActivityStarted: ${activity.componentName.className}")
            }

            override fun onActivityResumed(activity: Activity) {
                Log.e("XXXX", "onActivityResumed: ${activity.componentName.className}")
            }

            override fun onActivityPaused(activity: Activity) {
                Log.e("XXXX", "onActivityPaused: ${activity.componentName.className}")
            }

            override fun onActivityStopped(activity: Activity) {
                Log.e("XXXX", "onActivityStopped: ${activity.componentName.className}")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                Log.e("XXXX", "onActivitySaveInstanceState: ${activity.componentName.className}")
            }

            override fun onActivityDestroyed(activity: Activity) {
                Log.e("XXXX", "onActivityDestroyed: ${activity.componentName.className}")
            }
        })
    }
}

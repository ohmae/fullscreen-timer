/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object OpenUriUtils {
    private var defaultBrowserPackage: String? = null
    private var browserPackages: Set<String>? = null

    fun getBrowserPackages(context: Context): Set<String> {
        browserPackages?.let {
            return it
        }
        return getBrowserPackagesInner(context).also {
            browserPackages = it
        }
    }

    private fun getBrowserPackagesInner(context: Context): Set<String> =
        context.packageManager
            .queryIntentActivities(makeBrowserTestIntent(), PackageManager.MATCH_ALL)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()

    fun getDefaultBrowserPackage(context: Context): String? {
        defaultBrowserPackage?.let {
            return it
        }
        return getDefaultBrowserPackageInner(context)?.also {
            defaultBrowserPackage = it
        }
    }

    private fun getDefaultBrowserPackageInner(context: Context): String? {
        val packageName = context.packageManager
            .resolveActivity(makeBrowserTestIntent(), 0)
            ?.activityInfo
            ?.packageName
            ?: return null
        return if (getBrowserPackages(context).contains(packageName)) {
            packageName
        } else null
    }

    private fun makeBrowseIntent(uri: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(uri)).also {
            it.addCategory(Intent.CATEGORY_BROWSABLE)
        }
    }

    private fun makeBrowserTestIntent(): Intent {
        return makeBrowseIntent("http://www.example.com/")
    }
}

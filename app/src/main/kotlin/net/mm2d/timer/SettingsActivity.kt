/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.timer.dialog.FontDialog
import net.mm2d.timer.dialog.OrientationDialog
import net.mm2d.timer.util.Launcher

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        registerDialogListeners()
        setContent {
            SettingsScreen(
                viewModel = viewModel,
                onNavigate = ::onNavigate,
                onOpenFontDialog = {
                    FontDialog.show(this, REQUEST_KEY_FONT)
                },
                onOpenOrientationDialog = {
                    OrientationDialog.show(this, REQUEST_KEY_ORIENTATION)
                },
            )
        }
    }

    private fun registerDialogListeners() {
        FontDialog.registerListener(this, REQUEST_KEY_FONT) { font ->
            viewModel.updateFont(font)
        }
        OrientationDialog.registerListener(this, REQUEST_KEY_ORIENTATION) {
            viewModel.updateOrientation(it)
        }
    }

    private fun onNavigate(
        direction: NavigationDirection,
    ) {
        when (direction) {
            NavigationDirection.UP ->
                finish()

            NavigationDirection.TO_LICENSE ->
                LicenseActivity.start(this)

            NavigationDirection.TO_SOURCE_CODE ->
                Launcher.openSourceCode(this)

            NavigationDirection.TO_PRIVACY_POLICY ->
                Launcher.openPrivacyPolicy(this)

            NavigationDirection.TO_PLAY_STORE ->
                Launcher.openGooglePlay(this)
        }
    }

    companion object {
        private const val PREFIX = "SettingsActivity."
        private const val REQUEST_KEY_FONT = PREFIX + "REQUEST_KEY_FONT"
        private const val REQUEST_KEY_ORIENTATION = PREFIX + "REQUEST_KEY_ORIENTATION"

        fun start(
            context: Context,
        ) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }
}

enum class NavigationDirection {
    UP,
    TO_LICENSE,
    TO_SOURCE_CODE,
    TO_PRIVACY_POLICY,
    TO_PLAY_STORE,
}

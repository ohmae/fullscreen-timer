/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.timer.ui.theme.AppTheme

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge(
            SystemBarStyle.dark(Color.TRANSPARENT),
            SystemBarStyle.dark(Color.BLACK),
        )
        setContent {
            AppTheme {
                SettingsScreen(
                    goBack = { finish() },
                )
            }
        }
    }

    companion object {
        fun start(
            context: Context,
        ) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }
}

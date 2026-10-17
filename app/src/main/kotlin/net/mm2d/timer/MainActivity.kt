/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.timer.MainViewModel.UiEvent
import net.mm2d.timer.main.MainLaunchRequestParser
import net.mm2d.timer.ui.navigation.NavigationRoot
import net.mm2d.timer.ui.theme.AppTheme
import net.mm2d.timer.util.Updater

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK),
        )
        setContent {
            AppTheme {
                NavigationRoot()
            }
        }

        Updater.startUpdateIfAvailable(this)
        if (savedInstanceState == null) handleIntent(intent)
    }

    override fun onNewIntent(
        intent: Intent,
    ) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(
        intent: Intent,
    ) {
        MainLaunchRequestParser.parse(intent)?.let {
            viewModel.onEvent(UiEvent.HandleLaunchRequest(it))
        }
    }
}

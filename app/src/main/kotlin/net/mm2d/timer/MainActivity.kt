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
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import net.mm2d.timer.MainViewModel.UiEvent
import net.mm2d.timer.main.MainLaunchRequestParser
import net.mm2d.timer.settings.Orientation
import net.mm2d.timer.ui.navigation.NavigationRoot
import net.mm2d.timer.ui.theme.AppTheme
import net.mm2d.timer.util.Updater
import net.mm2d.timer.util.observe

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var renderedWindowState: WindowState? = null

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
        viewModel.getUiStateStream()
            .filter { it.initialized }
            .map { WindowState(it.orientation, it.keepScreenOn) }
            .distinctUntilChanged()
            .observe(this, action = ::renderWindow)

        Updater.startUpdateIfAvailable(this)
        if (savedInstanceState == null) handleIntent(intent)
    }

    private fun renderWindow(
        state: WindowState,
    ) {
        val previousState = renderedWindowState
        if (previousState?.orientation != state.orientation) requestedOrientation = state.orientation.value
        if (previousState?.keepScreenOn != state.keepScreenOn) {
            if (state.keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        renderedWindowState = state
    }

    override fun onStop() {
        viewModel.onEvent(UiEvent.PersistRunningState)
        super.onStop()
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

private data class WindowState(
    val orientation: Orientation,
    val keepScreenOn: Boolean,
)

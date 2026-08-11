/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.mm2d.timer.MainViewModel.UiEffect
import net.mm2d.timer.MainViewModel.UiEvent
import net.mm2d.timer.MainViewModel.UiState
import net.mm2d.timer.dialog.TimeDialog
import net.mm2d.timer.main.MainLaunchRequestParser
import net.mm2d.timer.sound.SoundEffect
import net.mm2d.timer.ui.theme.AppTheme
import net.mm2d.timer.util.FullscreenHelper
import net.mm2d.timer.util.Updater
import net.mm2d.timer.util.observe
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var soundEffect: SoundEffect

    private lateinit var fullscreenHelper: FullscreenHelper
    private val viewModel: MainViewModel by viewModels()
    private var renderedState: UiState? = null

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        fullscreenHelper = FullscreenHelper(window)
        setContent {
            AppTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Ltr,
                ) {
                    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
                    MainScreenRoute(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                    )
                }
            }
        }
        viewModel.uiStateFlow.observe(this, action = ::renderWindow)
        viewModel.uiEffectFlow
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach(::handleEffect)
            .launchIn(lifecycleScope)

        Updater.startUpdateIfAvailable(this)
        if (savedInstanceState == null) handleIntent(intent)
    }

    private fun renderWindow(
        state: UiState,
    ) {
        if (!state.initialized) return
        val previousState = renderedState
        if (previousState?.fullscreen != state.fullscreen) fullscreenHelper.invoke(state.fullscreen)
        if (previousState?.orientation != state.orientation) requestedOrientation = state.orientation.value
        if (previousState?.keepScreenOn != state.keepScreenOn) {
            if (state.keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        renderedState = state
    }

    private fun handleEffect(
        effect: UiEffect,
    ) {
        when (effect) {
            UiEffect.OpenSettings -> SettingsActivity.start(this)
            UiEffect.PlaySound -> soundEffect.play()
            UiEffect.PlayStopSound -> soundEffect.playStop()
        }
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

@Composable
private fun MainScreenRoute(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
) {
    val animatedButtonOpacity = remember { Animatable(1f) }
    var animationRequest by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        animationRequest++
    }
    LaunchedEffect(uiState.buttonOpacity, animationRequest) {
        animatedButtonOpacity.snapTo(1f)
        animatedButtonOpacity.animateTo(
            targetValue = uiState.buttonOpacity.coerceIn(0f, 1f),
            animationSpec = tween(BUTTON_ANIMATION_DURATION_MILLIS),
        )
    }
    MainScreen(
        uiState = uiState.copy(buttonOpacity = animatedButtonOpacity.value),
        onEvent = { event ->
            onEvent(event)
            if (event.animatesButtonOpacity) animationRequest++
        },
    )
    uiState.timerDialog?.let {
        TimeDialog(
            timeMillis = it.timeMillis,
            hourEnabled = it.hourEnabled,
            onSelectTime = { timeMillis ->
                onEvent(UiEvent.SelectTimerTime(timeMillis))
            },
            onDismissRequest = {
                onEvent(UiEvent.DismissTimerDialog)
            },
        )
    }
}

private val UiEvent.animatesButtonOpacity: Boolean
    get() = this == UiEvent.ClickFirstButton ||
        this == UiEvent.ClickSecondButton ||
        this == UiEvent.ClickTime

private const val BUTTON_ANIMATION_DURATION_MILLIS = 1_500

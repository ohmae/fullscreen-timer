/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isInvisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.mm2d.timer.MainViewModel.Button
import net.mm2d.timer.MainViewModel.UiEffect
import net.mm2d.timer.MainViewModel.UiEvent
import net.mm2d.timer.MainViewModel.UiState
import net.mm2d.timer.databinding.ActivityMainBinding
import net.mm2d.timer.dialog.TimeDialog
import net.mm2d.timer.main.MainLaunchRequestParser
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.sound.SoundEffect
import net.mm2d.timer.util.FullscreenHelper
import net.mm2d.timer.util.Updater
import net.mm2d.timer.util.observe
import net.mm2d.timer.util.resolveColor
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var soundEffect: SoundEffect

    private lateinit var binding: ActivityMainBinding
    private lateinit var fullscreenHelper: FullscreenHelper
    private val viewModel: MainViewModel by viewModels()
    private var renderedState: UiState? = null
    private var buttonOpacity: Float = 1f

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button1.setOnClickListener {
            viewModel.onEvent(UiEvent.ClickFirstButton)
            animateButtonOpacity()
        }
        binding.button2.setOnClickListener {
            viewModel.onEvent(UiEvent.ClickSecondButton)
            animateButtonOpacity()
        }
        binding.settings.setOnClickListener {
            viewModel.onEvent(UiEvent.ClickSettings)
        }
        binding.tapArea.setOnClickListener {
            viewModel.onEvent(UiEvent.ClickTime)
            animateButtonOpacity()
        }
        TimeDialog.registerListener(this, TIMER_DIALOG_REQUEST_KEY) {
            viewModel.onEvent(UiEvent.SelectTimerTime(it))
        }

        fullscreenHelper = FullscreenHelper(window)
        viewModel.uiStateFlow.observe(this, action = ::render)
        viewModel.uiEffectFlow
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach(::handleEffect)
            .launchIn(lifecycleScope)

        Updater.startUpdateIfAvailable(this)
        if (savedInstanceState == null) handleIntent(intent)
    }

    private fun render(
        state: UiState,
    ) {
        if (!state.initialized) return
        val previousState = renderedState
        renderClock(state, previousState)
        renderButtons(state, previousState)
        renderAppearance(state, previousState)
        renderWindow(state, previousState)
        renderedState = state
    }

    private fun renderClock(
        state: UiState,
        previousState: UiState?,
    ) {
        if (
            previousState?.mode != state.mode ||
            previousState.hourEnabled != state.hourEnabled ||
            previousState.hourFormat24 != state.hourFormat24 ||
            previousState.millisecondEnabled != state.millisecondEnabled ||
            previousState.secondEnabled != state.secondEnabled
        ) {
            when (state.mode) {
                Mode.CLOCK -> binding.clock.setDigit(
                    hourFormat24 = state.hourFormat24,
                    small = state.secondEnabled,
                )

                Mode.STOPWATCH,
                Mode.TIMER,
                    -> binding.clock.setDigit(
                    third = state.hourEnabled,
                    small = state.millisecondEnabled,
                )
            }
        }
        when (state.mode) {
            Mode.CLOCK -> binding.clock.updateClock(state.timeMillis)

            Mode.STOPWATCH,
            Mode.TIMER,
                -> binding.clock.updateTime(state.timeMillis)
        }
    }

    private fun renderButtons(
        state: UiState,
        previousState: UiState?,
    ) {
        if (previousState?.firstButton != state.firstButton) {
            binding.button1.render(state.firstButton)
        }
        if (previousState?.secondButton != state.secondButton) {
            binding.button2.render(state.secondButton)
        }
    }

    private fun renderAppearance(
        state: UiState,
        previousState: UiState?,
    ) {
        if (previousState?.foregroundColor != state.foregroundColor) {
            binding.clock.setColor(state.foregroundColor)
        }
        if (previousState?.font != state.font) binding.clock.setFont(state.font)
        if (previousState?.backgroundColor != state.backgroundColor) {
            window.setBackgroundDrawable(state.backgroundColor.toDrawable())
        }
        if (previousState?.shouldUseDarkForeground != state.shouldUseDarkForeground) {
            val (backgroundResource, foregroundTint) = if (state.shouldUseDarkForeground) {
                R.drawable.bg_button_dark to ColorStateList.valueOf(resolveColor(R.attr.colorControlDark))
            } else {
                R.drawable.bg_button_light to ColorStateList.valueOf(resolveColor(R.attr.colorControlLight))
            }
            listOf(binding.button1, binding.button2, binding.settings).forEach {
                it.setBackgroundResource(backgroundResource)
                it.imageTintList = foregroundTint
            }
        }
        if (previousState?.buttonOpacity != state.buttonOpacity) {
            buttonOpacity = state.buttonOpacity
            animateButtonOpacity()
        }
    }

    private fun renderWindow(
        state: UiState,
        previousState: UiState?,
    ) {
        if (previousState?.fullscreen != state.fullscreen) fullscreenHelper.invoke(state.fullscreen)
        if (previousState?.orientation != state.orientation) requestedOrientation = state.orientation.value
        if (previousState?.keepScreenOn != state.keepScreenOn) {
            if (state.keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    private fun ImageView.render(
        button: Button,
    ) {
        isInvisible = button == Button.HIDDEN
        val imageResource = when (button) {
            Button.HIDDEN -> return
            Button.START -> R.drawable.ic_start
            Button.PAUSE -> R.drawable.ic_pause
            Button.RESET -> R.drawable.ic_reset
            Button.TIMER -> R.drawable.ic_timer
        }
        setImageResource(imageResource)
    }

    private fun handleEffect(
        effect: UiEffect,
    ) {
        when (effect) {
            UiEffect.OpenSettings -> SettingsActivity.start(this)

            is UiEffect.ShowTimerDialog -> TimeDialog.show(
                activity = this,
                requestKey = TIMER_DIALOG_REQUEST_KEY,
                time = effect.timeMillis,
                hourEnabled = effect.hourEnabled,
            )

            UiEffect.PlaySound -> soundEffect.play()

            UiEffect.PlayStopSound -> soundEffect.playStop()
        }
    }

    override fun onStart() {
        super.onStart()
        animateButtonOpacity()
    }

    private fun animateButtonOpacity() {
        val opacity = buttonOpacity
        if (binding.button1.alpha == opacity) return
        listOf(binding.button1, binding.button2, binding.settings).forEach {
            it.alpha = 1f
            it.animate()
                .alpha(opacity)
                .setDuration(BUTTON_ANIMATION_DURATION_MILLIS)
                .start()
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

    private companion object {
        const val BUTTON_ANIMATION_DURATION_MILLIS = 1_500L
        const val TIMER_DIALOG_REQUEST_KEY = "MainActivity:Time"
    }
}

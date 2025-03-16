/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.timer.MainViewModel.UiState
import net.mm2d.timer.databinding.ActivityMainBinding
import net.mm2d.timer.delegate.ClockDelegate
import net.mm2d.timer.delegate.ModeDelegate
import net.mm2d.timer.delegate.StopwatchDelegate
import net.mm2d.timer.delegate.TimerDelegate
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.util.FullscreenHelper
import net.mm2d.timer.util.Updater
import net.mm2d.timer.util.observe
import net.mm2d.timer.util.resolveColor

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var delegates: List<ModeDelegate>
    private lateinit var fullscreenHelper: FullscreenHelper
    private val viewModel: MainViewModel by viewModels()
    private var buttonOpacity: Float = 1f

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        delegates = listOf(
            ClockDelegate(this, binding),
            StopwatchDelegate(this, binding),
            TimerDelegate(this, binding),
        )
        binding.button1.setOnClickListener {
            delegates.forEach { it.onClickButton1() }
            animateButtonOpacity()
        }
        binding.button2.setOnClickListener {
            delegates.forEach { it.onClickButton2() }
            animateButtonOpacity()
        }
        binding.settings.setOnClickListener {
            SettingsActivity.start(this)
        }
        binding.tapArea.setOnClickListener {
            delegates.forEach { it.onClickTime() }
            animateButtonOpacity()
        }
        fullscreenHelper = FullscreenHelper(window)
        viewModel.uiStateFlow.observe(this) {
            updateUiState(it)
        }
        viewModel.orientationFlow.observe(this, Lifecycle.State.CREATED) {
            requestedOrientation = it.value
        }
        Updater.startUpdateIfAvailable(this)
        if (savedInstanceState == null) {
            handleIntent(intent)
        }
    }

    private fun updateUiState(
        uiState: UiState,
    ) {
        binding.clock.setColor(uiState.foregroundColor)
        window.setBackgroundDrawable(ColorDrawable(uiState.backgroundColor))
        val (backgroundResource, foregroundTint) = if (uiState.shouldUseDarkForeground) {
            R.drawable.bg_button_dark to ColorStateList.valueOf(resolveColor(R.attr.colorControlDark))
        } else {
            R.drawable.bg_button_light to ColorStateList.valueOf(resolveColor(R.attr.colorControlLight))
        }
        listOf(
            binding.button1,
            binding.button2,
            binding.settings,
        ).forEach {
            it.setBackgroundResource(backgroundResource)
            it.imageTintList = foregroundTint
        }
        buttonOpacity = uiState.buttonOpacity
        animateButtonOpacity()
        fullscreenHelper.invoke(uiState.fullscreen)
    }

    override fun onStart() {
        super.onStart()
        animateButtonOpacity()
    }

    private fun animateButtonOpacity() {
        val opacity = buttonOpacity
        if (binding.button1.alpha == opacity) return
        listOf(
            binding.button1,
            binding.button2,
            binding.settings,
        ).forEach {
            it.alpha = 1f
            it.animate()
                .alpha(opacity)
                .setDuration(1500L)
                .start()
        }
    }

    override fun onStop() {
        super.onStop()
        delegates.forEach { it.onStop() }
    }

    override fun onDestroy() {
        super.onDestroy()
        delegates.forEach { it.onDestroy() }
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
        val mode = Mode.fromIntentExtra(intent) ?: return
        delegates.find { it.mode == mode }?.handleIntent(intent)
        viewModel.updateMode(mode)
    }
}

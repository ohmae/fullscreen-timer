/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import net.mm2d.timer.databinding.ActivityMainBinding
import net.mm2d.timer.delegate.ClockDelegate
import net.mm2d.timer.delegate.ModeDelegate
import net.mm2d.timer.delegate.StopwatchDelegate
import net.mm2d.timer.delegate.TimerDelegate
import net.mm2d.timer.util.FullscreenHelper
import net.mm2d.timer.util.Updater

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var delegates: List<ModeDelegate>
    private lateinit var fullscreenHelper: FullscreenHelper
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
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
        }
        binding.button2.setOnClickListener {
            delegates.forEach { it.onClickButton2() }
        }
        binding.settings.setOnClickListener {
            SettingsActivity.start(this)
        }
        binding.tapArea.setOnClickListener {
            delegates.forEach { it.onClickTime() }
        }
        fullscreenHelper = FullscreenHelper(window)
        viewModel.uiStateLiveData.observe(this) { uiState ->
            uiState ?: return@observe
            binding.clock.setColor(uiState.foregroundColor)
            fullscreenHelper.start(uiState.fullscreen)
        }
        Updater.startUpdateIfAvailable(this)
    }

    override fun onStop() {
        super.onStop()
        delegates.forEach { it.onStop() }
    }

    override fun onDestroy() {
        super.onDestroy()
        delegates.forEach { it.onDestroy() }
        fullscreenHelper.stop()
    }
}

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
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.mm2d.color.chooser.ColorChooserDialog
import net.mm2d.timer.SettingsViewModel.UiState
import net.mm2d.timer.databinding.ActivitySettingsBinding
import net.mm2d.timer.dialog.OrientationDialog
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.util.Launcher

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var singleSelectMediator: SingleSelectMediator
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setUpView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.license -> LicenseActivity.start(this)
            R.id.source_code -> Launcher.openSourceCode(this)
            R.id.privacy_policy -> Launcher.openPrivacyPolicy(this)
            R.id.play_store -> Launcher.openGooglePlay(this)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setUpView() {
        ColorChooserDialog.registerListener(
            this,
            REQUEST_KEY_FOREGROUND,
            { viewModel.updateForegroundColor(it) },
            null
        )
        ColorChooserDialog.registerListener(
            this,
            REQUEST_KEY_BACKGROUND,
            { viewModel.updateBackgroundColor(it) },
            null
        )
        binding.foregroundColor.setOnClickListener {
            ColorChooserDialog.show(
                this,
                REQUEST_KEY_FOREGROUND,
                binding.foregroundColor.getColor(),
                false
            )
        }
        binding.backgroundColor.setOnClickListener {
            ColorChooserDialog.show(
                this,
                REQUEST_KEY_BACKGROUND,
                binding.backgroundColor.getColor(),
                false
            )
        }
        binding.hourEnabled.setOnClickListener {
            viewModel.updateHourEnabled(!binding.hourEnabled.isChecked)
        }
        binding.volumeBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                viewModel.updateVolume(progress)
            }
        })
        binding.fullscreen.setOnClickListener {
            viewModel.updateFullscreen(!binding.fullscreen.isChecked)
        }
        OrientationDialog.registerListener(this, REQUEST_KEY_ORIENTATION) {
            viewModel.updateOrientation(it)
        }
        binding.orientation.setOnClickListener {
            OrientationDialog.show(this, REQUEST_KEY_ORIENTATION)
        }
        binding.versionDescription.text = BuildConfig.VERSION_NAME
        setUpSingleSelectMediator()
        setUpObserver()
    }

    private fun setUpSingleSelectMediator() {
        singleSelectMediator = SingleSelectMediator { id ->
            when (id) {
                binding.modeClock.id -> Mode.CLOCK
                binding.modeStopwatch.id -> Mode.STOPWATCH
                binding.modeTimer.id -> Mode.TIMER
                else -> null
            }?.let {
                viewModel.updateMode(it)
            }
        }
        singleSelectMediator.add(binding.modeClock)
        singleSelectMediator.add(binding.modeStopwatch)
        singleSelectMediator.add(binding.modeTimer)
    }

    private fun setUpObserver() {
        lifecycleScope.launch {
            viewModel.uiStateFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { updateUiState(it) }
        }
    }

    private fun updateUiState(uiState: UiState) {
        when (uiState.mode) {
            Mode.CLOCK -> binding.modeClock
            Mode.STOPWATCH -> binding.modeStopwatch
            Mode.TIMER -> binding.modeTimer
        }.let {
            singleSelectMediator.onSelect(it)
        }
        binding.foregroundColor.setColor(uiState.foregroundColor)
        binding.backgroundColor.setColor(uiState.backgroundColor)
        binding.hourEnabled.isChecked = uiState.hourEnabled
        binding.volumeBar.progress = uiState.volume
        binding.volumeValue.text = uiState.volume.toString()
        binding.fullscreen.isChecked = uiState.fullscreen
        binding.orientationIcon.setImageResource(uiState.orientation.icon)
        binding.orientationDescription.setText(uiState.orientation.description)
    }

    private class SingleSelectMediator(
        private val onSelectedListener: (Int) -> Unit
    ) {
        private val views = SparseArray<View>()
        var selectedId: Int = 0
            private set

        fun add(view: View) {
            views.put(view.id, view)
            if (view.isSelected) {
                if (selectedId == 0) {
                    selectedId = view.id
                } else {
                    view.isSelected = false
                }
            }
            view.setOnClickListener {
                onSelect(view)
            }
        }

        fun onSelect(view: View) {
            if (view.id == selectedId) return
            views.get(selectedId)?.isSelected = false
            view.isSelected = true
            selectedId = view.id
            onSelectedListener(selectedId)
        }
    }

    companion object {
        private const val PREFIX = "SettingsActivity."
        private const val REQUEST_KEY_FOREGROUND = PREFIX + "REQUEST_KEY_FOREGROUND"
        private const val REQUEST_KEY_BACKGROUND = PREFIX + "REQUEST_KEY_BACKGROUND"
        private const val REQUEST_KEY_ORIENTATION = PREFIX + "REQUEST_KEY_ORIENTATION"

        fun start(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }
}

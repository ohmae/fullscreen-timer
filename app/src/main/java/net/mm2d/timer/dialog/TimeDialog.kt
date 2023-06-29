/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.timer.R
import net.mm2d.timer.databinding.DialogTimeBinding

class TimeDialog : DialogFragment() {
    private var hourEnabled: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogTimeBinding.inflate(requireActivity().layoutInflater)
        val arguments = requireArguments()
        val requestKey = arguments.getString(KEY_REQUEST) ?: ""
        val time = arguments.getLong(KEY_TIME)
        hourEnabled = arguments.getBoolean(KEY_HOUR_ENABLED)
        binding.setValue(time)
        binding.setOnClickListener()
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.ok) { _, _ ->
                parentFragmentManager.setFragmentResult(
                    requestKey, bundleOf(KEY_RESULT to binding.getValue())
                )
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    private fun DialogTimeBinding.setValue(time: Long) {
        val maxSecond = if (hourEnabled) 35999L else 5999L
        val second = (time / 1000).coerceIn(0, maxSecond).toInt()
        second1.maxValue = 9
        second1.minValue = 0
        second10.maxValue = 5
        second10.minValue = 0
        minute1.maxValue = 9
        minute1.minValue = 0
        minute10.maxValue = if (hourEnabled) 5 else 9
        minute10.minValue = 0
        hour1.maxValue = 9
        hour1.minValue = 0

        second1.value = second % 10
        second10.value = (second / 10) % 6
        minute1.value = (second / 60) % 10
        hour1.isVisible = hourEnabled
        hourLabel.isVisible = hourEnabled
        if (hourEnabled) {
            minute10.value = (second / 600) % 6
            hour1.value = (second / 3600) % 10
        } else {
            minute10.value = (second / 600) % 10
            hour1.value = 0
        }
    }

    private fun DialogTimeBinding.getValue(): Long =
        second1.value * 1000L +
            second10.value * 10_000L +
            minute1.value * 60_000L +
            minute10.value * 600_000L +
            hour1.value * 3600_000L

    private fun DialogTimeBinding.setOnClickListener() {
        reset.setOnClickListener { setValue(0L) }
        plus5s.setOnClickListener { setValue(getValue() + 5_000) }
        plus30s.setOnClickListener { setValue(getValue() + 30_000) }
        plus5m.setOnClickListener { setValue(getValue() + 300_000) }
        minus5s.setOnClickListener { setValue(getValue() - 5_000) }
        minus30s.setOnClickListener { setValue(getValue() - 30_000) }
        minus5m.setOnClickListener { setValue(getValue() - 300_000) }
    }

    companion object {
        private const val TAG = "TimeDialog"
        private const val KEY_REQUEST = "KEY_REQUEST"
        private const val KEY_RESULT = "KEY_RESULT"
        private const val KEY_HOUR_ENABLED = "KEY_HOUR_ENABLED"
        private const val KEY_TIME = "KEY_TIME"

        fun registerListener(
            activity: FragmentActivity,
            requestKey: String,
            listener: (Long) -> Unit
        ) {
            val manager = activity.supportFragmentManager
            manager.setFragmentResultListener(requestKey, activity) { _, result ->
                listener(result.getLong(KEY_RESULT))
            }
        }

        fun show(
            activity: FragmentActivity,
            requestKey: String,
            time: Long,
            hourEnabled: Boolean,
        ) {
            val manager = activity.supportFragmentManager
            if (manager.findFragmentByTag(TAG) != null) return
            if (manager.isStateSaved) return
            TimeDialog().also {
                it.arguments = bundleOf(
                    KEY_REQUEST to requestKey,
                    KEY_TIME to time,
                    KEY_HOUR_ENABLED to hourEnabled,
                )
            }.show(manager, TAG)
        }
    }
}

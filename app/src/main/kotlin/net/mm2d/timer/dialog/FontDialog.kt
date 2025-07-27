package net.mm2d.timer.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import net.mm2d.timer.R
import net.mm2d.timer.settings.Font
import net.mm2d.timer.util.getSerializableSafely

class FontDialog : DialogFragment() {
    override fun onCreateDialog(
        savedInstanceState: Bundle?,
    ): Dialog {
        val activity = requireActivity()
        val arguments = requireArguments()
        val requestKey = arguments.getString(KEY_REQUEST_KEY, "")
        val adapter = ArrayAdapter(
            activity,
            android.R.layout.simple_list_item_1,
            Font.entries.map {
                when (it) {
                    Font.LED_7SEGMENT -> R.string.menu_description_font_led_7segment
                    Font.ROBOTO -> R.string.menu_description_font_roboto
                }.let { getString(it) }
            },
        )
        val listener = DialogInterface.OnClickListener { dialog, which ->
            val font = Font.entries[which]
            parentFragmentManager.setFragmentResult(
                requestKey,
                bundleOf(KEY_RESULT to font),
            )
            dialog.dismiss()
        }
        return AlertDialog.Builder(activity)
            .setTitle(R.string.menu_title_font)
            .setAdapter(adapter, listener)
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    companion object {
        private const val TAG = "FontDialog"
        private const val KEY_REQUEST_KEY = "KEY_REQUEST_KEY"
        private const val KEY_RESULT = "KEY_RESULT"

        fun registerListener(
            activity: FragmentActivity,
            requestKey: String,
            listener: (Font) -> Unit,
        ) {
            val manager = activity.supportFragmentManager
            manager.setFragmentResultListener(requestKey, activity) { _, result ->
                val font = result.getSerializableSafely<Font>(KEY_RESULT)
                    ?: return@setFragmentResultListener
                listener(font)
            }
        }

        fun show(
            activity: FragmentActivity,
            requestKey: String,
        ) {
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            FontDialog().also { dialog ->
                dialog.arguments = bundleOf(
                    KEY_REQUEST_KEY to requestKey,
                )
            }.show(manager, TAG)
        }
    }
}

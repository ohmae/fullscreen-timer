package net.mm2d.timer.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.mm2d.timer.R
import net.mm2d.timer.databinding.ItemOrientationBinding
import net.mm2d.timer.settings.Orientation
import net.mm2d.timer.util.getSerializableSafely

class OrientationDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val arguments = requireArguments()
        val requestKey = arguments.getString(KEY_REQUEST_KEY, "")
        val recyclerView = RecyclerView(activity)
        recyclerView.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = Adapter(activity) {
            parentFragmentManager.setFragmentResult(
                requestKey,
                bundleOf(RESULT_ORIENTATION to it)
            )
            dialog?.cancel()
        }
        recyclerView.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        recyclerView.isVerticalFadingEdgeEnabled = true
        return AlertDialog.Builder(activity)
            .setView(recyclerView)
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    class Adapter(
        activity: Activity,
        private val onClickListener: (orientation: Orientation) -> Unit
    ) : RecyclerView.Adapter<ViewHolder>() {
        private val inflater = activity.layoutInflater
        private val orientations = Orientation.values()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(ItemOrientationBinding.inflate(inflater, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val orientation = orientations[position]
            holder.bind(orientation)
            holder.itemView.setOnClickListener {
                onClickListener(orientation)
            }
        }

        override fun getItemCount(): Int = orientations.size
    }

    class ViewHolder(
        private val binding: ItemOrientationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(orientation: Orientation) {
            binding.icon.setImageResource(orientation.icon)
            binding.description.setText(orientation.description)
        }
    }

    companion object {
        private const val TAG = "OrientationDialog"
        private const val KEY_REQUEST_KEY = "KEY_REQUEST_KEY"
        private const val RESULT_ORIENTATION = "RESULT_ORIENTATION"

        fun registerListener(
            activity: FragmentActivity,
            requestKey: String,
            listener: (Orientation) -> Unit
        ) {
            val manager = activity.supportFragmentManager
            manager.setFragmentResultListener(requestKey, activity) { _, result ->
                val orientation = result.getSerializableSafely<Orientation>(RESULT_ORIENTATION)
                    ?: return@setFragmentResultListener
                listener(orientation)
            }
        }

        fun show(activity: FragmentActivity, requestKey: String) {
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            OrientationDialog().also { dialog ->
                dialog.arguments = bundleOf(
                    KEY_REQUEST_KEY to requestKey
                )
            }.show(manager, TAG)
        }
    }
}

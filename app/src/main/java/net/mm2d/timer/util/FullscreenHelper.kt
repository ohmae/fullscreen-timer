package net.mm2d.timer.util

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController

@Suppress("DEPRECATION")
class FullscreenHelper(
    private val window: Window,
) {
    private val decorView: View = window.decorView
    private val hideTask = Runnable {
        window.decorView.systemUiVisibility = SYSTEM_UI_INVISIBLE
    }

    fun start(enable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (enable) {
                window.insetsController?.let {
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    it.hide(WindowInsets.Type.systemBars())
                }
            } else {
                window.insetsController?.let {
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
                    it.show(WindowInsets.Type.systemBars())
                }
            }
        } else {
            if (enable) {
                decorView.systemUiVisibility = SYSTEM_UI_INVISIBLE
                decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                    if (visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0) {
                        decorView.systemUiVisibility = SYSTEM_UI_VISIBLE
                        decorView.removeCallbacks(hideTask)
                        decorView.postDelayed(hideTask, HIDE_INTERVAL)
                    }
                }
            } else {
                decorView.systemUiVisibility = SYSTEM_UI_VISIBLE
                decorView.removeCallbacks(hideTask)
                decorView.setOnSystemUiVisibilityChangeListener(null)
            }
        }
    }

    fun stop() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            decorView.removeCallbacks(hideTask)
            decorView.setOnSystemUiVisibilityChangeListener(null)
        }
    }

    companion object {
        private const val HIDE_INTERVAL = 3000L
        private const val SYSTEM_UI_VISIBLE: Int = (View.SYSTEM_UI_FLAG_LOW_PROFILE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        private const val SYSTEM_UI_INVISIBLE: Int = (View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }
}

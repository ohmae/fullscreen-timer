/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getStringOrThrow
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import net.mm2d.timer.R
import net.mm2d.timer.databinding.ViewColorMenuBinding

class ColorMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding = ViewColorMenuBinding.inflate(LayoutInflater.from(context), this)
    private var color: Int = 0

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ColorMenuView)
        binding.menuTitle.text = ta.getStringOrThrow(R.styleable.ColorMenuView_title)
        binding.menuDescription.text = ta.getStringOrThrow(R.styleable.ColorMenuView_description)
        binding.menuColor.setImageColor(color)
        ta.recycle()
    }

    fun getColor(): Int = color

    fun setColor(color: Int) {
        this.color = color
        binding.menuColor.setImageColor(color)
    }

    private fun ImageView.setImageColor(@ColorInt color: Int) {
        setColorFilter(Color.rgb(color.red, color.green, color.blue))
        imageAlpha = color.alpha
    }
}

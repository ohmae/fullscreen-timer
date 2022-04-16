/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getStringOrThrow
import net.mm2d.timer.R
import net.mm2d.timer.databinding.ViewColorMenuBinding
import net.mm2d.timer.util.alpha
import net.mm2d.timer.util.opaque

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
        setColorFilter(color.opaque())
        imageAlpha = color.alpha()
    }
}

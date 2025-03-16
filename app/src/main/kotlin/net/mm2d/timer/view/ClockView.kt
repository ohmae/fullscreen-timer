/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import net.mm2d.timer.R
import net.mm2d.timer.R.drawable
import net.mm2d.timer.databinding.ViewClockBinding
import java.util.Calendar

class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    private var calendar = Calendar.getInstance()

    init {
        orientation = HORIZONTAL
        gravity = Gravity.BOTTOM
    }

    private val binding: ViewClockBinding =
        ViewClockBinding.inflate(LayoutInflater.from(context), this)
    private val viewSizes = listOf(
        ViewSize(binding.third1, 3, 4),
        ViewSize(binding.colon2, 3, 1),
        ViewSize(binding.second10, 3, 4),
        ViewSize(binding.second1, 3, 4),
        ViewSize(binding.colon1, 3, 1),
        ViewSize(binding.first10, 3, 4),
        ViewSize(binding.first1, 3, 4),
        ViewSize(binding.small10, 1, 4),
        ViewSize(binding.small1, 1, 4),
    )

    fun setDigit(
        third: Boolean = false,
        small: Boolean = true,
        hourFormat24: Boolean = true,
    ) {
        binding.third1.isVisible = third
        binding.colon2.isVisible = third
        binding.small10.isVisible = small
        binding.small1.isVisible = small
        binding.amPm.isVisible = !hourFormat24
    }

    fun updateTime(
        millis: Long,
    ) {
        val base = millis / 10
        setNumber(binding.small1, base)
        setNumber(binding.small10, base / 10)
        setNumber(binding.first1, base / 100)
        setNumber(binding.first10, (base / 10_00) % 6)
        setNumber(binding.second1, base / 60_00)
        if (binding.third1.isVisible) {
            setNumber(binding.second10, (base / 600_00) % 6)
            setNumber(binding.third1, base / 3600_00)
        } else {
            setNumber(binding.second10, base / 600_00)
        }
    }

    fun updateClock(
        millis: Long,
    ) {
        calendar.timeInMillis = millis
        val second = calendar[Calendar.SECOND]
        setNumber(binding.small1, second)
        setNumber(binding.small10, second / 10)
        val minute = calendar[Calendar.MINUTE]
        setNumber(binding.first1, minute)
        setNumber(binding.first10, minute / 10)
        val hour = calendar[if (binding.amPm.isVisible) Calendar.HOUR else Calendar.HOUR_OF_DAY]
        setNumber(binding.second1, hour)
        setNumber(binding.second10, hour / 10)
        binding.amPm.setText(if (calendar[Calendar.AM_PM] == Calendar.AM) R.string.am else R.string.pm)
    }

    fun setColor(
        color: Int,
    ) {
        val colorStateList = ColorStateList.valueOf(color)
        binding.small1.backgroundTintList = colorStateList
        binding.small10.backgroundTintList = colorStateList
        binding.first1.backgroundTintList = colorStateList
        binding.first10.backgroundTintList = colorStateList
        binding.second1.backgroundTintList = colorStateList
        binding.second10.backgroundTintList = colorStateList
        binding.third1.backgroundTintList = colorStateList
        binding.small1.setColorFilter(color)
        binding.small10.setColorFilter(color)
        binding.first1.setColorFilter(color)
        binding.first10.setColorFilter(color)
        binding.second1.setColorFilter(color)
        binding.second10.setColorFilter(color)
        binding.third1.setColorFilter(color)
        binding.colon1.setColorFilter(color)
        binding.colon2.setColorFilter(color)
        binding.amPm.setTextColor(color)
    }

    private fun setNumber(
        view: ImageView,
        number: Int,
    ) {
        view.setImageResource(getRes((number % 10)))
    }

    private fun setNumber(
        view: ImageView,
        number: Long,
    ) {
        view.setImageResource(getRes((number % 10).toInt()))
    }

    private fun getRes(
        number: Int,
    ): Int =
        when (number) {
            1 -> drawable.ic_1
            2 -> drawable.ic_2
            3 -> drawable.ic_3
            4 -> drawable.ic_4
            5 -> drawable.ic_5
            6 -> drawable.ic_6
            7 -> drawable.ic_7
            8 -> drawable.ic_8
            9 -> drawable.ic_9
            else -> drawable.ic_0
        }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        val xSpec = MeasureSpec.getMode(widthMeasureSpec)
        val ySpec = MeasureSpec.getMode(heightMeasureSpec)
        if (xSpec == MeasureSpec.UNSPECIFIED || ySpec == MeasureSpec.UNSPECIFIED) {
            viewSizes.forEach {
                it.view.updateLayoutParams<LayoutParams> {
                    width = LayoutParams.WRAP_CONTENT
                    height = LayoutParams.WRAP_CONTENT
                }
            }
        } else {
            val amPmWidth = if (binding.amPm.isVisible) {
                (binding.amPm.layoutParams as MarginLayoutParams).let {
                    it.width + it.marginStart + it.marginEnd
                }
            } else {
                0
            }
            val xMax = MeasureSpec.getSize(widthMeasureSpec) - amPmWidth
            val yMax = MeasureSpec.getSize(heightMeasureSpec)
            val xScore = viewSizes.filter { it.view.isVisible }
                .sumOf { it.width }
            val yScore = viewSizes.maxOf { it.height }
            val unit = minOf(xMax / xScore, yMax / yScore)
            viewSizes.forEach {
                it.view.updateLayoutParams<LayoutParams> {
                    it.view.updateLayoutParams<LayoutParams> {
                        width = it.width * unit
                        height = it.height * unit
                    }
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private data class ViewSize(
        val view: View,
        val scale: Int,
        val x: Int,
        val y: Int = 6,
    ) {
        val width = x * scale
        val height = y * scale
    }
}

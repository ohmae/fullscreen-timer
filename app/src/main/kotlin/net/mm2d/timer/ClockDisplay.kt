/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mm2d.timer.MainViewModel.UiState
import net.mm2d.timer.settings.Font
import net.mm2d.timer.settings.Mode
import java.util.Calendar

/**
 * `ClockView`の桁比率を保ったまま、利用可能領域へ収まる最大サイズで時刻を描画する。
 */
@Composable
internal fun ClockDisplay(
    uiState: UiState,
    modifier: Modifier = Modifier,
) {
    val value = remember(
        uiState.mode,
        uiState.timeMillis,
        uiState.hourEnabled,
        uiState.hourFormat24,
        uiState.millisecondEnabled,
        uiState.secondEnabled,
    ) {
        DisplayValue.from(uiState)
    }
    BoxWithConstraints(
        modifier = modifier.semantics {
            contentDescription = value.contentDescription
        },
        contentAlignment = Alignment.Center,
    ) {
        val hasLeadingDigit = value.leadingDigit != null
        val hasSmallDigits = value.smallTens != null
        val hasAmPm = value.amPm != null
        val layoutSizes = remember(maxWidth, maxHeight, hasLeadingDigit, hasSmallDigits, hasAmPm) {
            calculateLayoutSizes(
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                hasLeadingDigit = hasLeadingDigit,
                hasSmallDigits = hasSmallDigits,
                hasAmPm = hasAmPm,
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
        ) {
            value.amPm?.let {
                AmPmLabel(
                    text = it,
                    foregroundColor = Color(uiState.foregroundColor),
                    height = layoutSizes.normalDigitSize.height,
                )
            }
            value.leadingDigit?.let {
                Digit(
                    number = it,
                    font = uiState.font,
                    foregroundColor = Color(uiState.foregroundColor),
                    size = layoutSizes.normalDigitSize,
                )
                Colon(
                    foregroundColor = Color(uiState.foregroundColor),
                    size = layoutSizes.colonSize,
                )
            }
            Digit(
                number = value.leftTens,
                font = uiState.font,
                foregroundColor = Color(uiState.foregroundColor),
                size = layoutSizes.normalDigitSize,
            )
            Digit(
                number = value.leftOnes,
                font = uiState.font,
                foregroundColor = Color(uiState.foregroundColor),
                size = layoutSizes.normalDigitSize,
            )
            Colon(
                foregroundColor = Color(uiState.foregroundColor),
                size = layoutSizes.colonSize,
            )
            Digit(
                number = value.rightTens,
                font = uiState.font,
                foregroundColor = Color(uiState.foregroundColor),
                size = layoutSizes.normalDigitSize,
            )
            Digit(
                number = value.rightOnes,
                font = uiState.font,
                foregroundColor = Color(uiState.foregroundColor),
                size = layoutSizes.normalDigitSize,
            )
            if (value.smallTens != null && value.smallOnes != null) {
                Digit(
                    number = value.smallTens,
                    font = uiState.font,
                    foregroundColor = Color(uiState.foregroundColor),
                    size = layoutSizes.smallDigitSize,
                )
                Digit(
                    number = value.smallOnes,
                    font = uiState.font,
                    foregroundColor = Color(uiState.foregroundColor),
                    size = layoutSizes.smallDigitSize,
                )
            }
        }
    }
}

@Composable
private fun Digit(
    number: Int,
    font: Font,
    foregroundColor: Color,
    size: DigitSize,
) {
    Box(
        modifier = Modifier.size(size.width, size.height),
    ) {
        if (font == Font.LED_7SEGMENT) {
            Image(
                painter = painterResource(R.drawable.bg_num),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(foregroundColor),
            )
        }
        Image(
            painter = painterResource(digitResource(number, font)),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(foregroundColor),
        )
    }
}

@Composable
private fun Colon(
    foregroundColor: Color,
    size: DigitSize,
) {
    Image(
        painter = painterResource(R.drawable.led_colon),
        contentDescription = null,
        modifier = Modifier.size(size.width, size.height),
        contentScale = ContentScale.FillBounds,
        colorFilter = ColorFilter.tint(foregroundColor),
    )
}

@Composable
private fun AmPmLabel(
    text: String,
    foregroundColor: Color,
    height: Dp,
) {
    Box(
        modifier = Modifier.size(width = AM_PM_WIDTH, height = height),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .width(AM_PM_TEXT_WIDTH)
                .padding(bottom = AM_PM_BOTTOM_PADDING),
            color = foregroundColor,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@DrawableRes
private fun digitResource(
    number: Int,
    font: Font,
): Int {
    val normalizedNumber = number.mod(10)
    return when (font) {
        Font.LED_7SEGMENT -> LED_DIGIT_RESOURCES[normalizedNumber]
        Font.ROBOTO -> ROBOTO_DIGIT_RESOURCES[normalizedNumber]
    }
}

private data class DigitSize(
    val width: Dp,
    val height: Dp,
)

private data class ClockLayoutSizes(
    val normalDigitSize: DigitSize,
    val colonSize: DigitSize,
    val smallDigitSize: DigitSize,
)

private fun calculateLayoutSizes(
    maxWidth: Dp,
    maxHeight: Dp,
    hasLeadingDigit: Boolean,
    hasSmallDigits: Boolean,
    hasAmPm: Boolean,
): ClockLayoutSizes {
    val normalDigitCount = NORMAL_DIGIT_COUNT + if (hasLeadingDigit) 1 else 0
    val colonCount = COLON_COUNT + if (hasLeadingDigit) 1 else 0
    val smallDigitCount = if (hasSmallDigits) 2 else 0
    val widthScore =
        normalDigitCount * NORMAL_DIGIT_WIDTH_SCORE +
            colonCount * COLON_WIDTH_SCORE +
            smallDigitCount * SMALL_DIGIT_WIDTH_SCORE
    val amPmWidth = if (hasAmPm) AM_PM_WIDTH else 0.dp
    val unit = minOf(
        (maxWidth - amPmWidth).coerceAtLeast(0.dp) / widthScore,
        maxHeight / NORMAL_DIGIT_HEIGHT_SCORE,
    )
    return ClockLayoutSizes(
        normalDigitSize = DigitSize(
            width = unit * NORMAL_DIGIT_WIDTH_SCORE,
            height = unit * NORMAL_DIGIT_HEIGHT_SCORE,
        ),
        colonSize = DigitSize(
            width = unit * COLON_WIDTH_SCORE,
            height = unit * NORMAL_DIGIT_HEIGHT_SCORE,
        ),
        smallDigitSize = DigitSize(
            width = unit * SMALL_DIGIT_WIDTH_SCORE,
            height = unit * SMALL_DIGIT_HEIGHT_SCORE,
        ),
    )
}

private data class DisplayValue(
    val leadingDigit: Int?,
    val leftTens: Int,
    val leftOnes: Int,
    val rightTens: Int,
    val rightOnes: Int,
    val smallTens: Int?,
    val smallOnes: Int?,
    val amPm: String?,
    val contentDescription: String,
) {
    companion object {
        fun from(
            uiState: UiState,
        ): DisplayValue =
            when (uiState.mode) {
                Mode.CLOCK -> fromClock(uiState)

                Mode.STOPWATCH,
                Mode.TIMER,
                    -> fromElapsedTime(uiState)
            }

        private fun fromClock(
            uiState: UiState,
        ): DisplayValue {
            val calendar = Calendar.getInstance().also {
                it.timeInMillis = uiState.timeMillis
            }
            val hour = calendar[
                if (uiState.hourFormat24) Calendar.HOUR_OF_DAY else Calendar.HOUR,
            ]
            val minute = calendar[Calendar.MINUTE]
            val second = calendar[Calendar.SECOND]
            val amPm = if (uiState.hourFormat24) {
                null
            } else if (calendar[Calendar.AM_PM] == Calendar.AM) {
                "AM"
            } else {
                "PM"
            }
            return DisplayValue(
                leadingDigit = null,
                leftTens = (hour / 10).mod(10),
                leftOnes = hour.mod(10),
                rightTens = (minute / 10).mod(10),
                rightOnes = minute.mod(10),
                smallTens = if (uiState.secondEnabled) (second / 10).mod(10) else null,
                smallOnes = if (uiState.secondEnabled) second.mod(10) else null,
                amPm = amPm,
                contentDescription = buildString {
                    append(hour.twoDigits())
                    append(':')
                    append(minute.twoDigits())
                    if (uiState.secondEnabled) {
                        append(':')
                        append(second.twoDigits())
                    }
                    amPm?.let {
                        append(' ')
                        append(it)
                    }
                },
            )
        }

        private fun fromElapsedTime(
            uiState: UiState,
        ): DisplayValue {
            val base = uiState.timeMillis.coerceAtLeast(0L) / 10L
            val hundredths = (base % 100L).toInt()
            val seconds = ((base / 100L) % 60L).toInt()
            val totalMinutes = (base / 6_000L).toInt()
            val hours = totalMinutes / 60
            val minutes = if (uiState.hourEnabled) totalMinutes % 60 else totalMinutes
            return DisplayValue(
                leadingDigit = if (uiState.hourEnabled) hours.mod(10) else null,
                leftTens = (minutes / 10).mod(10),
                leftOnes = minutes.mod(10),
                rightTens = (seconds / 10).mod(10),
                rightOnes = seconds.mod(10),
                smallTens = if (uiState.millisecondEnabled) (hundredths / 10).mod(10) else null,
                smallOnes = if (uiState.millisecondEnabled) hundredths.mod(10) else null,
                amPm = null,
                contentDescription = buildString {
                    if (uiState.hourEnabled) {
                        append(hours)
                        append(':')
                    }
                    append(minutes.twoDigits())
                    append(':')
                    append(seconds.twoDigits())
                    if (uiState.millisecondEnabled) {
                        append('.')
                        append(hundredths.twoDigits())
                    }
                },
            )
        }
    }
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private val LED_DIGIT_RESOURCES = intArrayOf(
    R.drawable.led_0,
    R.drawable.led_1,
    R.drawable.led_2,
    R.drawable.led_3,
    R.drawable.led_4,
    R.drawable.led_5,
    R.drawable.led_6,
    R.drawable.led_7,
    R.drawable.led_8,
    R.drawable.led_9,
)

private val ROBOTO_DIGIT_RESOURCES = intArrayOf(
    R.drawable.roboto_medium_0,
    R.drawable.roboto_medium_1,
    R.drawable.roboto_medium_2,
    R.drawable.roboto_medium_3,
    R.drawable.roboto_medium_4,
    R.drawable.roboto_medium_5,
    R.drawable.roboto_medium_6,
    R.drawable.roboto_medium_7,
    R.drawable.roboto_medium_8,
    R.drawable.roboto_medium_9,
)

private const val NORMAL_DIGIT_COUNT = 4
private const val COLON_COUNT = 1
private const val NORMAL_DIGIT_WIDTH_SCORE = 12
private const val NORMAL_DIGIT_HEIGHT_SCORE = 18
private const val COLON_WIDTH_SCORE = 3
private const val SMALL_DIGIT_WIDTH_SCORE = 4
private const val SMALL_DIGIT_HEIGHT_SCORE = 6
private val AM_PM_WIDTH = 38.dp
private val AM_PM_TEXT_WIDTH = 30.dp
private val AM_PM_BOTTOM_PADDING = 8.dp

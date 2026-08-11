/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.dialog

internal data class TimeDialogInput(
    val hour: String,
    val minute: String,
    val second: String,
    val hourEnabled: Boolean,
) {
    val timeMillisOrNull: Long?
        get() {
            val hourValue = hour.toIntOrNull()?.takeIf { it in 0..MAX_HOUR } ?: return null
            val minuteValue = minute.toIntOrNull()?.takeIf { it in 0..maxMinute } ?: return null
            val secondValue = second.toIntOrNull()?.takeIf { it in 0..MAX_MINUTE_OR_SECOND } ?: return null
            return (
                hourValue * SECONDS_PER_HOUR +
                    minuteValue * SECONDS_PER_MINUTE +
                    secondValue
                ) * MILLIS_PER_SECOND
        }

    val maxMinute: Int
        get() = if (hourEnabled) MAX_MINUTE_OR_SECOND else MAX_MINUTE_WITHOUT_HOUR

    fun update(
        unit: Unit,
        value: String,
    ): TimeDialogInput {
        if (value.length > unit.maxDigits || value.any { !it.isDigit() }) return this
        return when (unit) {
            Unit.HOUR -> if (hourEnabled) copy(hour = value) else this
            Unit.MINUTE -> copy(minute = value)
            Unit.SECOND -> copy(second = value)
        }
    }

    fun adjust(
        unit: Unit,
        amount: Int,
    ): TimeDialogInput {
        val normalized = resetIfInvalid(unit)
        val currentSeconds = normalized.timeMillisOrNull?.div(MILLIS_PER_SECOND) ?: return normalized
        val adjustedSeconds = (
            currentSeconds + amount * unit.seconds
            ).coerceIn(0L, maxSeconds)
        return fromSeconds(adjustedSeconds, hourEnabled)
    }

    fun resetIfInvalid(
        unit: Unit,
    ): TimeDialogInput {
        if (isValid(unit)) return this
        return when (unit) {
            Unit.HOUR -> copy(hour = "0")
            Unit.MINUTE -> copy(minute = "0")
            Unit.SECOND -> copy(second = "0")
        }
    }

    fun isValid(
        unit: Unit,
    ): Boolean =
        when (unit) {
            Unit.HOUR -> !hourEnabled || hour.toIntOrNull() in 0..MAX_HOUR
            Unit.MINUTE -> minute.toIntOrNull() in 0..maxMinute
            Unit.SECOND -> second.toIntOrNull() in 0..MAX_MINUTE_OR_SECOND
        }

    enum class Unit(
        val seconds: Int,
    ) {
        HOUR(SECONDS_PER_HOUR),
        MINUTE(SECONDS_PER_MINUTE),
        SECOND(1),
        ;

        val maxDigits: Int
            get() =
                when (this) {
                    HOUR -> 1
                    MINUTE -> 2
                    SECOND -> 2
                }
    }

    companion object {
        fun fromMillis(
            timeMillis: Long,
            hourEnabled: Boolean,
        ): TimeDialogInput =
            fromSeconds(
                seconds = (timeMillis / MILLIS_PER_SECOND).coerceIn(
                    minimumValue = 0L,
                    maximumValue = if (hourEnabled) MAX_SECONDS_WITH_HOUR else MAX_SECONDS_WITHOUT_HOUR,
                ),
                hourEnabled = hourEnabled,
            )

        private fun fromSeconds(
            seconds: Long,
            hourEnabled: Boolean,
        ): TimeDialogInput {
            val hour = if (hourEnabled) seconds / SECONDS_PER_HOUR else 0L
            val minute = if (hourEnabled) {
                seconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
            } else {
                seconds / SECONDS_PER_MINUTE
            }
            return TimeDialogInput(
                hour = hour.toString(),
                minute = minute.toString(),
                second = (seconds % SECONDS_PER_MINUTE).toString(),
                hourEnabled = hourEnabled,
            )
        }

        private const val MILLIS_PER_SECOND = 1_000L
        private const val SECONDS_PER_MINUTE = 60
        private const val SECONDS_PER_HOUR = 3_600
        private const val MAX_HOUR = 9
        private const val MAX_MINUTE_OR_SECOND = 59
        private const val MAX_MINUTE_WITHOUT_HOUR = 99
        private const val MAX_SECONDS_WITH_HOUR = 35_999L
        private const val MAX_SECONDS_WITHOUT_HOUR = 5_999L
    }

    private val maxSeconds: Long
        get() = if (hourEnabled) MAX_SECONDS_WITH_HOUR else MAX_SECONDS_WITHOUT_HOUR
}

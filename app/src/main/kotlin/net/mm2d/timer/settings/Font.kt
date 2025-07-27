package net.mm2d.timer.settings

enum class Font {
    LED_7SEGMENT,
    ROBOTO,
    ;

    companion object {
        fun of(
            value: String?,
        ): Font = entries.firstOrNull { it.name == value } ?: LED_7SEGMENT
    }
}

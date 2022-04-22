package com.cozmicgames.input

interface MouseButton {
    val ordinal: Int
}

enum class MouseButtons : MouseButton {
    LEFT,
    MIDDLE,
    RIGHT;

    companion object {
        val values = values()

        val count get() = values.size

        fun fromInt(i: Int): MouseButton? {
            return if (i < 0 || i >= count) null else values[i]
        }
    }
}
package com.gratedgames.input

interface MouseButton {
    val ordinal: Int
}

enum class MouseButtons : MouseButton {
    LEFT,
    MIDDLE,
    RIGHT;

    companion object {
        val count = values().size
    }
}
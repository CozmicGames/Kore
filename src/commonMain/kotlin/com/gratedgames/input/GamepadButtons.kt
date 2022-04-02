package com.gratedgames.input

interface GamepadButton {
    val ordinal: Int
}

enum class GamepadButtons : GamepadButton {
    A,
    B,
    X,
    Y,
    LEFT_BUMPER,
    RIGHT_BUMPER,
    BACK,
    START,
    GUIDE,
    LEFT_THUMB,
    RIGHT_THUMB,
    DPAD_UP,
    DPAD_RIGHT,
    DPAD_DOWN,
    DPAD_LEFT;

    companion object {
        val count = values().size
    }
}

val GamepadButtons.CROSS get() = GamepadButtons.A

val GamepadButtons.CIRCLE get() = GamepadButtons.B

val GamepadButtons.SQUARE get() = GamepadButtons.X

val GamepadButtons.TRIANGLE get() = GamepadButtons.Y

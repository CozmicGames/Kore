package com.cozmicgames.input

import com.cozmicgames.utils.maths.Vector2

class GamepadStick {
    val current = Vector2()
    val last = Vector2()
    val delta = Vector2()
}

enum class GamepadSticks {
    LEFT,
    RIGHT
}

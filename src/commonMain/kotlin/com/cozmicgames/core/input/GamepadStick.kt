package com.cozmicgames.core.input

import com.cozmicgames.core.utils.maths.Vector2

class GamepadStick {
    val current = Vector2()
    val last = Vector2()
    val delta = Vector2()
}

enum class GamepadSticks {
    LEFT,
    RIGHT
}

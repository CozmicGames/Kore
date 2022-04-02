package com.gratedgames.input

class DesktopGamepad() : Gamepad() {
    override val canVibrate = true

    override val id: String
        get() = TODO("Not yet implemented")

    override val isVibrating: Boolean
        get() = TODO("Not yet implemented")

    override val playerIndex: Int
        get() = TODO("Not yet implemented")

    override fun vibrate(duration: Double, strengthLeft: Float, strengthRight: Float) {

    }
}
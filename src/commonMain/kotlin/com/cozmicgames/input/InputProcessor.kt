package com.cozmicgames.input

interface InputProcessor {
    fun onKey(key: Key, down: Boolean, time: Double) {}
    fun onKeyTyped(key: Key, time: Double) {}
    fun onTouch(x: Int, y: Int, button: MouseButton, pointer: Int, down: Boolean, time: Double) {}
    fun onMouseMove(x: Int, y: Int, time: Double) {}
    fun onScroll(amount: Float, time: Double) {}
}
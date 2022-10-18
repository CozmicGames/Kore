package com.cozmicgames.input

interface InputProcessor {
    fun onKey(key: Key, down: Boolean, time: Double)
    fun onTouch(x: Int, y: Int, button: MouseButton, pointer: Int, down: Boolean, time: Double)
    fun onScroll(x: Float, y: Float, time: Double)
    fun onChar(char: Char, time: Double)
    fun onGamepad(id: Int, isConnected: Boolean, time: Double)
}
package com.cozmicgames.input

import com.cozmicgames.graphics.Image
import com.cozmicgames.utils.Disposable

interface InputListener {
    fun onKey(key: Key, down: Boolean, time: Double) {}
    fun onMouseButton(button: MouseButton, down: Boolean, time: Double) {}
    fun onTouch(x: Int, y: Int, pointer: Int, down: Boolean, time: Double) {}
    fun onScroll(x: Float, y: Float, time: Double) {}
    fun onChar(char: Char, time: Double) {}
    fun onGamepad(id: Int, isConnected: Boolean, time: Double) {}
}

interface Input : Disposable {
    val isTouched: Boolean
    val justTouchedDown: Boolean
    val justTouchedUp: Boolean
    val x: Int
    val y: Int
    val lastX: Int
    val lastY: Int
    val deltaX: Int
    val deltaY: Int

    var cursor: Cursor

    var isCursorGrabbed: Boolean

    val gamepads: List<Gamepad>

    fun isKeyDown(key: Key): Boolean
    fun isKeyJustDown(key: Key): Boolean
    fun isKeyJustUp(key: Key): Boolean
    fun isButtonDown(button: MouseButton): Boolean
    fun isButtonJustDown(button: MouseButton): Boolean
    fun isButtonJustUp(button: MouseButton): Boolean
    fun isTouched(pointer: Int): Boolean
    fun addListener(listener: InputListener)
    fun removeListener(listener: InputListener)
    fun createStandardCursor(type: Cursor.StandardType): Cursor
    fun createCursor(image: Image, xHot: Int, yHot: Int): Cursor
}

fun Input.getGamepad(id: Int) = gamepads.find { it.id == id }

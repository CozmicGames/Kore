package com.cozmicgames.input

import com.cozmicgames.utils.Disposable

typealias KeyListener = (Key, Boolean) -> Unit
typealias MouseButtonListener = (MouseButton, Boolean) -> Unit
typealias TouchListener = (Int, Int, Int, Boolean) -> Unit
typealias ScrollListener = (Float, Float) -> Unit
typealias CharListener = (Char) -> Unit
typealias GamepadListener = (Gamepad, Boolean) -> Unit

interface InputListener {
    fun onKey(key: Key, down: Boolean) {}
    fun onMouseButton(button: MouseButton, down: Boolean) {}
    fun onTouch(x: Int, y: Int, pointer: Int, down: Boolean) {}
    fun onScroll(x: Float, y: Float) {}
    fun onChar(char: Char) {}
    fun onGamepad(gamepad: Gamepad, isConnected: Boolean) {}
}

interface Input : Disposable {
    val isTouched: Boolean
    val justTouched: Boolean
    val x: Int
    val y: Int
    val lastX: Int
    val lastY: Int
    val deltaX: Int
    val deltaY: Int

    var isCursorGrabbed: Boolean

    val gamepads: List<Gamepad>

    fun isKeyDown(key: Key): Boolean
    fun isKeyJustDown(key: Key): Boolean
    fun isKeyJustUp(key: Key): Boolean
    fun isButtonDown(button: MouseButton): Boolean
    fun isButtonJustDown(button: MouseButton): Boolean
    fun isButtonJustUp(button: MouseButton): Boolean

    fun addKeyListener(listener: KeyListener)
    fun addMouseButtonListener(listener: MouseButtonListener)
    fun addTouchListener(listener: TouchListener)
    fun addScrollListener(listener: ScrollListener)
    fun addCharListener(listener: CharListener)
    fun addGamepadListener(listener: GamepadListener)

    fun removeKeyListener(listener: KeyListener)
    fun removeMouseButtonListener(listener: MouseButtonListener)
    fun removeTouchListener(listener: TouchListener)
    fun removeScrollListener(listener: ScrollListener)
    fun removeCharListener(listener: CharListener)
    fun removeGamepadListener(listener: GamepadListener)
}

fun Input.addListener(listener: InputListener) {
    addKeyListener(listener::onKey)
    addMouseButtonListener(listener::onMouseButton)
    addTouchListener(listener::onTouch)
    addScrollListener(listener::onScroll)
    addCharListener(listener::onChar)
    addGamepadListener(listener::onGamepad)
}

fun Input.removeListener(listener: InputListener) {
    removeKeyListener(listener::onKey)
    removeMouseButtonListener(listener::onMouseButton)
    removeTouchListener(listener::onTouch)
    removeScrollListener(listener::onScroll)
    removeCharListener(listener::onChar)
    removeGamepadListener(listener::onGamepad)
}

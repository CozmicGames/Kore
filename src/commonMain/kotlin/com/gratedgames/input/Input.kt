package com.gratedgames.input

import com.gratedgames.utils.Disposable

typealias KeyListener = (Key, Boolean) -> Unit
typealias MouseButtonListener = (MouseButton, Boolean) -> Unit
typealias MouseListener = (Int, Int) -> Unit
typealias TouchListener = (Int, Int, Int, Boolean) -> Unit
typealias ScrollListener = (Float) -> Unit
typealias CharListener = (Char) -> Unit
typealias GamepadListener = (Gamepad, Boolean) -> Unit

interface InputListener {
    fun onKey(key: Key, down: Boolean) {}
    fun onMouseButton(button: MouseButton, down: Boolean) {}
    fun onMouseMove(x: Int, y: Int) {}
    fun onTouch(x: Int, y: Int, pointer: Int, down: Boolean) {}
    fun onScroll(amount: Float) {}
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
    fun addMouseListener(listener: MouseListener)
    fun addTouchListener(listener: TouchListener)
    fun addScrollListener(listener: ScrollListener)
    fun addCharListener(listener: CharListener)
    fun addGamepadListener(listener: GamepadListener)

    fun removeKeyListener(listener: KeyListener)
    fun removeMouseButtonListener(listener: MouseButtonListener)
    fun removeMouseListener(listener: MouseListener)
    fun removeTouchListener(listener: TouchListener)
    fun removeScrollListener(listener: ScrollListener)
    fun removeCharListener(listener: CharListener)
    fun removeGamepadListener(listener: GamepadListener)
}

fun Input.addListener(listener: InputListener) {
    addKeyListener(listener::onKey)
    addMouseButtonListener(listener::onMouseButton)
    addMouseListener(listener::onMouseMove)
    addTouchListener(listener::onTouch)
    addScrollListener(listener::onScroll)
    addCharListener(listener::onChar)
    addGamepadListener(listener::onGamepad)
}

fun Input.removeListener(listener: InputListener) {
    removeKeyListener(listener::onKey)
    removeMouseButtonListener(listener::onMouseButton)
    removeMouseListener(listener::onMouseMove)
    removeTouchListener(listener::onTouch)
    removeScrollListener(listener::onScroll)
    removeCharListener(listener::onChar)
    removeGamepadListener(listener::onGamepad)
}

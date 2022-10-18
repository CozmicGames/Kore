package com.cozmicgames.input

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.DesktopGraphics
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.Updateable
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWGamepadState
import org.lwjgl.system.MemoryStack.stackPush
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class DesktopInput : InputProcessor, Input, Updateable, Disposable {
    private val lock = ReentrantReadWriteLock()
    private val gamepadState = GLFWGamepadState.calloc()
    private val listeners = arrayListOf<InputListener>()
    private val workingGamepads = arrayListOf<Gamepad>()

    private var keys = BooleanArray(Keys.values().size) { false }
    private var keyStates = BooleanArray(Keys.values().size) { false }
    private var keysJustDown = BooleanArray(Keys.values().size) { false }
    private var keysJustUp = BooleanArray(Keys.values().size) { false }

    private var buttons = BooleanArray(MouseButtons.values().size) { false }
    private var buttonStates = BooleanArray(MouseButtons.values().size) { false }
    private var buttonsJustDown = BooleanArray(MouseButtons.values().size) { false }
    private var buttonsJustUp = BooleanArray(MouseButtons.values().size) { false }

    private var firstUpdate = true
    private var previousX = 0
    private var previousY = 0

    private var internalX = 0
        private set(value) {
            internalLastX = field
            field = value
        }

    private var internalY = 0
        private set(value) {
            internalLastY = field
            field = value
        }

    private var internalLastX = 0

    private var internalLastY = 0

    private var internalDeltaX = 0

    private var internalDeltaY = 0

    private var internalIsTouched = false

    private var internalJustTouchedDown = false

    private var internalJustTouchedUp = false

    override val isTouched get() = internalIsTouched

    override val justTouchedDown get() = internalJustTouchedDown

    override val justTouchedUp get() = internalJustTouchedUp

    override val x get() = internalX

    override val y get() = internalY

    override val deltaX get() = internalDeltaX

    override val deltaY get() = internalDeltaY

    override val lastX get() = internalLastX

    override val lastY get() = internalLastY

    override var isCursorGrabbed: Boolean
        get() = glfwGetInputMode((Kore.graphics as DesktopGraphics).window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
        set(value) {
            glfwSetInputMode((Kore.graphics as DesktopGraphics).window, GLFW_CURSOR, if (value) GLFW_CURSOR_DISABLED else GLFW_CURSOR_NORMAL)
        }

    override val gamepads = arrayListOf<Gamepad>()

    override fun addListener(listener: InputListener) = lock.write {
        listeners += listener
    }

    override fun removeListener(listener: InputListener) = lock.write {
        listeners -= listener
    }

    override fun onKey(key: Key, down: Boolean, time: Double) {
        keys[key.ordinal] = down

        listeners.forEach {
            it.onKey(key, down, time)
        }
    }

    override fun onTouch(x: Int, y: Int, button: MouseButton, pointer: Int, down: Boolean, time: Double) {
        buttons[button.ordinal] = down

        listeners.forEach {
            it.onTouch(x, y, pointer, down, time)
        }
    }

    override fun onScroll(x: Float, y: Float, time: Double) {
        listeners.forEach {
            it.onScroll(x, y, time)
        }
    }

    override fun onChar(char: Char, time: Double) {
        listeners.forEach {
            it.onChar(char, time)
        }
    }

    override fun onGamepad(id: Int, isConnected: Boolean, time: Double) {
        if (isConnected) {
            var gamepad = gamepads.find { it.id == id }

            if (gamepad == null) {
                gamepad = Gamepad(id)
                gamepads += gamepad
            }
        } else
            gamepads.removeIf { it.id == id }

        listeners.forEach {
            it.onGamepad(id, isConnected, time)
        }
    }

    override fun update(delta: Float) = lock.write {
        (Kore.graphics as DesktopGraphics).inputEventQueue.process(this)

        workingGamepads.clear()
        workingGamepads.addAll(gamepads)

        workingGamepads.forEach {
            if (!glfwJoystickPresent(it.id))
                onGamepad(it.id, false, glfwGetTime())
            else {
                glfwGetGamepadState(it.id, gamepadState)

                it.onLeftTrigger(gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER))
                it.onRightTrigger(gamepadState.axes(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER))

                val leftStickX = gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_X)
                val leftStickY = gamepadState.axes(GLFW_GAMEPAD_AXIS_LEFT_Y)

                val rightStickX = gamepadState.axes(GLFW_GAMEPAD_AXIS_RIGHT_X)
                val rightStickY = gamepadState.axes(GLFW_GAMEPAD_AXIS_RIGHT_Y)

                it.onLeftStick(leftStickX, leftStickY)
                it.onRightStick(rightStickX, rightStickY)

                for (index in (0 until GLFW_GAMEPAD_BUTTON_LAST)) {
                    val down = gamepadState.buttons(index).toInt() == GLFW_PRESS
                    it.onButton(requireNotNull(getGamepadButtonFromCode(index)), down)
                }
            }
        }

        stackPush().use {
            val pX = it.callocDouble(1)
            val pY = it.callocDouble(1)
            glfwGetCursorPos((Kore.graphics as DesktopGraphics).window, pX, pY)
            internalX = pX.get(0).toInt()
            internalY = Kore.graphics.height - pY.get(0).toInt()
        }

        repeat(keys.size) {
            keysJustDown[it] = keys[it] && !keyStates[it]
            keysJustUp[it] = !keys[it] && keyStates[it]
            keyStates[it] = keys[it]
        }

        repeat(buttons.size) {
            buttonsJustDown[it] = buttons[it] && !buttonStates[it]
            buttonsJustUp[it] = !buttons[it] && buttonStates[it]
            buttonStates[it] = buttons[it]
        }

        val previousTouchState = internalIsTouched
        internalIsTouched = buttonStates.any { it }
        if (internalJustTouchedDown)
            internalJustTouchedDown = false
        else if (!previousTouchState && internalIsTouched)
            internalJustTouchedDown = true

        internalJustTouchedUp = buttonsJustUp.any { it }

        if (!firstUpdate) {
            internalDeltaX = internalX - previousX
            internalDeltaY = internalY - previousY
        }

        previousX = internalX
        previousY = internalY

        firstUpdate = false
    }

    override fun isKeyDown(key: Key) = lock.read { keys[key.ordinal] }

    override fun isKeyJustDown(key: Key) = lock.read { keysJustDown[key.ordinal] }

    override fun isKeyJustUp(key: Key) = lock.read { keysJustUp[key.ordinal] }

    override fun isButtonDown(button: MouseButton) = lock.read { buttons[button.ordinal] }

    override fun isButtonJustDown(button: MouseButton) = lock.read { buttonsJustDown[button.ordinal] }

    override fun isButtonJustUp(button: MouseButton) = lock.read { buttonsJustUp[button.ordinal] }

    fun getKeyFromCode(c: Int) = when (c) {
        GLFW_KEY_ENTER -> Keys.KEY_ENTER
        GLFW_KEY_BACKSPACE -> Keys.KEY_BACKSPACE
        GLFW_KEY_TAB -> Keys.KEY_TAB
        GLFW_KEY_LEFT_SHIFT -> Keys.KEY_SHIFT
        GLFW_KEY_RIGHT_SHIFT -> Keys.KEY_SHIFT
        GLFW_KEY_LEFT_CONTROL -> Keys.KEY_CONTROL
        GLFW_KEY_RIGHT_CONTROL -> Keys.KEY_CONTROL
        GLFW_KEY_LEFT_ALT -> Keys.KEY_ALT
        GLFW_KEY_RIGHT_ALT -> Keys.KEY_ALT
        GLFW_KEY_PAUSE -> Keys.KEY_PAUSE
        GLFW_KEY_CAPS_LOCK -> Keys.KEY_CAPSLOCK
        GLFW_KEY_ESCAPE -> Keys.KEY_ESCAPE
        GLFW_KEY_SPACE -> Keys.KEY_SPACE
        GLFW_KEY_PAGE_UP -> Keys.KEY_PAGE_UP
        GLFW_KEY_PAGE_DOWN -> Keys.KEY_PAGE_DOWN
        GLFW_KEY_END -> Keys.KEY_END
        GLFW_KEY_HOME -> Keys.KEY_HOME
        GLFW_KEY_LEFT -> Keys.KEY_LEFT
        GLFW_KEY_UP -> Keys.KEY_UP
        GLFW_KEY_RIGHT -> Keys.KEY_RIGHT
        GLFW_KEY_DOWN -> Keys.KEY_DOWN
        GLFW_KEY_COMMA -> Keys.KEY_COMMA
        GLFW_KEY_MINUS -> Keys.KEY_MINUS
        GLFW_KEY_PERIOD -> Keys.KEY_PERIOD
        GLFW_KEY_0 -> Keys.KEY_0
        GLFW_KEY_1 -> Keys.KEY_1
        GLFW_KEY_2 -> Keys.KEY_2
        GLFW_KEY_3 -> Keys.KEY_3
        GLFW_KEY_4 -> Keys.KEY_4
        GLFW_KEY_5 -> Keys.KEY_5
        GLFW_KEY_6 -> Keys.KEY_6
        GLFW_KEY_7 -> Keys.KEY_7
        GLFW_KEY_8 -> Keys.KEY_8
        GLFW_KEY_9 -> Keys.KEY_9
        GLFW_KEY_SEMICOLON -> Keys.KEY_SEMICOLON
        GLFW_KEY_A -> Keys.KEY_A
        GLFW_KEY_B -> Keys.KEY_B
        GLFW_KEY_C -> Keys.KEY_C
        GLFW_KEY_D -> Keys.KEY_D
        GLFW_KEY_E -> Keys.KEY_E
        GLFW_KEY_F -> Keys.KEY_F
        GLFW_KEY_G -> Keys.KEY_G
        GLFW_KEY_H -> Keys.KEY_H
        GLFW_KEY_I -> Keys.KEY_I
        GLFW_KEY_J -> Keys.KEY_J
        GLFW_KEY_K -> Keys.KEY_K
        GLFW_KEY_L -> Keys.KEY_L
        GLFW_KEY_M -> Keys.KEY_M
        GLFW_KEY_N -> Keys.KEY_N
        GLFW_KEY_O -> Keys.KEY_O
        GLFW_KEY_P -> Keys.KEY_P
        GLFW_KEY_Q -> Keys.KEY_Q
        GLFW_KEY_R -> Keys.KEY_R
        GLFW_KEY_S -> Keys.KEY_S
        GLFW_KEY_T -> Keys.KEY_T
        GLFW_KEY_U -> Keys.KEY_U
        GLFW_KEY_V -> Keys.KEY_V
        GLFW_KEY_W -> Keys.KEY_W
        GLFW_KEY_X -> Keys.KEY_X
        GLFW_KEY_Y -> Keys.KEY_Y
        GLFW_KEY_Z -> Keys.KEY_Z
        GLFW_KEY_DELETE -> Keys.KEY_DELETE
        GLFW_KEY_F1 -> Keys.KEY_F1
        GLFW_KEY_F2 -> Keys.KEY_F2
        GLFW_KEY_F3 -> Keys.KEY_F3
        GLFW_KEY_F4 -> Keys.KEY_F4
        GLFW_KEY_F5 -> Keys.KEY_F5
        GLFW_KEY_F6 -> Keys.KEY_F6
        GLFW_KEY_F7 -> Keys.KEY_F7
        GLFW_KEY_F8 -> Keys.KEY_F8
        GLFW_KEY_F9 -> Keys.KEY_F9
        GLFW_KEY_F10 -> Keys.KEY_F10
        GLFW_KEY_F11 -> Keys.KEY_F11
        GLFW_KEY_F12 -> Keys.KEY_F12
        else -> null
    }

    fun getMouseButtonFromCode(c: Int) = when (c) {
        GLFW_MOUSE_BUTTON_LEFT -> MouseButtons.LEFT
        GLFW_MOUSE_BUTTON_MIDDLE -> MouseButtons.MIDDLE
        GLFW_MOUSE_BUTTON_RIGHT -> MouseButtons.RIGHT
        else -> null
    }

    private fun getGamepadButtonFromCode(c: Int) = when (c) {
        GLFW_GAMEPAD_BUTTON_A -> GamepadButtons.A
        GLFW_GAMEPAD_BUTTON_B -> GamepadButtons.B
        GLFW_GAMEPAD_BUTTON_X -> GamepadButtons.X
        GLFW_GAMEPAD_BUTTON_Y -> GamepadButtons.Y
        GLFW_GAMEPAD_BUTTON_LEFT_BUMPER -> GamepadButtons.LEFT_BUMPER
        GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER -> GamepadButtons.RIGHT_BUMPER
        GLFW_GAMEPAD_BUTTON_BACK -> GamepadButtons.BACK
        GLFW_GAMEPAD_BUTTON_START -> GamepadButtons.START
        GLFW_GAMEPAD_BUTTON_GUIDE -> GamepadButtons.GUIDE
        GLFW_GAMEPAD_BUTTON_LEFT_THUMB -> GamepadButtons.LEFT_THUMB
        GLFW_GAMEPAD_BUTTON_RIGHT_THUMB -> GamepadButtons.RIGHT_THUMB
        GLFW_GAMEPAD_BUTTON_DPAD_UP -> GamepadButtons.DPAD_UP
        GLFW_GAMEPAD_BUTTON_DPAD_RIGHT -> GamepadButtons.DPAD_RIGHT
        GLFW_GAMEPAD_BUTTON_DPAD_DOWN -> GamepadButtons.DPAD_DOWN
        GLFW_GAMEPAD_BUTTON_DPAD_LEFT -> GamepadButtons.DPAD_LEFT
        else -> null
    }

    override fun dispose() {
        gamepadState.free()
    }
}
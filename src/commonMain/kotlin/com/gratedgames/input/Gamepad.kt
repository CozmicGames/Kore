package com.gratedgames.input

import com.gratedgames.utils.concurrency.Lock
import com.gratedgames.utils.maths.Vector2

typealias GamepadButtonListener = (GamepadButton, Boolean) -> Unit
typealias GamepadTriggerListener = (Float) -> Unit
typealias GamepadStickListener = (Float, Float) -> Unit

abstract class Gamepad {
    abstract val id: String
    abstract val playerIndex: Int
    abstract val canVibrate: Boolean
    abstract val isVibrating: Boolean

    val rightStick = Vector2()
    val leftStick = Vector2()
    val rightStickLast = Vector2()
    val leftStickLast = Vector2()
    val rightStickDelta = Vector2()
    val leftStickDelta = Vector2()

    var rightTrigger = 0.0f
    var leftTrigger = 0.0f
    var rightTriggerLast = 0.0f
    var leftTriggerLast = 0.0f
    var rightTriggerDelta = 0.0f
    var leftTriggerDelta = 0.0f

    private var isFirstUpdate = true
    private val lock = Lock()
    private val buttonListeners = arrayListOf<GamepadButtonListener>()
    private val rightTriggerListeners = arrayListOf<GamepadTriggerListener>()
    private val leftTriggerListeners = arrayListOf<GamepadTriggerListener>()
    private val rightStickListeners = arrayListOf<GamepadStickListener>()
    private val leftStickListeners = arrayListOf<GamepadStickListener>()

    private val buttons = BooleanArray(GamepadButtons.count)
    private val buttonStates = BooleanArray(GamepadButtons.count)
    private val buttonsJustDown = BooleanArray(GamepadButtons.count)
    private val buttonsJustUp = BooleanArray(GamepadButtons.count)

    private val rightStickInternal = Vector2()
    private val leftStickInternal = Vector2()

    private var rightTriggerInternal = 0.0f
    private var leftTriggerInternal = 0.0f

    fun isButtonDown(button: GamepadButton) = buttons[button.ordinal]

    fun isButtonJustDown(button: GamepadButton) = buttonsJustDown[button.ordinal]

    fun isButtonJustUp(button: GamepadButton) = buttonsJustUp[button.ordinal]

    fun isRightTriggerPressed() = rightTrigger > 0.0f

    fun isLeftTriggerPressed() = leftTrigger > 0.0f

    fun addButtonListener(listener: GamepadButtonListener) = lock.write {
        buttonListeners += listener
    }

    fun addRightTriggerListener(listener: GamepadTriggerListener) = lock.write {
        rightTriggerListeners += listener
    }

    fun addLeftTriggerListener(listener: GamepadTriggerListener) = lock.write {
        leftTriggerListeners += listener
    }

    fun addRightStickListener(listener: GamepadStickListener) = lock.write {
        rightStickListeners += listener
    }

    fun addLeftStickListener(listener: GamepadStickListener) = lock.write {
        leftStickListeners += listener
    }

    fun removeButtonListener(listener: GamepadButtonListener) = lock.write {
        buttonListeners -= listener
    }

    fun removeRightTriggerListener(listener: GamepadTriggerListener) = lock.write {
        rightTriggerListeners -= listener
    }

    fun removeLeftTriggerListener(listener: GamepadTriggerListener) = lock.write {
        leftTriggerListeners -= listener
    }

    fun removeRightStickListener(listener: GamepadStickListener) = lock.write {
        rightStickListeners -= listener
    }

    fun removeLeftStickListener(listener: GamepadStickListener) = lock.write {
        leftStickListeners -= listener
    }

    fun onButton(button: GamepadButton, down: Boolean) = lock.read {
        buttons[button.ordinal] = down

        buttonListeners.forEach {
            it(button, down)
        }
    }

    fun onRightTrigger(amount: Float) = lock.read {
        rightTriggerInternal = amount

        rightTriggerListeners.forEach {
            it(amount)
        }
    }

    fun onLeftTrigger(amount: Float) = lock.read {
        leftTriggerInternal = amount

        leftTriggerListeners.forEach {
            it(amount)
        }
    }

    fun onRightStick(x: Float, y: Float) = lock.read {
        rightStickInternal.set(x, y)

        rightStickListeners.forEach {
            it(x, y)
        }
    }

    fun onLeftStick(x: Float, y: Float) = lock.read {
        leftStickInternal.set(x, y)

        leftStickListeners.forEach {
            it(x, y)
        }
    }

    fun update() {
        if (!isFirstUpdate) {
            rightTriggerDelta = rightTrigger - rightTriggerLast
            leftTriggerDelta = leftTrigger - leftTriggerLast
            rightStickDelta.set(rightStick).sub(rightStickLast)
            leftStickDelta.set(leftStick).sub(leftStickLast)
        } else
            isFirstUpdate = false

        repeat(buttons.size) {
            buttonsJustDown[it] = buttons[it] && !buttonStates[it]
            buttonsJustUp[it] = !buttons[it] && buttonStates[it]
            buttonStates[it] = buttons[it]
        }

        rightTriggerLast = rightTrigger
        leftTriggerLast = leftTrigger
        rightStickLast.set(rightStick)
        leftStickLast.set(leftStick)

        rightTrigger = rightTriggerInternal
        leftTrigger = leftTriggerInternal
        rightStick.set(rightStickInternal)
        leftStick.set(leftStickInternal)
    }

    abstract fun vibrate(duration: Double, strengthLeft: Float, strengthRight: Float)
}

fun Gamepad.vibrate(duration: Double, strength: Float) = vibrate(duration, strength, strength)

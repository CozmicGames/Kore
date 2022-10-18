package com.cozmicgames.input

import com.cozmicgames.utils.concurrency.Lock
import com.cozmicgames.utils.extensions.element
import com.cozmicgames.utils.maths.Vector2

typealias GamepadButtonListener = (GamepadButton, Boolean) -> Unit
typealias GamepadTriggerListener = (Float) -> Unit
typealias GamepadStickListener = (Float, Float) -> Unit

class Gamepad(val id: Int) {
    val sticks = Array(2) { GamepadStick() }
    val triggers = Array(2) { GamepadTrigger() }

    val leftStick by sticks.element(GamepadSticks.LEFT.ordinal)
    val rightStick by sticks.element(GamepadSticks.RIGHT.ordinal)

    val leftTrigger by triggers.element(GamepadTriggers.LEFT.ordinal)
    val rightTrigger by triggers.element(GamepadTriggers.RIGHT.ordinal)

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

    fun isLeftTriggerPressed() = leftTrigger.current > 0.0f

    fun isRightTriggerPressed() = rightTrigger.current > 0.0f

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

    fun onButton(button: GamepadButton, down: Boolean) = lock.write {
        if (buttons[button.ordinal] == down)
            return

        buttons[button.ordinal] = down

        buttonListeners.forEach {
            it(button, down)
        }
    }

    fun onRightTrigger(amount: Float) = lock.write {
        rightTriggerInternal = amount

        rightTriggerListeners.forEach {
            it(amount)
        }
    }

    fun onLeftTrigger(amount: Float) = lock.write {
        leftTriggerInternal = amount

        leftTriggerListeners.forEach {
            it(amount)
        }
    }

    fun onRightStick(x: Float, y: Float) = lock.write {
        rightStickInternal.set(x, y)

        rightStickListeners.forEach {
            it(x, y)
        }
    }

    fun onLeftStick(x: Float, y: Float) = lock.write {
        leftStickInternal.set(x, y)

        leftStickListeners.forEach {
            it(x, y)
        }
    }

    fun update() = lock.write {
        if (!isFirstUpdate) {
            leftTrigger.delta = leftTrigger.current - leftTrigger.last
            rightTrigger.delta = rightTrigger.current - rightTrigger.last
            leftStick.delta.set(leftStick.current).sub(leftStick.last)
            rightStick.delta.set(rightStick.current).sub(rightStick.last)
        } else
            isFirstUpdate = false

        repeat(buttons.size) {
            buttonsJustDown[it] = buttons[it] && !buttonStates[it]
            buttonsJustUp[it] = !buttons[it] && buttonStates[it]
            buttonStates[it] = buttons[it]
        }

        rightTrigger.last = rightTrigger.current
        leftTrigger.last = leftTrigger.current
        rightStick.last.set(rightStick.current)
        leftStick.last.set(leftStick.current)

        leftTrigger.current = leftTriggerInternal
        rightTrigger.current = rightTriggerInternal
        leftStick.current.set(leftStickInternal)
        rightStick.current.set(rightStickInternal)
    }
}

package com.cozmicgames.input

import com.cozmicgames.utils.concurrency.Lock

class InputEventQueue {
    private enum class Type(val size: Int) {
        KEY_DOWN(1),
        KEY_UP(1),
        TOUCH_DOWN(4),
        TOUCH_UP(4),
        CHAR(1),
        SCROLLED(2),
        GAMEPAD_CONNECTED(1),
        GAMEPAD_DISCONNECTED(1);

        companion object {
            val values = values()

            fun fromInt(value: Int): Type {
                return values[value]
            }
        }
    }

    private val queue = arrayListOf<Int>()
    private val workingQueue = arrayListOf<Int>()
    private val lock = Lock()

    fun clear() {
        lock.write {
            queue.clear()
        }
    }

    fun process(processor: InputProcessor) {
        lock.write {
            workingQueue.clear()
            workingQueue.addAll(queue)
            queue.clear()
        }

        var i = 0
        while (i < workingQueue.size) {
            val type = workingQueue[i++]
            val time = Double.fromBits(workingQueue[i++].toLong() shl 32 or workingQueue[i++].toLong() and 0xFFFFFFFFL)

            when (Type.fromInt(type)) {
                Type.KEY_DOWN -> processor.onKey(requireNotNull(Keys.fromInt(workingQueue[i++])), true, time)
                Type.KEY_UP -> processor.onKey(requireNotNull(Keys.fromInt(workingQueue[i++])), false, time)
                Type.TOUCH_DOWN -> processor.onTouch(workingQueue[i++], workingQueue[i++], requireNotNull(MouseButtons.fromInt(workingQueue[i++])), workingQueue[i++], true, time)
                Type.TOUCH_UP -> processor.onTouch(workingQueue[i++], workingQueue[i++], requireNotNull(MouseButtons.fromInt(workingQueue[i++])), workingQueue[i++], false, time)
                Type.SCROLLED -> processor.onScroll(Float.fromBits(workingQueue[i++]), Float.fromBits(workingQueue[i++]), time)
                Type.CHAR -> processor.onChar(workingQueue[i++].toChar(), time)
                Type.GAMEPAD_CONNECTED -> processor.onGamepad(workingQueue[i++], true, time)
                Type.GAMEPAD_DISCONNECTED -> processor.onGamepad(workingQueue[i++], false, time)
            }
        }
    }

    fun onKeyDown(key: Key, time: Double) = add(Type.KEY_DOWN, time, key.ordinal)

    fun onKeyUp(key: Key, time: Double) = add(Type.KEY_UP, time, key.ordinal)

    fun onTouchDown(x: Int, y: Int, pointer: Int, button: MouseButton, time: Double) = add(Type.TOUCH_DOWN, time, x, y, button.ordinal, pointer)

    fun onTouchUp(x: Int, y: Int, pointer: Int, button: MouseButton, time: Double) = add(Type.TOUCH_UP, time, x, y, button.ordinal, pointer)

    fun onScroll(x: Float, y: Float, time: Double) = add(Type.SCROLLED, time, x.toBits(), y.toBits())

    fun onChar(char: Char, time: Double) = add(Type.CHAR, time, char.code)

    fun onGamepadConnected(id: Int, time: Double) = add(Type.GAMEPAD_CONNECTED, time, id)

    fun onGamepadDisconnected(id: Int, time: Double) = add(Type.GAMEPAD_DISCONNECTED, time, id)

    private fun add(type: Type, time: Double, vararg values: Int) = lock.write {
        queue.add(type.ordinal)
        val timeBits = time.toBits()
        queue.add((timeBits shr 32).toInt())
        queue.add(timeBits.toInt())
        values.forEach(queue::add)
    }
}
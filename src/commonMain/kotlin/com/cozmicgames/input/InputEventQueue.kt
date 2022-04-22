package com.cozmicgames.input

import com.cozmicgames.utils.concurrency.Lock

class InputEventQueue {
    private enum class Type(val size: Int) {
        SKIP(1),
        KEY_DOWN(1),
        KEY_UP(1),
        KEY_TYPED(1),
        TOUCH_DOWN(4),
        TOUCH_UP(4),
        MOUSE_MOVED(2),
        SCROLLED(1);

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
                Type.SKIP -> i += workingQueue[i]
                Type.KEY_DOWN -> processor.onKey(requireNotNull(Keys.fromInt(workingQueue[i++])), true, time)
                Type.KEY_UP -> processor.onKey(requireNotNull(Keys.fromInt(workingQueue[i++])), false, time)
                Type.KEY_TYPED -> processor.onKeyTyped(requireNotNull(Keys.fromInt(workingQueue[i++])), time)
                Type.TOUCH_DOWN -> processor.onTouch(workingQueue[i++], workingQueue[i++], requireNotNull(MouseButtons.fromInt(workingQueue[i++])), workingQueue[i++], true, time)
                Type.TOUCH_UP -> processor.onTouch(workingQueue[i++], workingQueue[i++], requireNotNull(MouseButtons.fromInt(workingQueue[i++])), workingQueue[i++], false, time)
                Type.MOUSE_MOVED -> processor.onMouseMove(workingQueue[i++], workingQueue[i++], time)
                Type.SCROLLED -> processor.onScroll(Float.fromBits(workingQueue[i++]), time)
            }
        }
    }

    fun onKeyDown(key: Key, time: Double) = add(Type.KEY_DOWN, time, key.ordinal)

    fun onKeyUp(key: Key, time: Double) = add(Type.KEY_UP, time, key.ordinal)

    fun onKeyTyped(character: Char, time: Double) = add(Type.KEY_TYPED, time, character.code)

    fun onTouchDown(x: Int, y: Int, pointer: Int, button: Int, time: Double) = add(Type.TOUCH_DOWN, time, x, y, pointer, button)

    fun onTouchUp(x: Int, y: Int, pointer: Int, button: Int, time: Double) = add(Type.TOUCH_UP, time, x, y, pointer, button)

    fun onMouseMove(x: Int, y: Int, time: Double) {
        var i = next(Type.MOUSE_MOVED, 0)

        while (i >= 0) {
            queue[i] = Type.SKIP.ordinal
            queue[i + 3] = 2
            i = next(Type.MOUSE_MOVED, i + 5)
        }

        add(Type.MOUSE_MOVED, time, x, y)
    }

    fun onScroll(amount: Float, time: Double) = add(Type.SCROLLED, time, amount.toBits())

    private fun add(type: Type, time: Double, vararg values: Int) = lock.write {
        queue.add(type.ordinal)
        val timeBits = time.toBits()
        queue.add((timeBits shr 32).toInt())
        queue.add(timeBits.toInt())
        values.forEach(queue::add)
    }

    private fun next(nextType: Type, from: Int): Int {
        var i = from
        while (i < queue.size) {
            val type = Type.fromInt(queue[i])

            if (type == nextType)
                return i

            i += 3 // Type and 64bit time

            i += when (type) {
                Type.SKIP -> queue[i]
                else -> type.size
            }
        }
        return -1
    }
}
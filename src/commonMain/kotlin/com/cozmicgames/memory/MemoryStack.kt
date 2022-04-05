package com.cozmicgames.memory

import com.cozmicgames.Kore
import com.cozmicgames.utils.Disposable
import kotlin.math.max

object MemoryStack {
    class Frame : Disposable {
        private var memory = alloc(10.megabytes)
        private var pointer = 0

        fun alloc(size: Int): Memory {
            if (size >= memory.size - pointer) {
                val newMemory = alloc(max(pointer + size, memory.size * 3 / 2))
                memory.copyTo(newMemory)
                memory.dispose()
                memory = newMemory
            }

            val result = memory.partition(pointer, size)
            pointer += size
            return result
        }

        fun clear() {
            memory.clear(size = pointer)
            pointer = 0
        }

        override fun dispose() {
            memory.dispose()
        }
    }

    private val frames = arrayListOf<Frame>()
    private var frameIndex = 0
    private val currentFrame get() = frames[frameIndex]

    init {
        frames += Frame()

        Kore.addShutdownListener {
            frames.forEach {
                it.dispose()
            }
        }
    }

    fun push() {
        frameIndex++
        if (frameIndex + 1 >= frames.size)
            frames += Frame()
    }

    fun alloc(size: Int) = currentFrame.alloc(size)

    fun pop() {
        require(frameIndex > 0)
        currentFrame.clear()
        frameIndex--
    }
}
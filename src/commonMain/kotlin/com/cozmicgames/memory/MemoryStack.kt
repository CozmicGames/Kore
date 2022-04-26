package com.cozmicgames.memory

import com.cozmicgames.Kore
import com.cozmicgames.utils.Disposable
import kotlin.math.max

/**
 * A stack of memory blocks.
 *
 * Recommended usage is by using [Memory.Companion.scope] to create a new scope, and then calling [stackAlloc] to allocate memory.
 * This will automatically pop the stack when [Memory.Companion.scope] returns.
 */
object MemoryStack {
    class Frame : Disposable {
        private var memory = alloc(10.megabytes)
        private var pointer = 0

        fun alloc(size: Int): Memory {
            // Resize the memory if needed
            if (size >= memory.size - pointer) {
                val newMemory = Memory(max(pointer + size, memory.size * 3 / 2))
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
        frames += Frame() // create the first frame

        Kore.addShutdownListener {
            frames.forEach {
                it.dispose()
            }
        }
    }

    /**
     * Push a new frame onto the stack.
     */
    fun push() {
        frameIndex++
        if (frameIndex + 1 >= frames.size)
            frames += Frame()
    }

    /**
     * Allocates a new [Memory] instance with the specified [size].
     * The memory is a partition of the pre-allocated memory in the current frame.
     *
     * @param size The size of the memory to allocate.
     *
     * @return The new [Memory] instance.
     */
    fun alloc(size: Int) = currentFrame.alloc(size)

    /**
     * Pop the current frame off the stack.
     * This will clear the current frame.
     * If there are no more frames, this will throw an exception.
     */
    fun pop() {
        require(frameIndex > 0)
        currentFrame.clear()
        frameIndex--
    }
}

fun <R> Memory.Companion.scope(block: () -> R): R {
    MemoryStack.push()
    return try {
        block()
    } finally {
        MemoryStack.pop()
    }
}

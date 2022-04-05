package com.cozmicgames.utils.collections

import com.cozmicgames.utils.concurrency.Lock

class Stack<T> {
    private val lock = Lock()
    private var index = 0
    private val values = DynamicArray<T>()

    val isEmpty get() = lock.read { index == 0 }

    val current get() = lock.read { if (index == 0) null else values[index - 1] }

    fun push(value: T) = lock.write {
        values[index] = value
        index++
    }

    fun pop(): T = lock.write {
        index--
        requireNotNull(values[index])
    }

    fun peek(): T = lock.read {
        requireNotNull(values[index - 1])
    }

    fun forEach(descending: Boolean = false, block: (T?) -> Unit) = lock.read {
        if (descending) {
            var i = index
            while (i >= 0) {
                block(values[i])
                i--
            }
        } else {
            var i = 0
            while (i < index) {
                block(values[i])
                i++
            }
        }
    }

    fun clear() = lock.write {
        values.clear()
    }
}

fun <T> Stack<T>.drain(block: (T) -> Unit) {
    while (!isEmpty) {
        block(pop())
    }
}

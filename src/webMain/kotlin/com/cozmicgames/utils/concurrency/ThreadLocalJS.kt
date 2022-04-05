package com.cozmicgames.utils.concurrency

actual class ThreadLocalImpl<T : Any> actual constructor(supplier: () -> T) {
    private var value = supplier()

    actual fun get(): T {
        return value
    }

    actual fun set(value: T) {
        this.value = value
    }
}

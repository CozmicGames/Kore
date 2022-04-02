package com.gratedgames.utils.concurrency

import java.lang.ThreadLocal

actual class ThreadLocalImpl<T : Any> actual constructor(supplier: () -> T) {
    private val tl = ThreadLocal.withInitial(supplier)

    actual fun get(): T {
        return tl.get()
    }

    actual fun set(value: T) {
        tl.set(value)
    }
}

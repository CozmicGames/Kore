package com.cozmicgames.utils.concurrency

import kotlin.reflect.KProperty

expect class Lock() {
    inline fun <R> read(block: () -> R): R
    inline fun <R> write(block: () -> R): R
}

class SharedProperty<T : Any>(private var value: T) {
    private val lock = Lock()

    operator fun getValue(thisRef: Any, property: KProperty<*>) = lock.read { value }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) = lock.write { this.value = value }
}

inline fun <T : Any> shared(value: T) = SharedProperty(value)

package com.gratedgames.utils.concurrency

import kotlin.reflect.KProperty

fun <T : Any> threadLocal(supplier: () -> T) = ThreadLocal(supplier)

expect class ThreadLocalImpl<T : Any>(supplier: () -> T) {
    fun get(): T
    fun set(value: T)
}

class ThreadLocal<T : Any>(supplier: () -> T) {
    val impl = ThreadLocalImpl(supplier)

    fun get() = impl.get()
    fun set(value: T) = impl.set(value)

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) = set(value)
    operator fun getValue(thisRef: Any, property: KProperty<*>) = get()
}

package com.cozmicgames.utils.concurrency

actual interface Queue<T> {
    actual fun add(element: T): Boolean
    actual fun remove(): T
    actual fun poll(): T?
}

actual typealias ConcurrentHashMap<K, V> = HashMap<K, V>

actual class ConcurrentQueue<E: Any> : Queue<E> {
    private val elements = arrayListOf<E>()

    override fun add(element: E) = elements.add(element)

    override fun poll() = if (elements.isEmpty())
        null
    else
        elements.removeAt(0)

    override fun remove() = elements.removeAt(0)
}

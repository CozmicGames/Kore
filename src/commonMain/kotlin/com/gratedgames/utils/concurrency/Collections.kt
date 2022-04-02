package com.gratedgames.utils.concurrency

expect interface Queue<T> {
    fun add(element: T): Boolean

    fun remove(): T

    fun poll(): T?
}

expect class ConcurrentHashMap<K, V>() : MutableMap<K, V>

expect class ConcurrentQueue<E : Any>() : Queue<E>

fun <K, V> concurrentHashMapOf(vararg pairs: Pair<K, V>) = ConcurrentHashMap<K, V>().apply { if (pairs.isNotEmpty()) putAll(pairs) }

fun <E : Any> concurrentQueueOf(vararg elements: E) = ConcurrentQueue<E>().apply { elements.forEach { add(it) } }


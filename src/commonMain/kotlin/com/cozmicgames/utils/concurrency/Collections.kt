package com.cozmicgames.utils.concurrency

expect interface Queue<T> {
    fun add(element: T): Boolean

    fun remove(): T

    fun poll(): T?

    fun peek(): T?
}

expect class ConcurrentHashMap<K, V>() : MutableMap<K, V> {
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
    override val keys: MutableSet<K>
    override val values: MutableCollection<V>
    override val size: Int
    override fun clear()
    override fun put(key: K, value: V): V?
    override fun putAll(from: Map<out K, V>)
    override fun remove(key: K): V?
    override fun containsKey(key: K): Boolean
    override fun containsValue(value: V): Boolean
    override fun get(key: K): V?
    override fun isEmpty(): Boolean
}

expect class ConcurrentQueue<E : Any>() : Queue<E> {
    override fun add(element: E): Boolean
    override fun remove(): E
    override fun poll(): E?
    override fun peek(): E?
}

fun <K, V> concurrentHashMapOf(vararg pairs: Pair<K, V>) = ConcurrentHashMap<K, V>().apply { if (pairs.isNotEmpty()) putAll(pairs) }

fun <E : Any> concurrentQueueOf(vararg elements: E) = ConcurrentQueue<E>().apply { elements.forEach { add(it) } }


package com.cozmicgames.utils.concurrency

actual typealias Queue<T> = java.util.Queue<T>

actual class ConcurrentHashMap<K, V> : MutableMap<K, V> {
    private val map = java.util.concurrent.ConcurrentHashMap<K, V>()

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entries
    actual override val keys: MutableSet<K> get() = map.keys
    actual override val values: MutableCollection<V> get() = map.values
    actual override val size: Int get() = map.size
    actual override fun clear() = map.clear()
    actual override fun put(key: K, value: V): V? = map.put(key, value)
    actual override fun putAll(from: Map<out K, V>) = map.putAll(from)
    actual override fun remove(key: K): V? = map.remove(key)
    actual override fun containsKey(key: K): Boolean = map.containsKey(key)
    actual override fun containsValue(value: V): Boolean = map.containsValue(value)
    actual override fun get(key: K): V? = map.get(key)
    actual override fun isEmpty(): Boolean = map.isEmpty()
}

actual class ConcurrentQueue<E: Any> : Queue<E> {
    private val queue = java.util.concurrent.ConcurrentLinkedQueue<E>()
    actual override fun add(element: E): Boolean = queue.add(element)
    override fun addAll(elements: Collection<E>): Boolean = queue.addAll(elements)
    override fun clear() = queue.clear()
    override fun iterator(): MutableIterator<E> = queue.iterator()
    actual override fun remove(): E = queue.remove()
    override fun retainAll(elements: Collection<E>): Boolean = queue.retainAll(elements)
    override fun removeAll(elements: Collection<E>): Boolean = queue.removeAll(elements)
    override fun remove(element: E): Boolean = queue.remove(element)
    override fun isEmpty(): Boolean = queue.isEmpty()
    actual override fun poll(): E? = queue.poll()
    override fun element(): E = queue.element()
    actual override fun peek(): E? = queue.peek()
    override fun offer(e: E): Boolean = queue.offer(e)
    override fun containsAll(elements: Collection<E>): Boolean = queue.containsAll(elements)
    override fun contains(element: E): Boolean = queue.contains(element)
    override val size: Int get() = queue.size
}

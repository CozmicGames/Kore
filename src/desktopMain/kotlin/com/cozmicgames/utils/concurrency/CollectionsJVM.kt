package com.cozmicgames.utils.concurrency

import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

actual typealias Queue<T> = Queue<T>

actual typealias ConcurrentHashMap<K, V> = ConcurrentHashMap<K, V>

actual typealias ConcurrentQueue<E> = ConcurrentLinkedQueue<E>

package com.cozmicgames.utils.concurrency

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

actual class Lock {
    val lock = ReentrantReadWriteLock()

    actual inline fun <R> read(block: () -> R) = lock.read(block)

    actual inline fun <R> write(block: () -> R) = lock.write(block)
}

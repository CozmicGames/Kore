package com.cozmicgames.utils.concurrency

actual class Lock actual constructor() {
    actual inline fun <R> read(block: () -> R): R = block()

    actual inline fun <R> write(block: () -> R): R = block()
}
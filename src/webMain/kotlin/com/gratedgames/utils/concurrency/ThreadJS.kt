package com.gratedgames.utils.concurrency

actual object Thread {
    actual val availableThreads get() = 0

    actual fun sleep(time: Double) {
    }
}
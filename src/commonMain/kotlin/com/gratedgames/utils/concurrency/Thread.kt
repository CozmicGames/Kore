package com.gratedgames.utils.concurrency

import com.gratedgames.utils.durationOf

expect object Thread {
    val availableThreads: Int

    fun sleep(time: Double)
}

fun <R> Thread.sleepUnusedTime(time: Double, block: () -> R): R {
    var result: R? = null
    val duration = durationOf {
        result = block()
    }
    val sleepTime = time - duration
    if (sleepTime > 0.0)
        sleep(sleepTime)
    return result!!
}
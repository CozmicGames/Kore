package com.cozmicgames.utils.concurrency

import com.cozmicgames.utils.Time
import com.cozmicgames.utils.TimeUnit

actual object Thread {
    actual val availableThreads get() = Runtime.getRuntime().availableProcessors()

    actual fun sleep(time: Double) {
        var waitingTime = Time.convert(time, TimeUnit.SECONDS, TimeUnit.MILLISECONDS).toLong()
        var previousTime = System.currentTimeMillis()

        while (waitingTime > 0) {
            java.lang.Thread.sleep(1)
            val currentTime = System.currentTimeMillis()
            val waitedTime = currentTime - previousTime
            waitingTime -= waitedTime
            previousTime = currentTime
        }
    }
}

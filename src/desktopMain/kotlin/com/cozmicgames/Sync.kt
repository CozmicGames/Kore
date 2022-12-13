package com.cozmicgames

import org.lwjgl.glfw.GLFW.glfwGetTime
import kotlin.math.max


/**
 * Adapted from https://github.com/libgdx/libgdx/blob/master/backends/gdx-backend-lwjgl3/src/com/badlogic/gdx/backends/lwjgl3/Sync.java
 */
object Sync {
    private const val NANOS_IN_SECOND = 1000L * 1000L * 1000L
    private var nextFrame = 0L
    private var isInitialized = false
    private val sleepDurations = RunningAverage(10)
    private val yieldDurations = RunningAverage(10)

    fun sync(fps: Int) {
        if (fps <= 0)
            return
        if (!isInitialized)
            initialize()
        try {
            run {
                var t0 = getTime()
                var t1: Long
                while (nextFrame - t0 > sleepDurations.average()) {
                    Thread.sleep(1)
                    sleepDurations.add(getTime().also { t1 = it } - t0)
                    t0 = t1
                }
            }

            sleepDurations.dampenForLowResTicker()

            var t0 = getTime()
            var t1: Long
            while (nextFrame - t0 > yieldDurations.average()) {
                Thread.yield()
                yieldDurations.add(getTime().also { t1 = it } - t0)
                t0 = t1
            }
        } catch (_: InterruptedException) {
        }

        nextFrame = max(nextFrame + NANOS_IN_SECOND / fps, getTime())
    }

    private fun initialize() {
        isInitialized = true
        sleepDurations.init((1000 * 1000).toLong())
        yieldDurations.init((-(getTime() - getTime()) * 1.333).toInt().toLong())
        nextFrame = getTime()
        val osName = System.getProperty("os.name")
        if (osName.startsWith("Win")) {
            val timerAccuracyThread = Thread {
                try {
                    Thread.sleep(Long.MAX_VALUE)
                } catch (_: Exception) {
                }
            }
            timerAccuracyThread.name = "LWJGL3 Timer"
            timerAccuracyThread.isDaemon = true
            timerAccuracyThread.start()
        }
    }

    private fun getTime(): Long {
        return (glfwGetTime() * NANOS_IN_SECOND).toLong()
    }

    private class RunningAverage(slotCount: Int) {
        private val slots = LongArray(slotCount)
        private var offset = 0

        fun init(value: Long) {
            while (offset < slots.size) {
                slots[offset++] = value
            }
        }

        fun add(value: Long) {
            slots[offset++ % slots.size] = value
            offset %= slots.size
        }

        fun average(): Long {
            var sum = 0L
            for (i in slots.indices) {
                sum += slots[i]
            }
            return sum / slots.size
        }

        fun dampenForLowResTicker() {
            if (average() > DAMPEN_THRESHOLD) {
                for (i in slots.indices) {
                    slots[i] = (slots[i] * DAMPEN_FACTOR).toLong()
                }
            }
        }

        companion object {
            private const val DAMPEN_THRESHOLD = 10 * 1000L * 1000L
            private const val DAMPEN_FACTOR = 0.9f
        }
    }
}
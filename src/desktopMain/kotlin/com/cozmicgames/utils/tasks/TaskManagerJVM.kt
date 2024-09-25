package com.cozmicgames.utils.tasks

import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.IDGenerator
import com.cozmicgames.utils.Updateable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write

actual class TaskManager actual constructor(actual val numThreads: Int) : Disposable, Updateable {
    private sealed class ScheduledTask(val id: Int)

    private class TimeScheduledTask(id: Int, val time: Float, val isRepeating: Boolean, val isAsync: Boolean, val block: () -> Unit) : ScheduledTask(id) {
        var timer = 0.0f
    }

    private class FramesScheduledTask(id: Int, val frames: Int, val isRepeating: Boolean, val isAsync: Boolean, val block: () -> Unit) : ScheduledTask(id) {
        var counter = 0
    }

    private val tasks = ConcurrentLinkedQueue<Task>()
    private val threads = Array(numThreads) {
        thread(start = false, isDaemon = true, name = "TaskManager-Thread_$it") {
            while (isRunning) {
                if (!processTask()) {
                    synchronized(locks[it]) {
                        try {
                            isAnyWaiting = true
                            isWaiting[it] = true
                            locks[it].wait()
                        } catch (_: InterruptedException) {
                        }
                    }
                }
            }
        }
    }

    private val locks = Array(numThreads) { Any() }
    private val isWaiting = BooleanArray(numThreads)
    private var isAnyWaiting = false
    private var isRunning = true
    private val scheduledTasksLock = ReentrantReadWriteLock()
    private val scheduledTasks = arrayListOf<ScheduledTask>()
    private val workingScheduledTasks = arrayListOf<ScheduledTask>()

    actual val hasMoreTasks = tasks.isNotEmpty()

    private fun Any.wait() = (this as Object).wait()

    private fun Any.notify() = (this as Object).notify()

    init {
        threads.forEach {
            it.start()
        }
    }

    private fun wakeThreads() {
        if (isAnyWaiting) {
            for (i in locks.indices) {
                synchronized(locks[i]) {
                    if (isWaiting[i]) {
                        isWaiting[i] = false
                        locks[i].notify()
                    }
                }
            }

            isAnyWaiting = false
        }
    }

    actual fun submit(task: Task, vararg dependencies: TaskHandle): TaskHandle {
        var isFinished = false

        if (dependencies.isEmpty()) {
            tasks += {
                task()
                isFinished = true
            }
        } else {
            tasks += {
                while (true) {
                    var dependenciesFinished = true

                    for (dependency in dependencies)
                        if (!dependency()) {
                            dependenciesFinished = false
                            break
                        }

                    if (dependenciesFinished)
                        break
                }

                task()
                isFinished = true
            }
        }

        wakeThreads()

        return { isFinished }
    }

    actual fun processTask(): Boolean {
        val task = tasks.poll() ?: return false
        task()
        return true
    }

    actual fun schedule(time: Float, isRepeating: Boolean, block: () -> Unit): Int = scheduledTasksLock.write {
        val id = IDGenerator.generateUniqueID()
        scheduledTasks += TimeScheduledTask(id, time, isRepeating, false, block)
        id
    }

    actual fun schedule(frames: Int, isRepeating: Boolean, block: () -> Unit): Int = scheduledTasksLock.write {
        val id = IDGenerator.generateUniqueID()
        scheduledTasks += FramesScheduledTask(id, frames, isRepeating, false, block)
        id
    }

    actual fun scheduleAsync(time: Float, isRepeating: Boolean, block: () -> Unit): Int = scheduledTasksLock.write {
        val id = IDGenerator.generateUniqueID()
        scheduledTasks += TimeScheduledTask(id, time, isRepeating, true, block)
        id
    }

    actual fun scheduleAsync(frames: Int, isRepeating: Boolean, block: () -> Unit): Int = scheduledTasksLock.write {
        val id = IDGenerator.generateUniqueID()
        scheduledTasks += FramesScheduledTask(id, frames, isRepeating, true, block)
        id
    }

    fun cancelScheduledTask(id: Int) = scheduledTasksLock.write {
        scheduledTasks.removeIf { it.id == id }
    }

    actual override fun update(delta: Float) {
        workingScheduledTasks.clear()

        scheduledTasksLock.read {
            workingScheduledTasks.addAll(scheduledTasks)
        }

        workingScheduledTasks.forEach {
            when (it) {
                is TimeScheduledTask -> {
                    it.timer += delta
                    if (it.timer >= it.time) {
                        if (it.isAsync)
                            submit(it.block)
                        else
                            it.block()
                        if (it.isRepeating)
                            it.timer -= it.time
                        else
                            scheduledTasks -= it
                    }
                }

                is FramesScheduledTask -> {
                    it.counter++
                    if (it.counter >= it.frames) {
                        if (it.isAsync)
                            submit(it.block)
                        else
                            it.block()
                        if (it.isRepeating)
                            it.counter = 0
                        else
                            scheduledTasks -= it
                    }
                }
            }
        }
    }

    actual override fun dispose() {
        isRunning = false
        wakeThreads()

        threads.forEach {
            try {
                it.join()
            } catch (_: InterruptedException) {
            }
        }
    }
}
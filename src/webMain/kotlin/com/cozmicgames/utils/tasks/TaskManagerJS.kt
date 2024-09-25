package com.cozmicgames.utils.tasks

import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.IDGenerator
import com.cozmicgames.utils.Updateable

actual class TaskManager actual constructor(actual val numThreads: Int) : Disposable, Updateable {
    private sealed class ScheduledTask(val id: Int)

    private class TimeScheduledTask(id: Int, val time: Float, val isRepeating: Boolean, val block: () -> Unit) : ScheduledTask(id) {
        var timer = 0.0f
    }

    private class FramesScheduledTask(id: Int, val frames: Int, val isRepeating: Boolean, val block: () -> Unit) : ScheduledTask(id) {
        var counter = 0
    }

    actual val hasMoreTasks = false

    private val scheduledTasks = arrayListOf<ScheduledTask>()
    private val workingScheduledTasks = arrayListOf<ScheduledTask>()

    actual fun submit(task: Task, vararg dependencies: TaskHandle): TaskHandle {
        task()
        return { true }
    }

    actual fun processTask() = true

    actual fun schedule(time: Float, isRepeating: Boolean, block: () -> Unit): Int {
        val id = IDGenerator.generateUniqueID()
        scheduledTasks += TimeScheduledTask(id, time, isRepeating, block)
        return id
    }

    actual fun schedule(frames: Int, isRepeating: Boolean, block: () -> Unit): Int {
        val id = IDGenerator.generateUniqueID()
        scheduledTasks += FramesScheduledTask(id, frames, isRepeating, block)
        return id
    }

    actual fun scheduleAsync(time: Float, isRepeating: Boolean, block: () -> Unit): Int {
        val id = IDGenerator.generateUniqueID()
        scheduledTasks += TimeScheduledTask(id, time, isRepeating, block)
        return id
    }

    actual fun scheduleAsync(frames: Int, isRepeating: Boolean, block: () -> Unit): Int {
        val id = IDGenerator.generateUniqueID()
        scheduledTasks += FramesScheduledTask(id, frames, isRepeating, block)
        return id
    }

    actual override fun update(delta: Float) {
        workingScheduledTasks.clear()
        workingScheduledTasks.addAll(scheduledTasks)

        workingScheduledTasks.forEach {
            when (it) {
                is TimeScheduledTask -> {
                    it.timer += delta
                    if (it.timer >= it.time) {
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

    }
}

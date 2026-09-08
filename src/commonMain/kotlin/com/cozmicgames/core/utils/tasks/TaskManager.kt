package com.cozmicgames.core.utils.tasks

import com.cozmicgames.core.utils.Disposable
import com.cozmicgames.core.utils.Updateable
import com.cozmicgames.core.utils.concurrency.Thread

expect class TaskManager(numThreads: Int = Thread.availableThreads - 1) : Disposable, Updateable {
    val numThreads: Int

    val hasMoreTasks: Boolean

    fun submit(task: Task, vararg dependencies: TaskHandle): TaskHandle

    fun processTask(): Boolean

    fun schedule(time: Float, isRepeating: Boolean = false, block: () -> Unit): Int

    fun schedule(frames: Int, isRepeating: Boolean = false, block: () -> Unit): Int

    fun scheduleAsync(time: Float, isRepeating: Boolean = false, block: () -> Unit): Int

    fun scheduleAsync(frames: Int, isRepeating: Boolean = false, block: () -> Unit): Int

    override fun update(delta: Float)

    override fun dispose()
}

fun TaskManager.processTasks(untilCondition: () -> Boolean) {
    while (hasMoreTasks || untilCondition())
        processTask()
}

fun TaskManager.finishTasks() {
    while (hasMoreTasks)
        processTask()
}

typealias Task = () -> Unit

typealias TaskHandle = () -> Boolean
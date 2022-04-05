package com.cozmicgames.utils.tasks

import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.concurrency.Thread

expect class TaskManager(numThreads: Int = Thread.availableThreads - 1) : Disposable {
    val numThreads: Int

    val hasMoreTasks: Boolean

    fun submit(task: Task, vararg dependencies: TaskHandle): TaskHandle

    fun processTask(): Boolean
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
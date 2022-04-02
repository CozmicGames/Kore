package com.gratedgames.utils.tasks

import com.gratedgames.utils.Disposable

actual class TaskManager actual constructor(actual val numThreads: Int): Disposable {
    actual val hasMoreTasks = false

    actual fun submit(task: Task, vararg dependencies: TaskHandle): TaskHandle {
        task()
        return { true }
    }

    actual fun processTask() = true

    override fun dispose() {

    }
}

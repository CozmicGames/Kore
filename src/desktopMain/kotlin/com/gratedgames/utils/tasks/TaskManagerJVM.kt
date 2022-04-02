package com.gratedgames.utils.tasks

import com.gratedgames.utils.Disposable
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread

actual class TaskManager actual constructor(actual val numThreads: Int) : Disposable {
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
                        } catch (e: InterruptedException) {
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

    override fun dispose() {
        isRunning = false
        wakeThreads()

        threads.forEach {
            try {
                it.join()
            } catch (e: InterruptedException) {
            }
        }
    }
}
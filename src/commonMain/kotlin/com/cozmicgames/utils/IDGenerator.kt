package com.cozmicgames.utils

import com.cozmicgames.utils.concurrency.Lock

object IDGenerator {
    private var currentId = 0
    private val lock = Lock()

    fun generateUniqueID() = lock.write {
        currentId++
    }
}
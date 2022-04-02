package com.gratedgames.utils.events

interface EventListener<T : Any> {
    val removeAfterEvent get() = false

    fun onEvent(event: T)
}

interface EventTrigger<T : Any> : EventListener<T> {
    override val removeAfterEvent get() = true
}

fun <T : Any> listen(block: (T) -> Unit) = object : EventListener<T> {
    override fun onEvent(event: T) {
        block(event)
    }
}

fun <T : Any> trigger(block: (T) -> Unit) = object : EventTrigger<T> {
    override fun onEvent(event: T) {
        block(event)
    }
}

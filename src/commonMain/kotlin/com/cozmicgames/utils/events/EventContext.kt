package com.cozmicgames.utils.events

import com.cozmicgames.utils.concurrency.Lock
import com.cozmicgames.utils.concurrency.concurrentHashMapOf
import kotlin.reflect.KClass

class EventContext {
    private class ListenerList<T : Any> {
        private val lock = Lock()
        private val listeners = arrayListOf<EventListener<T>>()

        fun dispatch(event: T) {
            var toRemove: ArrayList<EventListener<T>>? = null
            lock.read {
                listeners.forEach {
                    it.onEvent(event)

                    if (it.removeAfterEvent) {
                        if (toRemove == null)
                            toRemove = arrayListOf()

                        requireNotNull(toRemove) += it
                    }
                }
            }

            toRemove?.forEach {
                removeListener(it)
            }
        }

        fun addListener(listener: EventListener<T>) = lock.write {
            listeners += listener
        }

        fun removeListener(listener: EventListener<T>) = lock.write {
            listeners -= listener
        }
    }

    private val listenerLists = concurrentHashMapOf<KClass<*>, ListenerList<*>>()

    inline fun <reified T : Any> dispatch(event: T) = dispatch({ event }, T::class)

    inline fun <reified T : Any> dispatch(noinline event: () -> T) = dispatch(event, T::class)

    fun <T : Any> dispatch(event: () -> T, type: KClass<T>) {
        val list = listenerLists[type] ?: return
        (list as? ListenerList<T>)?.dispatch(event())
    }

    inline fun <reified T : Any> addListener(listener: EventListener<T>) = addListener(listener, T::class)

    fun <T : Any> addListener(listener: EventListener<T>, type: KClass<T>) {
        val list = listenerLists.getOrPut(type) { ListenerList<T>() }
        (list as? ListenerList<T>)?.addListener(listener)
    }

    inline fun <reified T : Any> removeListener(listener: EventListener<T>) = removeListener(listener, T::class)

    fun <T : Any> removeListener(listener: EventListener<T>, type: KClass<T>) {
        val list = listenerLists[type] ?: return
        (list as? ListenerList<T>)?.removeListener(listener)
    }
}



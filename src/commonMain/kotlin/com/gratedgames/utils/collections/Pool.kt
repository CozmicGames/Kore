package com.gratedgames.utils.collections

import com.gratedgames.utils.Disposable
import com.gratedgames.utils.Reflection
import kotlin.reflect.KClass

class Pool<T : Any>(size: Int = 10, private val supplier: () -> T, private val reset: (T) -> Unit = { if (it is Resettable) it.reset() }) : Disposable {
    constructor(type: KClass<T>, size: Int = 10, reset: (T) -> Unit = { if (it is Resettable) it.reset() }) : this(size, requireNotNull(Reflection.getSupplier(type)), reset)

    private var elements = ArrayList<T>(size)

    fun obtain(): T {
        return if (elements.isNotEmpty())
            elements.removeAt(0)
        else
            supplier()
    }

    fun free(element: T) {
        reset(element)
        elements.add(element)
    }

    override fun dispose() {
        elements.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}

fun <T: Any> Pool<T>.reclaiming() = ReclaimingPool(this)

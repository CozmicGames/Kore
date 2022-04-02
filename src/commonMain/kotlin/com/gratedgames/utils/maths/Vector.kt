package com.gratedgames.utils.maths

import kotlin.reflect.KProperty


abstract class Vector<T, V : Vector<T, V>> {
    protected class ComponentAccessor(val index: Int) {
        inline operator fun <T, V : Vector<T, V>> getValue(thisRef: V, property: KProperty<*>) = thisRef.data[index]

        inline operator fun <T, V : Vector<T, V>> setValue(thisRef: V, property: KProperty<*>, value: T) {
            thisRef.data[index] = value
        }
    }

    companion object {
        private val xComponentAccessor = ComponentAccessor(0)
        private val yComponentAccessor = ComponentAccessor(1)
        private val zComponentAccessor = ComponentAccessor(2)
        private val wComponentAccessor = ComponentAccessor(3)
    }

    protected fun xComponent() = xComponentAccessor
    protected fun yComponent() = yComponentAccessor
    protected fun zComponent() = zComponentAccessor
    protected fun wComponent() = wComponentAccessor

    abstract val data: Array<T>

    val size get() = data.size

    abstract fun set(vector: V): V

    abstract fun set(scalar: T): V

    abstract fun setZero(): V

    abstract fun add(vector: V): V

    abstract fun sub(vector: V): V

    abstract fun mul(vector: V): V

    abstract fun div(vector: V): V

    abstract fun add(scalar: T): V

    abstract fun sub(scalar: T): V

    abstract fun mul(scalar: T): V

    abstract fun div(scalar: T): V

    abstract fun lerp(vector: V, alpha: Float): V

    abstract fun negate(): V

    abstract fun copy(): V

    override fun hashCode() = data.contentHashCode()

    override fun toString() = data.joinToString(", ", "[", "]") { it.toString() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Vector<*, *>

        if (size != other.size) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    inline operator fun component1() = data.component1()

    inline operator fun component2() = data.component2()

    inline operator fun component3() = data.component3()

    inline operator fun component4() = data.component4()
}


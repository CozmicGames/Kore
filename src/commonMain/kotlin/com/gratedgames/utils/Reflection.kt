package com.gratedgames.utils

import kotlin.reflect.KClass

expect object Reflection {
    fun <T : Any> getClassName(cls: KClass<T>): String

    fun getClassByName(name: String): KClass<*>?

    fun <T : Any> createInstance(cls: KClass<T>): T?

    fun <T : Any> getSupplier(cls: KClass<T>): (() -> T)?
}

inline fun <reified T : Any> Reflection.getClassName() = getClassName(T::class)

inline fun <reified T : Any> Reflection.createInstance() = createInstance(T::class)

inline fun <reified T : Any> Reflection.getSupplier() = getSupplier(T::class)

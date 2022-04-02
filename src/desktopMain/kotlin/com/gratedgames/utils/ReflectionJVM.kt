package com.gratedgames.utils

import kotlin.reflect.KClass

actual object Reflection {
    actual fun <T : Any> getClassName(cls: KClass<T>) = cls.java.name

    actual fun getClassByName(name: String): KClass<*>? {
        return try {
            Class.forName(name).kotlin
        } catch (e: Exception) {
            null
        }
    }

    actual fun <T : Any> createInstance(cls: KClass<T>): T? {
        return cls.constructors.find { it.parameters.isEmpty() }?.call()
    }

    actual fun <T : Any> getSupplier(cls: KClass<T>): (() -> T)? {
        val ctor = cls.constructors.find { it.parameters.isEmpty() } ?: return null
        return { ctor.call() }
    }
}
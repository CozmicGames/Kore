package com.gratedgames.utils

import kotlin.reflect.KClass

actual object Reflection {
    actual fun <T : Any> getClassName(cls: KClass<T>) = cls.js.name

    actual fun getClassByName(name: String): KClass<*>? {
        return (name.asDynamic() as? JsClass<*>)?.kotlin
    }

    actual fun <T : Any> createInstance(cls: KClass<T>): T? {
        val ctor = cls.js.asDynamic()
        return js("new ctor()") as T
    }

    actual fun <T : Any> getSupplier(cls: KClass<T>): (() -> T)? {
        val ctor = cls.js.asDynamic()
        return { js("new ctor()") as T }
    }
}
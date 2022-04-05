package com.cozmicgames.log

import kotlin.reflect.KClass

interface Log {
    fun debug(caller: KClass<*>, message: String)
    fun info(caller: KClass<*>, message: String)
    fun error(caller: KClass<*>, message: String)
    fun fail(caller: KClass<*>, message: String)
}
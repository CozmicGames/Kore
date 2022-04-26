package com.cozmicgames.log

import kotlin.reflect.KClass

interface Log {
    /**
     * Logs a message at the debug level.
     *
     * @param caller The class that called the log method.
     * @param message the message to log
     */
    fun debug(caller: KClass<*>, message: String)

    /**
     * Logs a message at the info level.
     *
     * @param caller The class that called the log method.
     * @param message the message to log
     */
    fun info(caller: KClass<*>, message: String)

    /**
     * Logs a message at the error level.
     *
     * @param caller The class that called the log method.
     * @param message the message to log
     */
    fun error(caller: KClass<*>, message: String)

    /**
     * Logs a message at the fail level.
     * This will crash the application.
     *
     * @param caller The class that called the log method.
     * @param message the message to log
     */
    fun fail(caller: KClass<*>, message: String)
}
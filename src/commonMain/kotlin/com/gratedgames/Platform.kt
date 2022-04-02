package com.gratedgames

import com.gratedgames.utils.Disposable

typealias FrameListener = (Float) -> Unit
typealias DropListener = (Array<String>) -> Unit
typealias ShutdownListener = () -> Unit
typealias ResizeListener = (Int, Int) -> Unit

interface Platform : Disposable {
    val name: String

    var clipboard: String?

    fun addFrameListener(listener: FrameListener)
    fun removeFrameListener(listener: FrameListener)
    fun addDropListener(listener: DropListener)
    fun removeDropListener(listener: DropListener)
    fun addShutdownListener(listener: ShutdownListener)
    fun removeShutdownListener(listener: ShutdownListener)
    fun addResizeListener(listener: ResizeListener)
    fun removeResizeListener(listener: ResizeListener)
    fun executeNextFrame(block: () -> Unit)
    fun run(application: Application)
}

inline fun <R> runForPlatform(platform: String, block: () -> R) = if (Kore.platformName.equals(platform, true))
    block()
else
    null
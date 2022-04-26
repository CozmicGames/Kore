package com.cozmicgames

import com.cozmicgames.utils.Disposable

/**
 * Describes a listener that is called every frame. It receives the frames' delta time.
 */
typealias FrameListener = (Float) -> Unit

/**
 * Describes a listener that is called when something is dropped in the applications' window. Receives the dropped elements as an array of their file names.
 */
typealias DropListener = (Array<String>) -> Unit

/**
 * Describes a listener that is called when the application is shut down.
 */
typealias ShutdownListener = () -> Unit

/**
 * Describes a listener that is called when the application is resized. Receives the new width and height of the application's window.
 */
typealias ResizeListener = (Int, Int) -> Unit

/**
 * The platform interface.
 * This is the interface that the platform implementation must implement.
 * It is used internally by the framework to call the platform's functions.
 */
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

/**
 * Only run when the current platform name matches the given platform name.
 *
 * @param platform The platform name to check against.
 * @param block The block to run if the platform matches.
 *
 * @return The result of the block.
 */
inline fun <R> runForPlatform(platform: String, block: () -> R) = if (Kore.platformName.equals(platform, true))
    block()
else
    null
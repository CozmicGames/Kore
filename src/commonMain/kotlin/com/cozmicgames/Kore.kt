package com.cozmicgames

import com.cozmicgames.audio.Audio
import com.cozmicgames.files.Files
import com.cozmicgames.graphics.Graphics
import com.cozmicgames.input.Input
import com.cozmicgames.log.Dialogs
import com.cozmicgames.log.Log
import com.cozmicgames.memory.MemoryAccess
import com.cozmicgames.utils.Context

/**
 * The main class for the Kore framework.
 */
object Kore {
    /**
     * The name of the current platform.
     */
    val platformName get() = platform?.name ?: "None"

    /**
     * The applications' context. All framework modules are stored in this context.
     * This is the only object that should be used to access the framework modules.
     * A module can be accessed by calling [Context.inject] with the module's type or by creating an injector for it.
     */
    val context = Context()

    private var platform: Platform? = null

    /**
     * Initializes the Kore framework and starts the application.
     *
     * @param application The application to run.
     * @param configuration The configuration to use.
     * @param platformSupplier The platform to use.
     */
    fun start(application: Application, configuration: Configuration, platformSupplier: () -> Platform) {
        require(platform == null)

        context.bind(application)
        context.bind(configuration)

        platform = platformSupplier()
        platform?.run(application)
    }

    /**
     * Stops the Kore framework and closes the application.
     */
    fun stop() {
        if (platform == null)
            return

        platform?.dispose()
        platform = null
    }

    /**
     * Executes the given [block] the next frame.
     * This is useful for executing code that needs to be executed on the next frame or needs to be run on the main thread.
     *
     * @param block The block to execute.
     */
    fun onNextFrame(block: () -> Unit) = platform?.executeNextFrame(block)

    /**
     * Adds a frame listener to the framework.
     * @see [FrameListener]
     */
    fun addFrameListener(listener: FrameListener) = platform?.addFrameListener(listener)

    /**
     * Removes a frame listener fom the framework.
     * @see [FrameListener]
     */
    fun removeFrameListener(listener: FrameListener) = platform?.removeFrameListener(listener)

    /**
     * Adds a shutdown listener to the framework.
     * @see [ShutdownListener]
     */
    fun addShutdownListener(listener: ShutdownListener) = platform?.addShutdownListener(listener)

    /**
     * Removes a shutdown listener from the framework.
     * @see [ShutdownListener]
     */
    fun removeShutdownListener(listener: ShutdownListener) = platform?.removeShutdownListener(listener)

    /**
     * Adds a drop listener to the framework.
     * @see [DropListener]
     */
    fun addDropListener(listener: DropListener) = platform?.addDropListener(listener)

    /**
     * Removes a drop listener from the framework.
     * @see [DropListener]
     */
    fun removeDropListener(listener: DropListener) = platform?.removeDropListener(listener)

    /**
     * Adds a resize listener to the framework.
     * @see [ResizeListener]
     */
    fun addResizeListener(listener: ResizeListener) = platform?.addResizeListener(listener)

    /**
     * Removes a resize listener from the framework.
     * @see [ResizeListener]
     */
    fun removeResizeListener(listener: ResizeListener) = platform?.removeResizeListener(listener)
}

/**
 * The configuration for the application.
 * @see [Configuration]
 */
val Kore.configuration by Kore.context.injector<Configuration>()

/**
 * The application.
 * @see [Application]
 */
val Kore.application by Kore.context.injector<Application>()

/**
 * The log module.
 * @see [Log]
 */
val Kore.log by Kore.context.injector<Log>()

/**
 * The dialog module.
 * @see [Dialogs]
 */
val Kore.dialogs by Kore.context.injector<Dialogs>()

/**
 * The memory access module.
 * @see [MemoryAccess]
 */
val Kore.memoryAccess by Kore.context.injector<MemoryAccess>()

/**
 * The files module.
 * @see [Files]
 */
val Kore.files by Kore.context.injector<Files>()

/**
 * The input module.
 * @see [Input]
 */
val Kore.input by Kore.context.injector<Input>()

/**
 * The graphics module.
 * @see [Graphics]
 */
val Kore.graphics by Kore.context.injector<Graphics>()

/**
 * The audio module.
 * @see [Audio]
 */
val Kore.audio by Kore.context.injector<Audio>()

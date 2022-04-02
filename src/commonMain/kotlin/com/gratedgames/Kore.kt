package com.gratedgames

import com.gratedgames.audio.Audio
import com.gratedgames.files.Files
import com.gratedgames.graphics.Graphics
import com.gratedgames.input.Input
import com.gratedgames.log.Dialogs
import com.gratedgames.log.Log
import com.gratedgames.memory.MemoryAccess
import com.gratedgames.utils.Context


object Kore {
    val platformName get() = platform?.name ?: "None"
    val context = Context()

    private var platform: Platform? = null

    fun start(application: Application, configuration: Configuration, platformSupplier: () -> Platform) {
        require(platform == null)

        context.bind(application)
        context.bind(configuration)

        platform = platformSupplier()
        platform?.run(application)
    }

    fun stop() {
        if (platform == null)
            return

        platform?.dispose()
        platform = null
    }

    fun onNextFrame(block: () -> Unit) = platform?.executeNextFrame(block)

    fun addFrameListener(listener: FrameListener) = platform?.addFrameListener(listener)

    fun removeFrameListener(listener: FrameListener) = platform?.removeFrameListener(listener)

    fun addShutdownListener(listener: ShutdownListener) = platform?.addShutdownListener(listener)

    fun removeShutdownListener(listener: ShutdownListener) = platform?.removeShutdownListener(listener)

    fun addDropListener(listener: DropListener) = platform?.addDropListener(listener)

    fun removeDropListener(listener: DropListener) = platform?.removeDropListener(listener)

    fun addResizeListener(listener: ResizeListener) = platform?.addResizeListener(listener)

    fun removeResizeListener(listener: ResizeListener) = platform?.removeResizeListener(listener)
}

val Kore.configuration by Kore.context.injector<Configuration>()
val Kore.application by Kore.context.injector<Application>()
val Kore.log by Kore.context.injector<Log>()
val Kore.dialogs by Kore.context.injector<Dialogs>()
val Kore.memoryAccess by Kore.context.injector<MemoryAccess>()
val Kore.files by Kore.context.injector<Files>()
val Kore.input by Kore.context.injector<Input>()
val Kore.graphics by Kore.context.injector<Graphics>()
val Kore.audio by Kore.context.injector<Audio>()

package com.cozmicgames

import com.cozmicgames.audio.Audio
import com.cozmicgames.audio.DesktopAudio
import com.cozmicgames.files.DesktopFiles
import com.cozmicgames.files.Files
import com.cozmicgames.graphics.DesktopGraphics
import com.cozmicgames.graphics.Graphics
import com.cozmicgames.input.DesktopInput
import com.cozmicgames.input.Input
import com.cozmicgames.log.DesktopDialogs
import com.cozmicgames.log.DesktopLog
import com.cozmicgames.log.Dialogs
import com.cozmicgames.log.Log
import com.cozmicgames.memory.DesktopMemoryAccess
import com.cozmicgames.memory.MemoryAccess
import com.cozmicgames.memory.megabytes
import com.cozmicgames.utils.*
import org.lwjgl.glfw.GLFW.*
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

class DesktopPlatform : Platform {
    override val name = "Desktop"

    override var clipboard: String?
        set(value) {
            glfwSetClipboardString((Kore.graphics as DesktopGraphics).window, value ?: "")
        }
        get() = glfwGetClipboardString((Kore.graphics as DesktopGraphics).window)

    private val shutdownListeners = arrayListOf<ShutdownListener>()
    private val frameListeners = arrayListOf<FrameListener>()
    private val nextFrameActions = arrayListOf<() -> Unit>()

    private var running = false

    init {
        Kore.context.bind(DesktopLog() as Log)
        Kore.context.bind(DesktopDialogs() as Dialogs)
        Kore.context.bind(DesktopMemoryAccess() as MemoryAccess)
        Kore.context.bind(DesktopFiles() as Files)
        Kore.context.bind(DesktopInput() as Input)
        Kore.context.bind(DesktopGraphics() as Graphics)
        Kore.context.bind(DesktopAudio() as Audio)
    }

    override fun addFrameListener(listener: FrameListener) {
        frameListeners += listener
    }

    override fun removeFrameListener(listener: FrameListener) {
        frameListeners -= listener
    }

    override fun addShutdownListener(listener: ShutdownListener) {
        shutdownListeners += listener
    }

    override fun removeShutdownListener(listener: ShutdownListener) {
        shutdownListeners -= listener
    }

    override fun addDropListener(listener: DropListener) {
        (Kore.graphics as DesktopGraphics).dropListeners += listener
    }

    override fun removeDropListener(listener: DropListener) {
        (Kore.graphics as DesktopGraphics).dropListeners -= listener
    }

    override fun addResizeListener(listener: ResizeListener) {
        (Kore.graphics as DesktopGraphics).resizeListeners += listener
    }

    override fun removeResizeListener(listener: ResizeListener) {
        (Kore.graphics as DesktopGraphics).resizeListeners -= listener
    }

    override fun executeNextFrame(block: () -> Unit) {
        nextFrameActions += block
    }

    override fun run(application: Application) {
        try {
            Kore.log.info(this::class, "Initializing application")

            val g = Kore.graphics as DesktopGraphics

            g.initialize()

            application.onCreate()

            var isRunning = true
            val processingNextFrameActions = arrayListOf<() -> Unit>()
            var previousFrameTime = Time.current

            while (isRunning) {
                glfwPollEvents()

                if (glfwWindowShouldClose(g.window)) {
                    isRunning = false
                    glfwHideWindow(g.window)
                    Kore.stop()
                }

                processingNextFrameActions.addAll(nextFrameActions)
                nextFrameActions.clear()
                processingNextFrameActions.forEach { it() }
                processingNextFrameActions.clear()

                val currentFrameTime = Time.current
                val deltaTime = currentFrameTime - previousFrameTime
                previousFrameTime = currentFrameTime

                g.beginFrame(deltaTime.toFloat())

                val averageDeltaTime = g.statistics.averageFrameTime

                Kore.context.forEach {
                    if (it is Updateable)
                        it.update(averageDeltaTime)
                }

                frameListeners.forEach {
                    it(averageDeltaTime)
                }

                application.onFrame(averageDeltaTime)

                g.endFrame()
                g.internalFrameIndex++
                glfwSwapBuffers(g.window)

                if (Kore.configuration.framerate > 0)
                    Sync.sync(Kore.configuration.framerate)
            }

            Kore.log.info(this::class, "Closing application")

            application.onDispose()

            Kore.log.info(this::class, "Running shutdown listeners")

            shutdownListeners.forEach { it() }

            Kore.context.dispose()
            g.disposeWindow()
        } catch (e: Exception) {
            val bytes = buildByteArray {
                val stream = object : OutputStream() {
                    override fun write(b: Int) {
                        append(b.toByte())
                    }
                }
                e.printStackTrace(PrintStream(stream))
            }
            Kore.log.fail(this::class, bytes.decodeToString())
        }
    }

    override fun dispose() {
        running = false
    }
}

var Configuration.icons by stringArray { emptyArray() }

var Configuration.audioStreamThreshold by int { 10.megabytes }

fun Configuration.readFromFile(file: String): Boolean {
    val f = File(file)
    return if (f.exists()) {
        read(f.readText())
        true
    } else
        false
}

fun Configuration.writeToFile(file: String) {
    val f = File(file)
    if (f.exists())
        f.delete()
    f.createNewFile()
    f.writeText(write())
}

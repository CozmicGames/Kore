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
import com.cozmicgames.utils.*
import org.lwjgl.glfw.GLFW.*
import java.io.File
import java.io.FileOutputStream
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

            var previousTime = Time.current
            val processingNextFrameActions = arrayListOf<() -> Unit>()

            running = true

            var frameCounter = 0.0
            var previousFrameTime = Time.current

            fun frame() = durationOf {
                processingNextFrameActions.addAll(nextFrameActions)
                nextFrameActions.clear()
                processingNextFrameActions.forEach { it() }
                processingNextFrameActions.clear()

                val currentFrameTime = Time.current
                val deltaTime = currentFrameTime - previousFrameTime
                previousFrameTime = currentFrameTime

                Kore.context.forEach {
                    if (it is Updateable)
                        it.update(deltaTime.toFloat())
                }

                frameListeners.forEach {
                    it(deltaTime.toFloat())
                }

                g.beginFrame(deltaTime.toFloat())
                application.onFrame(deltaTime.toFloat())
                g.endFrame()
                g.internalFrameIndex++
                glfwSwapBuffers(g.window)
            }

            while (running) {
                glfwPollEvents()

                if (glfwWindowShouldClose(g.window)) {
                    glfwHideWindow(g.window)
                    Kore.stop()
                }

                val currentTime = Time.current
                frameCounter += currentTime - previousTime
                previousTime = currentTime

                if (Kore.configuration.framerate <= 0)
                    frameCounter -= frame()
                else {
                    val frameTime = 1.0 / Kore.configuration.framerate

                    if (frameCounter >= frameTime)
                        while (frameCounter >= frameTime) {
                            frame()
                            frameCounter -= frameTime
                        }
                    else
                        Thread.sleep(1)
                }
            }

            Kore.log.info(this::class, "Closing application")

            g.closeWindow()
            application.onDispose()

            Kore.log.info(this::class, "Running shutdown listeners")

            shutdownListeners.forEach { it() }

            Kore.context.dispose()
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

fun Configuration.readFromFile(file: String) {
    val f = File(file)
    if (f.exists())
        read(f.readText())
}

fun Configuration.writeToFile(file: String) {
    val f = File(file)
    if (f.exists())
        f.delete()
    f.createNewFile()
    f.writeText(write())
}

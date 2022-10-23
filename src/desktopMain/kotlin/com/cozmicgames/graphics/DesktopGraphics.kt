package com.cozmicgames.graphics

import com.cozmicgames.*
import com.cozmicgames.files.*
import com.cozmicgames.graphics.gpu.*
import com.cozmicgames.graphics.opengl.gl32.GL32GraphicsImpl
import com.cozmicgames.graphics.opengl.gl43.GL43GraphicsImpl
import com.cozmicgames.input.DesktopInput
import com.cozmicgames.input.InputEventQueue
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.use
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWDropCallback
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.opengl.GL.createCapabilities
import org.lwjgl.opengl.GL20C.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memAlloc
import org.lwjgl.system.MemoryUtil.memFree
import org.lwjgl.system.Platform
import java.awt.image.BufferedImage
import java.io.OutputStream
import java.lang.Exception
import java.nio.ByteBuffer
import javax.imageio.ImageIO

class DesktopGraphics : Graphics, Disposable {
    private val errorCallback: GLFWErrorCallback

    internal val inputEventQueue = InputEventQueue()

    internal var window = 0L

    internal val dropListeners = arrayListOf<DropListener>()

    internal val resizeListeners = arrayListOf<ResizeListener>()

    private lateinit var impl: GraphicsImpl

    override val width get() = internalWidth

    override val height get() = internalHeight

    override val isFocused get() = internalIsFocused

    override val frameIndex get() = internalFrameIndex

    override val statistics get() = DesktopStatistics as Statistics

    override val defaultFont: Font = DesktopFont(java.awt.Font("Arial", java.awt.Font.PLAIN, 14))

    override val uniformBufferLayout get() = impl.uniformBufferLayout

    override val supportedImageFormats = ImageIO.getReaderFormatNames().asIterable()

    override val supportedFontFormats = arrayOf("ttf").asIterable()

    private var internalWidth: Int
        get() = Kore.configuration.width
        private set(value) {
            Kore.configuration.width = value
        }

    private var internalHeight: Int
        get() = Kore.configuration.height
        private set(value) {
            Kore.configuration.height = value
        }

    override var isVSync: Boolean
        get() = Kore.configuration.vsync
        set(value) {
            glfwSwapInterval(if (value) 1 else 0)
            Kore.configuration.vsync = value
        }

    override val clientScale: Float
        get() {
            stackPush().use {
                val pX = it.callocFloat(1)
                val pY = it.callocFloat(1)
                glfwGetWindowContentScale(window, pX, pY)
                return pX.get(0)
            }
        }

    override val safeInsetLeft = 0
    override val safeInsetRight = 0
    override val safeInsetTop = 0
    override val safeInsetBottom = 0

    override var title: String
        get() = Kore.configuration.title
        set(value) {
            glfwSetWindowTitle(window, value)
            Kore.configuration.title = value
        }

    override val isResizable = true

    private var internalIsFocused = true

    internal var internalFrameIndex = 0

    override val supportsCompute get() = impl.supportsCompute

    init {
        errorCallback = object : GLFWErrorCallback() {
            override fun invoke(error: Int, description: Long) {
                Kore.log.fail(this::class, String.format("GLFW error [0x%X]: %s", error, getDescription(description)))
            }
        }
        glfwSetErrorCallback(errorCallback)

        if (!glfwInit())
            Kore.log.fail(this::class, "Failed to initialize GLFW")

        createWindow()
    }

    fun initialize() {
        impl.initialize()
    }

    fun beginFrame(delta: Float) {
        DesktopStatistics.newFrame(delta)
        impl.beginFrame()
    }

    fun endFrame() {
        impl.endFrame()
    }

    override fun readImage(file: FileHandle): Image? {
        if (file.extension.lowercase() !in supportedImageFormats) {
            Kore.log.error(this::class, "Unsupported image format: $file.extension")
            return null
        }

        val bufferedImage = file.read().use {
            try {
                ImageIO.read((it as DesktopReadStream).stream)
            } catch (e: Exception) {
                Kore.log.error(this::class, "Unable to read image data")
                return null
            }
        }

        val image = Image(bufferedImage.width, bufferedImage.height)

        repeat(bufferedImage.height) { y ->
            repeat(bufferedImage.width) { x ->
                val color = bufferedImage.getRGB(x, (bufferedImage.height - 1) - y)

                val a = ((color ushr 24) and 0xFF).toFloat() / 0xFF
                val r = ((color ushr 16) and 0xFF).toFloat() / 0xFF
                val g = ((color ushr 8) and 0xFF).toFloat() / 0xFF
                val b = (color and 0xFF).toFloat() / 0xFF

                image.pixels.data[image.getPixelsIndex(x, y)].set(r, g, b, a)
            }
        }

        return image
    }

    override fun writeImage(file: FileHandle, image: Image) {
        val format = file.extension.lowercase()

        if (format !in supportedImageFormats) {
            Kore.log.error(this::class, "Unsupported image format: ${file.extension}")
            return
        }

        val bufferedImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)

        repeat(image.height) { y ->
            repeat(image.width) { x ->
                val color = image[x, y]
                val r = (color.r * 0xFF).toInt() and 0xFF
                val g = (color.g * 0xFF).toInt() and 0xFF
                val b = (color.b * 0xFF).toInt() and 0xFF
                val a = (color.a * 0xFF).toInt() and 0xFF

                bufferedImage.setRGB(x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
            }
        }

        val stream = file.write(false)

        val wrappedOutputStream = object : OutputStream() {
            override fun write(b: Int) {
                stream.writeByte((b and 0xFF).toByte())
            }
        }

        try {
            ImageIO.write(bufferedImage, format, wrappedOutputStream)
        } finally {
            wrappedOutputStream.close()
            stream.dispose()
        }
    }

    override fun readFont(file: FileHandle): Font? {
        if (file.extension.lowercase() !in supportedFontFormats) {
            Kore.log.error(this::class, "Unsupported font format: ${file.extension}")
            return null
        }

        val awtFont = file.read().use {
            java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, (it as DesktopReadStream).stream)
        }

        if (awtFont == null) {
            Kore.log.error(this::class, "Unable to read font data")
            return null
        }

        return DesktopFont(awtFont)
    }

    override fun createPipeline(type: Pipeline.Type, block: Pipeline.() -> Unit) = impl.createPipeline(type, block)

    override fun createBuffer(usage: GraphicsBuffer.Usage, block: GraphicsBuffer.() -> Unit) = impl.createBuffer(usage, block)

    override fun createTexture2D(format: Texture.Format, sampler: Sampler, block: Texture2D.() -> Unit) = impl.createTexture2D(format, sampler, block)

    override fun createTextureCube(format: Texture.Format, sampler: Sampler, block: TextureCube.() -> Unit) = impl.createTextureCube(format, sampler, block)

    override fun createTexture3D(format: Texture.Format, sampler: Sampler, block: Texture3D.() -> Unit) = impl.createTexture3D(format, sampler, block)

    override fun createFramebuffer(block: Framebuffer.() -> Unit) = impl.createFramebuffer(block)

    override fun createSampler(block: Sampler.() -> Unit) = impl.createSampler(block)

    override fun setPipeline(pipeline: Pipeline?) = impl.setPipeline(pipeline)

    override fun setFramebuffer(framebuffer: Framebuffer?) = impl.setFramebuffer(framebuffer)

    override fun setScissor(rect: ScissorRect?) = impl.setScissor(rect)

    override fun setViewport(viewport: Viewport?) = impl.setViewport(viewport)

    override fun setVertexBuffer(buffer: GraphicsBuffer?, indices: IntArray) = impl.setVertexBuffer(buffer, indices)

    override fun setIndexBuffer(buffer: GraphicsBuffer?) = impl.setIndexBuffer(buffer)

    override fun clear(color: Color?, depth: Float?, stencil: Int?) = impl.clear(color, depth, stencil)

    override fun draw(primitive: Primitive, length: Int, offset: Int, numInstances: Int) = impl.draw(primitive, length, offset, numInstances)

    override fun drawIndexed(primitive: Primitive, length: Int, offset: Int, type: IndexDataType, numInstances: Int) = impl.drawIndexed(primitive, length, offset, type, numInstances)

    override fun dispatchCompute(groupsX: Int, groupsY: Int, groupsZ: Int) = impl.dispatchCompute(groupsX, groupsY, groupsZ)

    override fun drawIndirect(primitive: Primitive, buffer: GraphicsBuffer, count: Int, stride: Int) = impl.drawIndirect(primitive, buffer, count, stride)

    override fun drawIndexedIndirect(primitive: Primitive, buffer: GraphicsBuffer, type: IndexDataType, count: Int, stride: Int) = impl.drawIndexedIndirect(primitive, buffer, type, count, stride)

    override fun dispatchComputeIndirect(buffer: GraphicsBuffer) = impl.dispatchComputeIndirect(buffer)

    private fun createWindow() {
        val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
        if (Platform.get() == Platform.MACOSX) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE)
        }

        vidMode?.let {
            glfwWindowHint(GLFW_RED_BITS, it.redBits())
            glfwWindowHint(GLFW_GREEN_BITS, it.greenBits())
            glfwWindowHint(GLFW_BLUE_BITS, it.blueBits())
            glfwWindowHint(GLFW_REFRESH_RATE, it.refreshRate())
        }

        window = glfwCreateWindow(Kore.configuration.width, Kore.configuration.height, Kore.configuration.title, if (Kore.configuration.fullscreen) glfwGetPrimaryMonitor() else 0L, 0L)

        if (window == 0L)
            Kore.log.fail(this::class, "Failed to create GLFW window")

        vidMode?.let {
            glfwSetWindowPos(window, (it.width() - Kore.configuration.width) / 2, (it.height() - Kore.configuration.height) / 2)
        }

        val iconPaths = Kore.configuration.icons

        if (iconPaths.isNotEmpty()) {
            val images = GLFWImage.calloc(iconPaths.size)
            val buffers = arrayListOf<ByteBuffer>()
            repeat(iconPaths.size) {
                val file = DesktopAssetFileHandle(iconPaths[it])
                if (file.exists)
                    readImage(file)?.let { image ->
                        images[it].width(image.width)
                        images[it].height(image.height)
                        val data = image.pixels.toByteArray()
                        val buffer = memAlloc(data.size)
                        buffer.put(data)
                        buffer.flip()
                        images[it].pixels(buffer)
                        buffers += buffer
                    }
            }

            glfwSetWindowIcon(window, images)
            images.free()
            buffers.forEach { memFree(it) }
        }

        glfwSetKeyCallback(window) { _, key, _, action, _ ->
            val down = action != GLFW_RELEASE
            if (down)
                inputEventQueue.onKeyDown(requireNotNull((Kore.input as DesktopInput).getKeyFromCode(key)), glfwGetTime())
            else
                inputEventQueue.onKeyUp(requireNotNull((Kore.input as DesktopInput).getKeyFromCode(key)), glfwGetTime())
        }

        glfwSetCharCallback(window) { _, code ->
            inputEventQueue.onChar(code.toChar(), glfwGetTime())
        }

        glfwSetMouseButtonCallback(window) { _, button, action, _ ->
            val down = action == GLFW_PRESS
            if (down)
                inputEventQueue.onTouchDown(Kore.input.x, Kore.input.y, 0, requireNotNull((Kore.input as DesktopInput).getMouseButtonFromCode(button)), glfwGetTime())
            else
                inputEventQueue.onTouchUp(Kore.input.x, Kore.input.y, 0, requireNotNull((Kore.input as DesktopInput).getMouseButtonFromCode(button)), glfwGetTime())
        }

        glfwSetScrollCallback(window) { _, x, y ->
            inputEventQueue.onScroll(x.toFloat(), y.toFloat(), glfwGetTime())
        }

        glfwSetJoystickCallback { id, event ->
            when (event) {
                GLFW_CONNECTED -> inputEventQueue.onGamepadConnected(id, glfwGetTime())
                GLFW_DISCONNECTED -> inputEventQueue.onGamepadDisconnected(id, glfwGetTime())
            }
        }

        glfwSetDropCallback(window) { _, count, names ->
            val array = Array(count) {
                GLFWDropCallback.getName(names, it)
            }

            dropListeners.forEach { listener ->
                listener(array)
            }
        }

        var firstResize = true
        glfwSetFramebufferSizeCallback(window) { _, width, height ->
            if (!Kore.configuration.fullscreen) {
                this.internalWidth = width
                this.internalHeight = height
                if (firstResize)
                    firstResize = false
                else {
                    resizeListeners.forEach {
                        it(width, height)
                    }
                    Kore.application.onResize(width, height)
                }
            }
        }

        glfwSetWindowFocusCallback(window) { _, focused ->
            internalIsFocused = focused
        }

        glfwMakeContextCurrent(window)
        val caps = createCapabilities()

        Kore.log.info(
            this::class, """
            OpenGL Info:
                Vendor:     ${glGetString(GL_VENDOR)}
                Version:    ${glGetString(GL_VERSION)}
                Renderer:   ${glGetString(GL_RENDERER)}
        """.trimIndent()
        )

        if (caps.OpenGL43)
            impl = GL43GraphicsImpl()
        else if (caps.OpenGL32)
            impl = GL32GraphicsImpl()
        else
            Kore.log.fail(this::class, "Minimum required OpenGL version is 3.2")

        glfwSwapInterval(if (Kore.configuration.vsync) 1 else 0)
        glfwShowWindow(window)

        Kore.log.info(this::class, "Successfully created window")
    }

    internal fun closeWindow() {
        Kore.log.info(this::class, "Closing window")

        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
        window = 0L
        impl.dispose()
    }

    override fun dispose() {
        glfwTerminate()

        glfwSetErrorCallback(null)
        errorCallback.free()
    }
}

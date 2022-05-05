package com.cozmicgames.graphics

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.Files
import com.cozmicgames.files.ReadStream
import com.cozmicgames.files.WriteStream
import com.cozmicgames.graphics
import com.cozmicgames.graphics.gpu.*
import com.cozmicgames.graphics.gpu.pipeline.PipelineDefinition
import com.cozmicgames.memory.Memory
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.extensions.emptyIntArray
import com.cozmicgames.utils.extensions.extension
import com.cozmicgames.utils.maths.Rectangle
import com.cozmicgames.utils.use

/**
 * [Graphics] is the framework module for reading fonts and images, writing images and abstracting the platform specific graphics API.
 * It must be implemented by the platform specific implementation and bound to [Kore.context].
 */
interface Graphics {
    /**
     * The width of the client area in pixels.
     */
    val width: Int

    /**
     * The height of the client area in pixels.
     */
    val height: Int

    /**
     * Whether the application runs in VSync mode.
     * This is modifiable at runtime.
     */
    var isVSync: Boolean

    /**
     * The native client scale factor.
     */
    val clientScale: Float

    /**
     * The inset from the left which avoids display cutouts in pixels.
     */
    val safeInsetLeft: Int

    /**
     * The inset from the right which avoids display cutouts in pixels.
     */
    val safeInsetRight: Int

    /**
     * The inset from the top which avoids display cutouts in pixels.
     */
    val safeInsetTop: Int

    /**
     * The inset from the bottom which avoids display cutouts in pixels.
     */
    val safeInsetBottom: Int

    /**
     * The title of the application.
     * This is modifiable at runtime.
     */
    var title: String

    /**
     * Whether the application is resizable.
     */
    val isResizable: Boolean

    /**
     * Whether the application is focused.
     */
    val isFocused: Boolean

    /**
     * The current frame index, which represents the number of rendered frames.
     */
    val frameIndex: Int

    /**
     * Whether the platform specific graphics API supports compute functionality.
     */
    val supportsCompute: Boolean

    /**
     * The frameworks graphics statistics.
     */
    val statistics: Statistics

    /**
     * The default font provided by the framework.
     */
    val defaultFont: Font

    /**
     * The native [UniformBuffer.Layout] used by the platform specific graphics API.
     */
    val uniformBufferLayout: UniformBuffer.Layout

    /**
     * The formats supported for loading images.
     */
    val supportedImageFormats: Iterable<String>

    /**
     * The formats supported for loading fonts.
     */
    val supportedFontFormats: Iterable<String>

    /**
     * Reads an image from the given [ReadStream] in the specified [format].
     * Returns null if the image could not be loaded.
     *
     * @param stream The [ReadStream] to read the image from.
     * @param format The format of the image.
     *
     * @return The loaded image.
     */
    fun readImage(stream: ReadStream, format: String): Image?

    /**
     * Writes an image to the given [WriteStream] in the specified [format].
     *
     * @param stream The [WriteStream] to write the image to.
     * @param image The image to write.
     * @param format The format of the image.
     */
    fun writeImage(stream: WriteStream, image: Image, format: String)

    /**
     * Reads a font from the given [ReadStream] in the specified [format].
     * Returns null if the font could not be loaded.
     *
     * @param stream The [ReadStream] to read the font from.
     * @param format The format of the font.
     *
     * @return The loaded font.
     */
    fun readFont(stream: ReadStream, format: String): Font?

    /**
     * This is part of the abstracted graphics API.
     * Creates a new [Pipeline] with the specified [type].
     *
     * Pipelines can be either of type [Pipeline.Type.GRAPHICS] or [Pipeline.Type.COMPUTE].
     * The [Pipeline.Type.GRAPHICS] pipeline is used for rendering and the [Pipeline.Type.COMPUTE] pipeline is used for compute operations.
     *
     * Pipelines encapsulate the state of the graphics API as well as the shaders used for rendering or compute.
     * They also provide all uniforms used by the shaders.
     *
     * It is advised to use a [PipelineDefinition] to create a pipeline instead of using this function directly.
     * However, if you need to create a pipeline manually, you can use this function.
     *
     * @param type The type of the pipeline.
     * @param block The block to execute to configure the pipeline.
     *
     * @return The created [Pipeline].
     */
    fun createPipeline(type: Pipeline.Type, block: Pipeline.() -> Unit = {}): Pipeline

    /**
     * This is part of the abstracted graphics API.
     * Creates a new [GraphicsBuffer] with the specified [usage].
     *
     * Buffers are used to store vertex data, index data, uniform data, etc.
     *
     * @param usage The usage of the buffer.
     * @param block The block to execute to configure the buffer.
     *
     * @return The created [GraphicsBuffer].
     */
    fun createBuffer(usage: GraphicsBuffer.Usage, block: GraphicsBuffer.() -> Unit = {}): GraphicsBuffer

    /**
     * This is part of the abstracted graphics API.
     * Creates a new [Texture2D] with the specified [format].
     * The [format] is used to determine the internal format of the texture.
     *
     * @param format The format of the texture.
     * @param block The block to execute to configure the texture.
     *
     * @return The created [Texture2D].
     */
    fun createTexture2D(format: Texture.Format, block: Texture2D.() -> Unit = {}): Texture2D

    /**
     * This is part of the abstracted graphics API.
     * Creates a new [TextureCube] with the specified [format].
     * The [format] is used to determine the internal format of the texture.
     *
     * @param format The format of the texture.
     * @param block The block to execute to configure the texture.
     *
     * @return The created [TextureCube].
     */
    fun createTextureCube(format: Texture.Format, block: TextureCube.() -> Unit = {}): TextureCube

    /**
     * This is part of the abstracted graphics API.
     * Creates a new [Texture3D] with the specified [format].
     * The [format] is used to determine the internal format of the texture.
     *
     * @param format The format of the texture.
     * @param block The block to execute to configure the texture.
     *
     * @return The created [Texture3D].
     */
    fun createTexture3D(format: Texture.Format, block: Texture3D.() -> Unit = {}): Texture3D

    /**
     * This is part of the abstracted graphics API.
     * Creates a new [Framebuffer].
     * A framebuffer is used to render to textures.
     * It can have multiple color attachments, a depth attachment and a stencil attachment.
     * The attachments are of type [Texture2D].
     *
     * @param block The block to execute to configure the framebuffer.
     *
     * @return The created [Framebuffer].
     */
    fun createFramebuffer(block: Framebuffer.() -> Unit = {}): Framebuffer

    /**
     * This is part of the abstracted graphics API.
     * Makes the specified [Pipeline] the current pipeline.
     * If it is of the type [Pipeline.Type.GRAPHICS], it will be used for rendering.
     * If it is of the type [Pipeline.Type.COMPUTE], it will be used for compute operations.
     * If the pipeline is null, it will unbind the current pipeline.
     * Unbinding is not necessary before binding a different pipeline.
     *
     * @param pipeline The pipeline to make the current pipeline.
     */
    fun setPipeline(pipeline: Pipeline?)

    /**
     * This is part of the abstracted graphics API.
     * Makes the specified [Framebuffer] the current framebuffer.
     * If the framebuffer is null, the default framebuffer is used.
     */
    fun setFramebuffer(framebuffer: Framebuffer?)

    /**
     * This is part of the abstracted graphics API.
     * Sets the specified [ScissorRect].
     * The scissor test is used to clip the rendering to a specified rectangle.
     * The scissor test is not affected by the viewport.
     * If the scissor rect is null, the scissor test is disabled.
     *
     * @param rect The scissor rect to set.
     */
    fun setScissor(rect: ScissorRect?)

    /**
     * This is part of the abstracted graphics API.
     * Sets the specified [Viewport].
     * The viewport is used to scale the rendering to a specified rectangle.
     * The viewport is not affected by the scissor test.
     * If the viewport is null, the viewport is set to the size of the window.
     *
     * @param viewport The viewport to set.
     */
    fun setViewport(viewport: Viewport?)

    /**
     * This is part of the abstracted graphics API.
     * Makes the specified [GraphicsBuffer] the current vertex buffer for the specified [indices].
     * The [indices] are used to specify the index of the vertex attributes in the [VertexLayout] in the current [Pipeline].
     * If the [GraphicsBuffer] is null, the current vertex buffer is unbound.
     * Unbinding is not necessary before binding a different vertex buffer.
     *
     * @param buffer The buffer to make the current vertex buffer.
     * @param indices The indices of the vertex attributes in the [VertexLayout] in the current [Pipeline].
     */
    fun setVertexBuffer(buffer: GraphicsBuffer?, indices: IntArray = emptyIntArray())

    /**
     * This is part of the abstracted graphics API.
     * Makes the specified [GraphicsBuffer] the current index buffer.
     * If the [GraphicsBuffer] is null, the current index buffer is unbound.
     * Unbinding is not necessary before binding a different index buffer.
     *
     * @param buffer The buffer to make the current index buffer.
     */
    fun setIndexBuffer(buffer: GraphicsBuffer?)

    /**
     * This is part of the abstracted graphics API.
     * Clears the current framebuffer with the specified [color], [depth] and/or [stencil].
     * If the [color] is null, the color buffer is not cleared.
     * If the [depth] is null, the depth buffer is not cleared.
     * If the [stencil] is null, the stencil buffer is not cleared.
     *
     * @param color The color to clear the color buffer with. Defaults to null.
     * @param depth The depth to clear the depth buffer with. Defaults to null.
     * @param stencil The stencil to clear the stencil buffer with. Defaults to null.
     */
    fun clear(color: Color? = null, depth: Float? = null, stencil: Int? = null)

    /**
     * This is part of the abstracted graphics API.
     * Draws the specified [Primitive] with the specified [length] without using an index buffer.
     *
     * @param primitive The primitive to draw.
     * @param length The number of vertices to draw.
     * @param offset The offset of the first vertex to draw.
     * @param numInstances The number of instances to draw.
     */
    fun draw(primitive: Primitive, length: Int, offset: Int = 0, numInstances: Int = 1)

    /**
     * This is part of the abstracted graphics API.
     * Draws the specified [Primitive] with the specified [length] using the current index buffer.
     *
     * @param primitive The primitive to draw.
     * @param length The number of vertices to draw.
     * @param offset The offset of the first index to use.
     * @param type The type of the data in the index buffer.
     * @param numInstances The number of instances to draw.
     */
    fun drawIndexed(primitive: Primitive, length: Int, offset: Int = 0, type: IndexDataType = IndexDataType.INT, numInstances: Int = 1)

    /**
     * This is part of the abstracted graphics API.
     * Dispatches a compute shader with the specified [groupsX], [groupsY] and [groupsZ].
     * The number of groups is the number of times the compute shader is executed.
     * This needs [supportsCompute] to be true.
     *
     * @param groupsX The number of groups to dispatch in the x direction.
     * @param groupsY The number of groups to dispatch in the y direction.
     * @param groupsZ The number of groups to dispatch in the z direction.
     */
    fun dispatchCompute(groupsX: Int, groupsY: Int, groupsZ: Int)

    /**
     * This is part of the abstracted graphics API.
     * Draws the specified [Primitive] with draw data stored in [buffer] without using an index buffer.
     *
     * Draw commands are laid out as follows:
     * 32-bit integer: number of vertices
     * 32-bit integer: number of instances
     * 32-bit integer: offset of first vertex
     * 32-bit integer: offset of first instance
     *
     * @param primitive The primitive to draw.
     * @param buffer The buffer containing the draw data.
     * @param count The number of draw commands to execute.
     * @param stride The stride between draw commands. Defaults to 16.
     */
    fun drawIndirect(primitive: Primitive, buffer: GraphicsBuffer, count: Int = 1, stride: Int = 16)

    /**
     * This is part of the abstracted graphics API.
     * Draws the specified [Primitive] with draw data stored in [buffer] using the current index buffer.
     *
     * Draw commands are laid out as follows:
     * 32-bit integer: number of vertices
     * 32-bit integer: number of instances
     * 32-bit integer: offset of first index
     * 32-bit integer: offset of first vertex
     * 32-bit integer: offset of first instance
     *
     * @param primitive The primitive to draw.
     * @param buffer The buffer containing the draw data.
     * @param type The type of the data in the index buffer. Defaults to [IndexDataType.INT].
     * @param count The number of draw commands to execute.
     * @param stride The stride between draw commands. Defaults to 20.
     */
    fun drawIndexedIndirect(primitive: Primitive, buffer: GraphicsBuffer, type: IndexDataType = IndexDataType.INT, count: Int = 1, stride: Int = 20)

    /**
     * This is part of the abstracted graphics API.
     * Dispatches a compute shader with data stored in [buffer].
     * This needs [supportsCompute] to be true.
     *
     * Dispatch commands are laid out as follows:
     * 32-bit integer: number of groups in x direction
     * 32-bit integer: number of groups in y direction
     * 32-bit integer: number of groups in z direction
     *
     * @param buffer The buffer containing the dispatch data.
     */
    fun dispatchComputeIndirect(buffer: GraphicsBuffer)
}

/**
 * The aspect ratio of the client area.
 */
val Graphics.aspect get() = width.toFloat() / height.toFloat()

/**
 * @see [Graphics.setVertexBuffer]
 */
fun Graphics.setVertexBuffer(buffer: GraphicsBuffer?, vararg indices: Int) = setVertexBuffer(buffer, indices)

/**
 * The safe width to avoid display cutouts in pixels.
 */
val Graphics.safeWidth get() = width - safeInsetLeft - safeInsetRight

/**
 * The safe height to avoid display cutouts in pixels.
 */
val Graphics.safeHeight get() = height - safeInsetBottom - safeInsetTop

/**
 * Gets the safe view rectangle in pixels.
 * The safe view rectangle is the area of the screen that is not cut out by the system.
 *
 * @param rectangle The rectangle to fill with the safe view rectangle. Defaults to creating a new [Rectangle] instance.
 *
 * @return The safe view rectangle.
 */
fun Graphics.getViewRectangle(rectangle: Rectangle = Rectangle()): Rectangle {
    rectangle.x = safeInsetLeft.toFloat()
    rectangle.y = safeInsetBottom.toFloat()
    rectangle.width = safeWidth.toFloat()
    rectangle.height = safeHeight.toFloat()
    return rectangle
}

private val _defaultTexture2D by lazy {
    Kore.graphics.createTexture2D(Texture.Format.RGBA8_UNORM) {
        Memory(Memory.SIZEOF_INT).use {
            it.setInt(0, Color.MAGENTA.bits)
            setImage(1, 1, it)
        }
    }.also {
        Kore.addShutdownListener {
            it.dispose()
        }
    }
}

private val _defaultTextureCube by lazy {
    Kore.graphics.createTextureCube(Texture.Format.RGBA8_UNORM) {
        Memory(Memory.SIZEOF_INT).use {
            it.setInt(0, Color.MAGENTA.bits)
            setImages(1, 1, Texture.Format.RGBA8_UNORM) {
                negativeX = it
                positiveX = it
                negativeY = it
                positiveY = it
                negativeZ = it
                positiveZ = it
            }
        }
    }.also {
        Kore.addShutdownListener {
            it.dispose()
        }
    }
}

private val _defaultTexture3D by lazy {
    Kore.graphics.createTexture3D(Texture.Format.RGBA8_UNORM) {
        Memory(Memory.SIZEOF_INT).use {
            it.setInt(0, Color.MAGENTA.bits)
            setImage(1, 1, 1, it)
        }
    }.also {
        Kore.addShutdownListener {
            it.dispose()
        }
    }
}

/**
 * The default [Texture2D] to use when no texture is specified.
 */
val Graphics.defaultTexture2D get() = _defaultTexture2D

/**
 * The default [TextureCube] to use when no texture is specified.
 */
val Graphics.defaultTextureCube get() = _defaultTextureCube

/**
 * The default [Texture3D] to use when no texture is specified.
 */
val Graphics.defaultTexture3D get() = _defaultTexture3D

/**
 * @see [Graphics.setVertexBuffer]
 */
fun Graphics.setVertexBuffer(buffer: GraphicsBuffer?, layout: VertexLayout?) = setVertexBuffer(buffer, layout?.indices ?: emptyIntArray())

/**
 * Loads an image from the given [file] with the specified [type].
 * @see [Graphics.readImage]
 *
 * @param file The file to load the image from.
 * @param type The type of the image.
 *
 * @return The loaded image.
 */
fun Graphics.loadImage(file: String, type: Files.Type) = when (type) {
    Files.Type.ASSET -> Kore.files.readAsset(file)
    Files.Type.RESOURCE -> Kore.files.readAsset(file)
}.use {
    readImage(it, file.extension)
}

/**
 * Writes the given [image] to the given [file] as a resource.
 * @see [Graphics.writeImage]
 *
 * @param file The file to write the image to.
 * @param image The image to write.
 */
fun Graphics.saveImage(file: String, image: Image) = Kore.files.writeResource(file, false).use {
    writeImage(it, image, file.extension)
}

/**
 * Loads a font from the given [file] with the specified [type].
 * @see [Graphics.readFont]
 *
 * @param file The file to load the font from.
 * @param type The type of the font.
 *
 * @return The loaded font.
 */
fun Graphics.loadFont(file: String, type: Files.Type) = when (type) {
    Files.Type.ASSET -> Kore.files.readAsset(file)
    Files.Type.RESOURCE -> Kore.files.readAsset(file)
}.use {
    readFont(it, file.extension)
}

/**
 * Loads an image from the given [fileHandle].
 * @see [Graphics.readImage]
 *
 * @param fileHandle The file handle to load the image from.
 *
 * @return The loaded image.
 */
fun Graphics.loadImage(fileHandle: FileHandle) = loadImage(fileHandle.path, fileHandle.type)

/**
 * Writes the given [image] to the given [fileHandle].
 * @see [Graphics.writeImage]
 *
 * @param fileHandle The file handle to write the image to.
 * @param image The image to write.
 */
fun Graphics.saveImage(fileHandle: FileHandle, image: Image) = saveImage(fileHandle.path, image)

/**
 * Loads a font from the given [fileHandle].
 * @see [Graphics.readFont]
 *
 * @param fileHandle The file handle to load the font from.
 * @param type The type of the font.
 *
 * @return The loaded font.
 */
fun Graphics.loadFont(fileHandle: FileHandle) = loadFont(fileHandle.path, fileHandle.type)


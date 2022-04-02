package com.gratedgames.graphics

import com.gratedgames.Kore
import com.gratedgames.files
import com.gratedgames.files.Files
import com.gratedgames.files.ReadStream
import com.gratedgames.files.WriteStream
import com.gratedgames.graphics
import com.gratedgames.graphics.gpu.*
import com.gratedgames.memory.Memory
import com.gratedgames.utils.Color
import com.gratedgames.utils.extensions.emptyIntArray
import com.gratedgames.utils.extensions.extension
import com.gratedgames.utils.maths.Rectangle
import com.gratedgames.utils.use

interface Graphics {
    val width: Int
    val height: Int
    var isVSync: Boolean
    val clientScale: Float
    val safeInsetLeft: Int
    val safeInsetRight: Int
    val safeInsetTop: Int
    val safeInsetBottom: Int
    var title: String
    val isResizable: Boolean
    val isFocused: Boolean
    val frameIndex: Int
    val supportsCompute: Boolean
    val statistics: Statistics
    val defaultFont: Font
    val uniformBufferLayout: UniformBuffer.Layout
    val supportedImageFormats: Iterable<String>
    val supportedFontFormats: Iterable<String>

    fun readImage(stream: ReadStream, format: String): Image?
    fun writeImage(stream: WriteStream, image: Image, format: String)
    fun readFont(stream: ReadStream, format: String): Font?
    fun createPipeline(type: Pipeline.Type, block: Pipeline.() -> Unit = {}): Pipeline
    fun createBuffer(usage: GraphicsBuffer.Usage, block: GraphicsBuffer.() -> Unit = {}): GraphicsBuffer
    fun createTexture2D(format: Texture.Format, block: Texture2D.() -> Unit = {}): Texture2D
    fun createTextureCube(format: Texture.Format, block: TextureCube.() -> Unit = {}): TextureCube
    fun createTexture3D(format: Texture.Format, block: Texture3D.() -> Unit = {}): Texture3D
    fun createFramebuffer(block: Framebuffer.() -> Unit = {}): Framebuffer
    fun setPipeline(pipeline: Pipeline?)
    fun setFramebuffer(framebuffer: Framebuffer?)
    fun setScissor(rect: ScissorRect?)
    fun setViewport(viewport: Viewport?)
    fun setVertexBuffer(buffer: GraphicsBuffer?, indices: IntArray = emptyIntArray())
    fun setIndexBuffer(buffer: GraphicsBuffer?)
    fun clear(color: Color? = null, depth: Float? = null, stencil: Int? = null)
    fun draw(primitive: Primitive, length: Int, offset: Int = 0, numInstances: Int = 1)
    fun drawIndexed(primitive: Primitive, length: Int, offset: Int = 0, type: IndexDataType = IndexDataType.INT, numInstances: Int = 1)
    fun dispatchCompute(groupsX: Int, groupsY: Int, groupsZ: Int)
    fun drawIndirect(primitive: Primitive, buffer: GraphicsBuffer, count: Int = 1, stride: Int = 16)
    fun drawIndexedIndirect(primitive: Primitive, buffer: GraphicsBuffer, type: IndexDataType = IndexDataType.INT, count: Int = 1, stride: Int = 20)
    fun dispatchComputeIndirect(buffer: GraphicsBuffer)
}

val Graphics.aspect get() = width.toFloat() / height.toFloat()

fun Graphics.setVertexBuffer(buffer: GraphicsBuffer?, vararg indices: Int) = setVertexBuffer(buffer, indices)

val Graphics.safeWidth get() = width - safeInsetLeft - safeInsetRight

val Graphics.safeHeight get() = height - safeInsetBottom - safeInsetTop

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

val Graphics.defaultTexture2D get() = _defaultTexture2D
val Graphics.defaultTextureCube get() = _defaultTextureCube
val Graphics.defaultTexture3D get() = _defaultTexture3D

fun Graphics.setVertexBuffer(buffer: GraphicsBuffer?, layout: VertexLayout?) = setVertexBuffer(buffer, layout?.indices ?: emptyIntArray())

fun Graphics.loadImage(file: String, type: Files.Type) = when (type) {
    Files.Type.ASSET -> Kore.files.readAsset(file)
    Files.Type.RESOURCE -> Kore.files.readAsset(file)
}.use {
    readImage(it, file.extension)
}

fun Graphics.saveImage(file: String, image: Image) = Kore.files.writeResource(file, false).use {
    writeImage(it, image, file.extension)
}

fun Graphics.loadFont(file: String, type: Files.Type) = when (type) {
    Files.Type.ASSET -> Kore.files.readAsset(file)
    Files.Type.RESOURCE -> Kore.files.readAsset(file)
}.use {
    readFont(it, file.extension)
}

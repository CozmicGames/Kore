package com.cozmicgames.core.graphics

import com.cozmicgames.core.Kore
import com.cozmicgames.core.graphics.rhi.GPUShaderSource
import com.cozmicgames.core.graphics.rhi.GPUDevice
import com.cozmicgames.core.files.FileHandle
import com.cozmicgames.core.utils.maths.Rectangle

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
     * The frameworks graphics statistics.
     */
    val statistics: Statistics

    /**
     * The default font provided by the framework.
     */
    val defaultFont: Font

    /**
     * The formats supported for loading images.
     */
    val supportedImageFormats: Iterable<String>

    /**
     * The formats supported for loading fonts.
     */
    val supportedFontFormats: Iterable<String>

    /**
     * The GPU device used by the framework.
     */
    val device: GPUDevice

    /**
     * The shader compiler used by the framework.
     */
    val shaderCompiler: ShaderCompiler

    /**
     * Reads an image from the given [file].
     *
     * @param file The file handle to load the image from.
     *
     * @return The loaded image.
     */
    fun readImage(file: FileHandle): Image?

    /**
     * Writes the given [image] to the given [file].
     *
     * @param file The file handle to write the image to.
     * @param image The image to write.
     */
    fun writeImage(file: FileHandle, image: Image)

    /**
     * Reads a font from the given [file].
     *
     * @param file The file handle to load the font from.
     *
     * @return The loaded font.
     */
    fun readFont(file: FileHandle): Font?

    /**
     * Reads a shader source from the given [file].
     *
     * @param file The file handle to read the shader source from.
     *
     * @return The shader source, or null if the file could not be read.
     */
    fun readShaderSource(file: FileHandle): GPUShaderSource?

    /**
     * Writes the given [shaderSource] to the given [file].
     *
     * @param file The file handle to write the shader to.
     * @param shaderSource The shader to write.
     */
    fun writeShaderSource(file: FileHandle, shaderSource: GPUShaderSource)
}

/**
 * The aspect ratio of the client area.
 */
val Graphics.aspect get() = width.toFloat() / height.toFloat()

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

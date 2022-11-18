package com.cozmicgames.graphics

import com.cozmicgames.memory.Memory
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.extensions.clamp

/**
 * A flattened array of pixels.
 *
 * @param width The width of the pixel array.
 * @param height The height of the pixel array.
 */
class PixelData(val width: Int, val height: Int) {
    val data = Array(width * height) { Color(Color.CLEAR) }

    /**
     * Gets the pixel at the specified index.
     * If the index is out of bounds, it will return [Color.CLEAR].
     *
     * @param index The index of the pixel to get.
     *
     * @return The pixel at the specified index.
     */
    operator fun get(index: Int): Color {
        if (index < 0 || index >= data.size)
            return Color.CLEAR

        return data[index]
    }

    /**
     * Gets the pixel at the specified index.
     *
     * @param index The index of the pixel to get.
     * @param color The color to set the pixel to.
     */
    operator fun set(index: Int, color: Color) {
        if (index < 0 || index >= data.size)
            return

        data[index].set(color)
    }

    /**
     * Fills the pixel array with the specified color.
     *
     * @param color The color to fill the pixel array with.
     */
    fun fill(color: Color) {
        data.forEach {
            it.set(color)
        }
    }

    /**
     * Copies this [PixelData].
     */
    fun copy(): PixelData {
        val copiedData = PixelData(width, height)
        data.forEachIndexed { index, color ->
            copiedData.data[index] = color.copy()
        }
        return copiedData
    }
}

/**
 * Converts the pixel array to a [ByteArray].
 * The byte array will be in RGBA format.
 *
 * @return The byte array containing the pixel data.
 */
fun PixelData.toByteArray(): ByteArray {
    val array = ByteArray(data.size * 4)
    data.forEachIndexed { index, color ->
        val memoryIndex = index * Memory.SIZEOF_BYTE * 4
        array[memoryIndex] = (color.r.clamp(0.0f, 1.0f) * 0xFF).toInt().toByte()
        array[memoryIndex + 1] = (color.g.clamp(0.0f, 1.0f) * 0xFF).toInt().toByte()
        array[memoryIndex + 2] = (color.b.clamp(0.0f, 1.0f) * 0xFF).toInt().toByte()
        array[memoryIndex + 3] = (color.a.clamp(0.0f, 1.0f) * 0xFF).toInt().toByte()
    }
    return array
}
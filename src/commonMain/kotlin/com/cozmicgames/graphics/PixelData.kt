package com.cozmicgames.graphics

import com.cozmicgames.memory.Memory
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.extensions.clamp

class PixelData(width: Int, height: Int) {
    val data = Array(width * height) { Color(Color.CLEAR) }

    operator fun get(index: Int): Color {
        if (index < 0 || index >= data.size)
            return Color.CLEAR

        return data[index]
    }

    operator fun set(index: Int, color: Color) {
        if (index < 0 || index >= data.size)
            return

        data[index].set(color)
    }

    fun fill(color: Color) {
        data.forEach {
            it.set(color)
        }
    }
}

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
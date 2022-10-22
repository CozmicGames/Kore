package com.cozmicgames.audio

import com.cozmicgames.utils.Disposable
import org.lwjgl.openal.AL10.*
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioBuffer : Disposable {
    val handle = alGenBuffers()

    fun setData(data: ByteArray, count: Int, isBigEndian: Boolean, format: Int, sampleRate: Int) {
        val buffer = MemoryUtil.memAlloc(count)
        val srcBuffer = ByteBuffer.wrap(data, 0, count)
        srcBuffer.order(if (isBigEndian) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN)

        if (format == AL_FORMAT_MONO16 || format == AL_FORMAT_STEREO16) {
            val bufferShort = buffer.asShortBuffer()
            val srcBufferShort = srcBuffer.asShortBuffer()

            while (srcBufferShort.hasRemaining())
                bufferShort.put(srcBufferShort.get())
        } else {
            while (srcBuffer.hasRemaining())
                buffer.put(srcBuffer.get())
        }

        buffer.position(0)
        alBufferData(handle, format, buffer, sampleRate)
        MemoryUtil.memFree(buffer)
    }

    override fun dispose() {
        alDeleteBuffers(handle)
    }
}
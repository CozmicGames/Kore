package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.audio.formats.AudioStream
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.use
import org.lwjgl.openal.AL10.*

abstract class AudioData : Disposable {
    var isDisposed = false
        private set

    var isPlaying = false
        protected set

    open fun begin(source: AudioSource, loop: Boolean) {}
    open fun update(source: AudioSource) {}
    open fun end(source: AudioSource) {}

    open fun endLooping(source: AudioSource) {}

    abstract fun freeBuffers()

    override fun dispose() {
        if (!isPlaying)
            freeBuffers()

        isDisposed = true
    }
}

class LoadedAudioData(stream: AudioStream) : AudioData() {
    val buffer = (Kore.audio as DesktopAudio).obtainBuffer()

    init {
        stream.use {
            val data = ByteArray(it.remaining)
            val count = it.read(data)

            buffer.setData(data, count, it.isBigEndian, it.sampleSize, it.channels, it.sampleRate)
        }
    }

    override fun begin(source: AudioSource, loop: Boolean) {
        alSourcei(source.handle, AL_LOOPING, if (loop) AL_TRUE else AL_FALSE)
        alSourcei(source.handle, AL_BUFFER, buffer.handle)
        isPlaying = true
    }

    override fun end(source: AudioSource) {
        isPlaying = false
    }

    override fun endLooping(source: AudioSource) {
        alSourcei(source.handle, AL_LOOPING, AL_FALSE)
    }

    override fun freeBuffers() {
        (Kore.audio as DesktopAudio).freeBuffer(buffer)
    }
}

class StreamedAudioData(private val stream: AudioStream) : AudioData() {
    companion object {
        private const val TEMP_BUFFER_SIZE = 4096 * 10
    }

    private val buffers = Array(3) { (Kore.audio as DesktopAudio).obtainBuffer() }

    private var isLooping = false

    private val tempBytes = ByteArray(TEMP_BUFFER_SIZE)

    private fun fill(buffer: AudioBuffer): Boolean {
        var length = stream.read(tempBytes)

        if (length <= 0) {
            if (isLooping) {
                stream.reset()
                length = stream.read(tempBytes)
                if (length <= 0)
                    return false
            } else
                return false
        }

        buffer.setData(tempBytes, length, stream.isBigEndian, stream.sampleSize, stream.channels, stream.sampleRate)

        return true
    }

    override fun begin(source: AudioSource, loop: Boolean) {
        isLooping = loop

        buffers.forEach {
            if (!fill(it))
                return

            alSourceQueueBuffers(source.handle, it.handle)
        }
    }

    override fun update(source: AudioSource) {
        var end = false
        var buffers = alGetSourcei(source.handle, AL_BUFFERS_PROCESSED)

        while (buffers-- > 0) {
            val bufferHandle = alSourceUnqueueBuffers(source.handle)
            if (bufferHandle == AL_INVALID_VALUE)
                break

            if (end)
                continue

            val buffer = requireNotNull(this.buffers.find { it.handle == bufferHandle })

            if (fill(buffer))
                alSourceQueueBuffers(source.handle, bufferHandle)
            else
                end = true
        }
    }

    override fun end(source: AudioSource) {
        alSourcei(source.handle, AL_BUFFER, 0)
        stream.reset()
    }

    override fun endLooping(source: AudioSource) {
        isLooping = false
    }

    override fun freeBuffers() {
        buffers.forEach {
            (Kore.audio as DesktopAudio).freeBuffer(it)
        }
    }

    override fun dispose() {
        super.dispose()
        stream.dispose()
    }
}

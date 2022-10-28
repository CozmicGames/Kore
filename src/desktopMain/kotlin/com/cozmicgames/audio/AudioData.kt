package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.audio.formats.AudioFile
import com.cozmicgames.utils.Disposable
import org.lwjgl.openal.AL10.*

abstract class AudioData : Disposable {
    open fun begin(source: AudioDataSource) {}
    open fun update(source: AudioDataSource) {}
    open fun end(source: AudioDataSource) {}
}

class LoadedAudioData(file: AudioFile) : AudioData() {
    val buffer = (Kore.audio as DesktopAudio).obtainBuffer()

    init {
        val data = ByteArray(file.size)
        val count = file.readFully(data)
        buffer.setData(data, count, file.isBigEndian, file.sampleSize, file.channels, file.sampleRate)
    }

    override fun begin(source: AudioDataSource) {
        alSourcei(source.handle, AL_BUFFER, buffer.handle)
    }

    override fun end(source: AudioDataSource) {
        alSourcei(source.handle, AL_BUFFER, AL_NONE)
    }

    override fun dispose() {
        (Kore.audio as DesktopAudio).freeBuffer(buffer)
    }
}

class StreamedAudioData(private val file: AudioFile) : AudioData() {
    companion object {
        private const val TEMP_BUFFER_SIZE = 4096 * 10
    }

    private inner class Stream(val source: AudioDataSource) {
        private val stream = file.openStream()
        private val buffers = Array(3) { (Kore.audio as DesktopAudio).obtainBuffer() }
        private val tempBytes = ByteArray(TEMP_BUFFER_SIZE)

        private fun fill(buffer: AudioBuffer): Boolean {
            var length = stream.read(tempBytes)

            if (length <= 0) {
                if (alGetSourcei(source.handle, AL_LOOPING) == AL_TRUE) {
                    stream.reset()
                    length = stream.read(tempBytes)
                    if (length <= 0)
                        return false
                } else
                    return false
            }

            buffer.setData(tempBytes, length, file.isBigEndian, file.sampleSize, file.channels, file.sampleRate)

            return true
        }

        fun begin() {
            for (buffer in buffers) {
                if (!fill(buffer))
                    continue

                alSourceQueueBuffers(source.handle, buffer.handle)
            }
        }

        fun update() {
            var end = false
            var buffers = alGetSourcei(source.handle, AL_BUFFERS_PROCESSED)

            while (buffers-- > 0) {
                val bufferHandle = alSourceUnqueueBuffers(source.handle)

                if (bufferHandle == AL_INVALID_VALUE)
                    end = true

                if (end)
                    continue

                val buffer = requireNotNull(this.buffers.find { it.handle == bufferHandle })

                if (fill(buffer))
                    alSourceQueueBuffers(source.handle, bufferHandle)
                else
                    end = true
            }

            if (end)
                source.stopPlaying()
        }

        fun end() {
            var queuedBuffers = alGetSourcei(source.handle, AL_BUFFERS_QUEUED)
            while (queuedBuffers-- > 0)
                alSourceUnqueueBuffers(source.handle)

            stream.dispose()
            buffers.forEach {
                (Kore.audio as DesktopAudio).freeBuffer(it)
            }

            streams.removeIf { it.source.handle == source.handle }
        }
    }

    private val streams = arrayListOf<Stream>()

    override fun begin(source: AudioDataSource) {
        val stream = Stream(source)
        stream.begin()
        streams += stream
    }

    override fun update(source: AudioDataSource) {
        streams.filter { it.source == source }.forEach { it.update() }
    }

    override fun end(source: AudioDataSource) {
        streams.filter { it.source == source }.forEach { it.end() }
    }

    override fun dispose() {

    }
}

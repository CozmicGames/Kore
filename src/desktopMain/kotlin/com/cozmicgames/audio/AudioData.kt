package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.audio.formats.AudioStream
import com.cozmicgames.utils.Disposable
import org.lwjgl.openal.AL10.*

abstract class AudioData : Disposable {
    var isDisposed = false
        private set

    var isPlaying = false
        protected set

    open fun begin(source: AudioSource) {}
    open fun update(source: AudioSource) {}
    open fun end(source: AudioSource) {}

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
            val sampleFormat = when (it.sampleSize) {
                8 -> when (it.channels) {
                    1 -> AL_FORMAT_MONO8
                    2 -> AL_FORMAT_STEREO8
                    else -> throw RuntimeException()
                }
                16 -> when (it.channels) {
                    1 -> AL_FORMAT_MONO16
                    2 -> AL_FORMAT_STEREO16
                    else -> throw RuntimeException()
                }
                else -> throw RuntimeException()
            }

            val data = ByteArray(it.remaining)
            val count = it.read(data)

            buffer.setData(data, count, it.isBigEndian, sampleFormat, it.sampleRate)
        }
    }

    override fun begin(source: AudioSource) {
        alSourcei(source.handle, AL_BUFFER, buffer.handle)
        isPlaying = true
    }

    override fun end(source: AudioSource) {
        isPlaying = false
    }

    override fun freeBuffers() {
        (Kore.audio as DesktopAudio).freeBuffer(buffer)
    }
}

class StreamedAudioData(val stream: AudioStream) : AudioData() {
    private val buffers = Array(3) { (Kore.audio as DesktopAudio).obtainBuffer() }

    override fun begin(source: AudioSource) {

    }

    override fun update(source: AudioSource) {

    }

    override fun end(source: AudioSource) {

    }

    override fun freeBuffers() {
        buffers.forEach {
            (Kore.audio as DesktopAudio).freeBuffer(it)
        }
    }
}

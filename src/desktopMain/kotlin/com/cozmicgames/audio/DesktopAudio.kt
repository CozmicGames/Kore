package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio.formats.MP3
import com.cozmicgames.audio.formats.WAV
import com.cozmicgames.files.DesktopReadStream
import com.cozmicgames.files.ReadStream
import com.cozmicgames.log
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.Updateable
import com.cozmicgames.utils.collections.DynamicStack
import com.cozmicgames.utils.collections.Pool
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10.*
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memAlloc
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

class DesktopAudio : Audio, Updateable, Disposable {
    private class Source : Disposable {
        val handle: Int
        val state get() = alGetSourcei(handle, AL_SOURCE_STATE)

        init {
            val handle = alGenSources()
            this.handle = if (alGetError() == AL_NO_ERROR)
                handle
            else
                -1
        }

        override fun dispose() {
            if (handle != -1)
                alDeleteSources(handle)
        }
    }

    private class Buffer : Disposable {
        val handle = alGenBuffers()

        fun setData(data: ByteArray, count: Int, isBigEndian: Boolean, format: Int, sampleRate: Int) {
            val buffer = memAlloc(count)
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
            memFree(buffer)
        }

        override fun dispose() {
            alDeleteBuffers(handle)
        }
    }

    var noDevice = false
        private set

    private var device = 0L
    private var context = 0L
    private val sources = Pool(supplier = { Source() })
    private val activeSources = arrayListOf<Source>()
    private val buffers = arrayListOf<Buffer>()
    private val freeBuffers = DynamicStack<Int>()

    override var listener = AudioListener()

    override val supportedSoundFormats = arrayOf("wav", "mp3").asIterable()

    init {
        device = alcOpenDevice(null as ByteBuffer?)

        if (device == 0L)
            Kore.log.error(this::class, "Failed to open OpenAL device")
        else {
            val deviceCapabilities = ALC.createCapabilities(device)
            context = alcCreateContext(device, null as IntBuffer?)

            if (context == 0L) {
                alcCloseDevice(device)
                Kore.log.error(this::class, "Failed to create OpenAL context")
                noDevice = true
            }

            if (!alcMakeContextCurrent(context)) {
                alcDestroyContext(context)
                alcCloseDevice(device)
                Kore.log.error(this::class, "Failed to make OpenAL context current")
                noDevice = true
            }

            if (!noDevice)
                AL.createCapabilities(deviceCapabilities)
        }
    }

    override fun readSound(stream: ReadStream, format: String): Sound? {
        if (noDevice)
            return null

        if (format !in supportedSoundFormats) {
            Kore.log.error(this::class, "Unsupported audio format: $format")
            return null
        }

        val audioStream = when (format.lowercase()) {
            "wav" -> WAV.createStream((stream as DesktopReadStream).stream)
            "mp3" -> MP3.createStream((stream as DesktopReadStream).stream)
            else -> {
                Kore.log.error(this::class, "Unable to read audio data")
                return null
            }
        }

        audioStream.use {
            val sampleFormat = when (it.sampleSize) {
                8 -> when (it.channels) {
                    1 -> AL_FORMAT_MONO8
                    2 -> AL_FORMAT_STEREO8
                    else -> return null
                }
                16 -> when (it.channels) {
                    1 -> AL_FORMAT_MONO16
                    2 -> AL_FORMAT_STEREO16
                    else -> return null
                }
                else -> return null
            }

            val data = ByteArray(it.remaining)
            val count = it.read(data)

            return DesktopSound(registerSoundData(data, count, it.isBigEndian, sampleFormat, it.sampleRate))
        }
    }

    override fun update(delta: Float) {
        if (noDevice)
            return

        stackPush().use {
            alListenerfv(AL_POSITION, it.floats(listener.position.x, listener.position.y, listener.position.z))
            alListenerfv(AL_ORIENTATION, it.floats(listener.direction.x, listener.direction.y, listener.direction.z, listener.up.x, listener.up.y, listener.up.z))
            alListenerfv(AL_VELOCITY, it.floats(listener.velocity.x, listener.velocity.y, listener.velocity.z))
        }

        with(activeSources.iterator()) {
            while (hasNext()) {
                val source = next()

                if (source.state != AL_PLAYING) {
                    remove()
                    sources.free(source)
                }
            }
        }
    }

    private fun obtainSource(): Source? {
        val source = sources.obtain()
        if (source.handle == -1)
            return null

        activeSources += source
        return source
    }

    private fun obtainBufferHandle(): Int {
        if (!freeBuffers.isEmpty)
            return freeBuffers.pop()

        val buffer = Buffer()
        buffers += buffer
        return buffers.size - 1
    }

    fun freeBufferHandle(handle: Int) {
        freeBuffers.push(handle)
    }

    fun registerSoundData(data: ByteArray, count: Int, isBigEndian: Boolean, format: Int, sampleRate: Int): Int {
        val buffer = obtainBufferHandle()
        buffers[buffer].setData(data, count, isBigEndian, format, sampleRate)
        return buffer
    }

    override fun play(sound: Sound, volume: Float, loop: Boolean): AudioPlayer {
        val source = obtainSource() ?: return DesktopAudioPlayer(-1, 1.0f)
        alSourcei(source.handle, AL_BUFFER, buffers[(sound as DesktopSound).buffer].handle)
        alSourcef(source.handle, AL_GAIN, volume)
        alSourcei(source.handle, AL_LOOPING, if (loop) AL_TRUE else AL_FALSE)
        alSourcePlay(source.handle)
        return DesktopAudioPlayer(source.handle, volume)
    }

    override fun dispose() {
        if (noDevice)
            return

        sources.dispose()
        buffers.forEach {
            it.dispose()
        }

        alcDestroyContext(context)
        alcCloseDevice(device)
    }
}
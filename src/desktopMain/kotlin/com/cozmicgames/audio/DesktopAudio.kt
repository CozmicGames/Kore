package com.cozmicgames.audio

import com.cozmicgames.*
import com.cozmicgames.audio.formats.MP3
import com.cozmicgames.audio.formats.WAV
import com.cozmicgames.files.DesktopReadStream
import com.cozmicgames.files.ReadStream
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.Updateable
import com.cozmicgames.utils.collections.Pool
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10.*
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10.*
import org.lwjgl.system.MemoryStack.stackPush
import java.nio.ByteBuffer
import java.nio.IntBuffer

class DesktopAudio : Audio, Updateable, Disposable {
    var noDevice = false
        private set

    private var device = 0L
    private var context = 0L
    private val sources = Pool(supplier = { AudioSource() })
    private val activeSources = arrayListOf<AudioSource>()
    private val buffers = Pool(supplier = { AudioBuffer() })

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

        return DesktopSound(LoadedAudioData(audioStream))
        //return DesktopSound(if (audioStream.remaining >= Kore.configuration.audioStreamThreshold) StreamedAudioData(audioStream) else LoadedAudioData(audioStream))
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

                if (!source.update()) {
                    remove()
                    sources.free(source)
                }
            }
        }
    }

    private fun obtainSource(): AudioSource? {
        val source = sources.obtain()
        if (source.handle == -1)
            return null

        activeSources += source
        return source
    }

    fun obtainBuffer(): AudioBuffer {
        return buffers.obtain()
    }

    fun freeBuffer(buffer: AudioBuffer) {
        buffers.free(buffer)
    }

    override fun play(sound: Sound, volume: Float, loop: Boolean): AudioPlayer {
        val source = obtainSource() ?: return DesktopAudioPlayer(-1, 1.0f)
        alSourcef(source.handle, AL_GAIN, volume)
        alSourcei(source.handle, AL_LOOPING, if (loop) AL_TRUE else AL_FALSE)

        source.setData((sound as DesktopSound).data)

        alSourcePlay(source.handle)

        return DesktopAudioPlayer(source.handle, volume)
    }

    override fun dispose() {
        if (noDevice)
            return

        sources.dispose()
        buffers.dispose()

        alcDestroyContext(context)
        alcCloseDevice(device)
    }
}

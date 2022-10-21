package com.cozmicgames.audio.formats

import fr.delthas.javamp3.Sound
import java.io.InputStream

object MP3 : AudioFormat {
    class MP3AudioStream(stream: InputStream) : AudioStream {
        private val sound = Sound(stream)

        override val sampleSize = sound.audioFormat.sampleSizeInBits
        override val channels = sound.audioFormat.channels
        override val sampleRate = sound.audioFormat.sampleRate.toInt()
        override val isBigEndian = sound.audioFormat.isBigEndian
        override val remaining get() = sound.available()

        override fun read(buffer: ByteArray): Int {
            return sound.read(buffer)
        }

        override fun close() {
            sound.close()
        }
    }

    override fun createStream(stream: InputStream) = MP3AudioStream(stream)
}
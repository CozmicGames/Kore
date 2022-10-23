package com.cozmicgames.audio.formats

import com.cozmicgames.files.DesktopReadStream
import com.cozmicgames.files.FileHandle
import fr.delthas.javamp3.Sound

object MP3 : AudioFormat {
    class MP3AudioStream(private val file: FileHandle) : AudioStream {
        private var sound = Sound((file.read() as DesktopReadStream).stream)

        override val sampleSize = sound.audioFormat.sampleSizeInBits
        override val channels = sound.audioFormat.channels
        override val sampleRate = sound.audioFormat.sampleRate.toInt()
        override val isBigEndian = sound.audioFormat.isBigEndian
        override val remaining get() = sound.available()

        override fun read(buffer: ByteArray): Int {
            return sound.read(buffer)
        }

        override fun reset() {
            sound.close()
            sound = Sound((file.read() as DesktopReadStream).stream)
        }

        override fun dispose() {
            sound.close()
        }
    }

    override fun createStream(file: FileHandle) = MP3AudioStream(file)
}
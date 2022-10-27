package com.cozmicgames.audio.formats

import com.cozmicgames.files.DesktopReadStream
import com.cozmicgames.files.FileHandle
import com.cozmicgames.utils.use
import fr.delthas.javamp3.Sound

object MP3 : AudioFormat {
    class MP3AudioFile(private val file: FileHandle) : AudioFile {
        override var sampleSize = 0
        override var channels = 0
        override var sampleRate = 0
        override var isBigEndian = false
        override var size = 0

        init {
            file.read().use {
                val sound = Sound((it as DesktopReadStream).stream)
                sampleSize = sound.audioFormat.sampleSizeInBits
                channels = sound.audioFormat.channels
                sampleRate = sound.audioFormat.sampleRate.toInt()
                isBigEndian = sound.audioFormat.isBigEndian
                size = sound.available()
            }
        }

        override fun readFully(buffer: ByteArray): Int {
            file.read().use {
                val sound = Sound((it as DesktopReadStream).stream)
                return sound.read(buffer)
            }
        }

        override fun openStream() = object : AudioStream {
            var sound = Sound((file.read() as DesktopReadStream).stream)

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
    }

    override fun createFile(file: FileHandle) = MP3AudioFile(file)
}
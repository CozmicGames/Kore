package com.cozmicgames.audio.formats

import com.cozmicgames.files.DesktopReadStream
import com.cozmicgames.files.FileHandle
import com.cozmicgames.utils.use
import javax.sound.sampled.AudioSystem

object WAV : AudioFormat {
    class WAVAudioFile(private val file: FileHandle) : AudioFile {
        override var sampleSize = 0
        override var channels = 0
        override var sampleRate = 0
        override var isBigEndian = false
        override var size = 0

        init {
            file.read().use {
                val fileFormat = AudioSystem.getAudioFileFormat((it as DesktopReadStream).stream)
                sampleSize = fileFormat.format.sampleSizeInBits
                channels = fileFormat.format.channels
                sampleRate = fileFormat.format.sampleRate.toInt()
                isBigEndian = fileFormat.format.isBigEndian
                size = fileFormat.byteLength
            }
        }

        override fun readFully(buffer: ByteArray): Int {
            file.read().use {
                val audioInputStream = AudioSystem.getAudioInputStream((it as DesktopReadStream).stream)
                return audioInputStream.read(buffer)
            }
        }

        override fun openStream() = object : AudioStream {
            var audioStream = AudioSystem.getAudioInputStream((file.read() as DesktopReadStream).stream)

            override fun read(buffer: ByteArray): Int {
                return audioStream.read(buffer)
            }

            override fun reset() {
                audioStream.close()
                audioStream = AudioSystem.getAudioInputStream((file.read() as DesktopReadStream).stream)
            }

            override fun dispose() {
                audioStream.close()
            }
        }
    }

    override fun createFile(file: FileHandle) = WAVAudioFile(file)
}
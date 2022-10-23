package com.cozmicgames.audio.formats

import com.cozmicgames.files.DesktopReadStream
import com.cozmicgames.files.FileHandle
import javax.sound.sampled.AudioSystem

object WAV : AudioFormat {
    class WAVAudioStream(private val file: FileHandle) : AudioStream {
        private var audioInputStream = AudioSystem.getAudioInputStream((file.read() as DesktopReadStream).stream)

        override val sampleSize = audioInputStream.format.sampleSizeInBits
        override val channels = audioInputStream.format.channels
        override val sampleRate = audioInputStream.format.sampleRate.toInt()
        override val isBigEndian = audioInputStream.format.isBigEndian
        override val remaining get() = audioInputStream.available()

        override fun read(buffer: ByteArray): Int {
            return audioInputStream.read(buffer)
        }

        override fun reset() {
            audioInputStream.close()
            audioInputStream = AudioSystem.getAudioInputStream((file.read() as DesktopReadStream).stream)
        }

        override fun dispose() {
            audioInputStream.close()
        }
    }

    override fun createStream(file: FileHandle) = WAVAudioStream(file)
}
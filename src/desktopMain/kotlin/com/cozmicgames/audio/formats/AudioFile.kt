package com.cozmicgames.audio.formats

interface AudioFile {
    val sampleSize: Int
    val channels: Int
    val sampleRate: Int
    val isBigEndian: Boolean
    val size: Int

    fun readFully(buffer: ByteArray): Int

    fun openStream(): AudioStream
}
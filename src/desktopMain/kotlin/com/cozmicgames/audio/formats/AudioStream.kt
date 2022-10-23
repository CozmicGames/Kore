package com.cozmicgames.audio.formats

import com.cozmicgames.utils.Disposable

interface AudioStream : Disposable {
    val sampleSize: Int
    val channels: Int
    val sampleRate: Int
    val isBigEndian: Boolean
    val remaining: Int

    fun read(buffer: ByteArray): Int

    fun reset()
}
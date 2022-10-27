package com.cozmicgames.audio.formats

import com.cozmicgames.utils.Disposable

interface AudioStream : Disposable {
    fun read(buffer: ByteArray): Int

    fun reset()
}
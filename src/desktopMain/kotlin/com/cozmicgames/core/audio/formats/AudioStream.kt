package com.cozmicgames.core.audio.formats

import com.cozmicgames.core.utils.Disposable

interface AudioStream : Disposable {
    fun read(buffer: ByteArray): Int

    fun reset()
}
package com.cozmicgames.audio.formats

import com.cozmicgames.files.FileHandle

interface AudioFormat {
    fun createStream(file: FileHandle): AudioStream
}
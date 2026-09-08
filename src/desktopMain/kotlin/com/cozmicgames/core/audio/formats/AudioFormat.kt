package com.cozmicgames.core.audio.formats

import com.cozmicgames.core.files.FileHandle

interface AudioFormat {
    fun createFile(file: FileHandle): AudioFile
}
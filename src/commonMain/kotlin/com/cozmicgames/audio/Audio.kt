package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.files.Files
import com.cozmicgames.files.ReadStream
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.extensions.extension

interface Audio : Disposable {
    val supportedSoundFormats: Iterable<String>

    fun readSound(stream: ReadStream, format: String): Sound?
    fun play(sound: Sound, volume: Float, loop: Boolean = false): AudioPlayer
}

fun Audio.loadSound(file: String, type: Files.Type) = readSound(
    when (type) {
        Files.Type.ASSET -> Kore.files.readAsset(file)
        Files.Type.RESOURCE -> Kore.files.readAsset(file)
    }, file.extension
)

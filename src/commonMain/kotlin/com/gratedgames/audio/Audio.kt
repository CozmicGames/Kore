package com.gratedgames.audio

import com.gratedgames.Kore
import com.gratedgames.files
import com.gratedgames.files.Files
import com.gratedgames.files.ReadStream
import com.gratedgames.utils.Disposable
import com.gratedgames.utils.extensions.extension

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

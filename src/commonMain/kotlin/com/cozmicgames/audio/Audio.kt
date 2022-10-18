package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.files.ReadStream
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.extension
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.use

/**
 * [Audio] is the framework module for reading and playing audio.
 * It must be implemented by the platform specific implementation and bound to [Kore.context].
 */
interface Audio : Disposable {
    /**
     * The current listener.
     */
    var listener: AudioListener

    /**
     * The supported audio formats
     */
    val supportedSoundFormats: Iterable<String>

    /**
     * Loads audio from the given [stream] in the given [format].
     *
     * @param stream The stream to load the audio from
     * @param format The format of the audio
     *
     * @return The loaded audio or null if the audio could not be loaded
     */
    fun readSound(stream: ReadStream, format: String): Sound?

    /**
     * Plays the given [sound] at the given [volume].
     * If [loop] is true, the sound will loop.
     *
     * @param sound The sound to play
     * @param volume The volume of the sound
     * @param loop Whether the sound should loop
     *
     * @return An [AudioPlayer] that can be used to control the sound
     */
    fun play(sound: Sound, volume: Float, loop: Boolean = false): AudioPlayer
}

/**
 * Loads the audio from the given [file].
 *
 * @param file The file handle to load the audio from
 *
 * @return The loaded audio or null if the audio could not be loaded.
 */
fun Audio.loadSound(file: FileHandle) = file.read().use {
    readSound(it, file.extension)
}

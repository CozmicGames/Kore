package com.cozmicgames.audio

import com.cozmicgames.Kore
import com.cozmicgames.files.FileHandle
import com.cozmicgames.utils.Disposable

/**
 * [Audio] is the framework module for reading and playing audio.
 * It must be implemented by the platform specific implementation and bound to [Kore.context].
 */
interface Audio : Disposable {
    /**
     * The current listener.
     * Defaults to a listener positioned at [0, 0, 0], facing the positive z-direction.
     */
    var listener: AudioListener

    /**
     * The supported audio formats
     */
    val supportedSoundFormats: Iterable<String>

    /**
     * Loads the audio from the given [file].
     *
     * @param file The file handle to load the audio from
     *
     * @return The loaded audio or null if the audio could not be loaded.
     */
    fun readSound(file: FileHandle): Sound?

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

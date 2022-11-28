package com.cozmicgames.files

import com.cozmicgames.Kore

/**
 * [Files] is the framework module for reading and writing files.
 * It must be implemented by the platform specific implementation and bound to [Kore.context].
 */
interface Files {
    /**
     * The type a file handle can be.
     * ASSET: An asset file. On Desktop, the applications' root directory is used. If the file is not found there, the classpath will be used. Asset files are always read-only.
     * LOCAL: A local file. On Desktop, the applications' root directory is used. Local files can be read-write.
     * EXTERNAL: An external file. On Desktop, the users' home directory is used. External files can be read-write.
     * ABSOLUTE: An absolute file. Absolute files can be read-write.
     * ZIP: A zip file. This represents a file inside a zip archive.
     */
    enum class Type {
        ASSET,
        LOCAL,
        EXTERNAL,
        ABSOLUTE,
        ZIP
    }

    /**
     * Describes the endianness of a stream.
     * [LITTLE_ENDIAN]: The stream is little endian.
     * [BIG_ENDIAN]: The stream is big endian.
     */
    enum class Endianness {
        LITTLE_ENDIAN,
        BIG_ENDIAN
    }

    /**
     * The native endianness of the current platform.
     */
    val nativeEndianness: Endianness

    /**
     * Creates a new asset file handle.
     */
    fun asset(path: String): FileHandle

    /**
     * Creates a new local file handle.
     */
    fun local(path: String): FileHandle

    /**
     * Creates a new external file handle.
     */
    fun external(path: String): FileHandle

    /**
     * Creates a new absolute file handle.
     */
    fun absolute(path: String): FileHandle
}
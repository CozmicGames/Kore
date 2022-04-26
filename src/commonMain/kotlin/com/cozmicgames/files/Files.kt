package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.Charsets
import com.cozmicgames.utils.use

/**
 * [Files] is the framework module for reading and writing files.
 * It must be implemented by the platform specific implementation and bound to [Kore.context].
 */
interface Files {
    /**
     * Describes file types.
     * [ASSET]: A file that is loaded as an asset. This is read only.
     * [RESOURCE]: A file that is loaded as a resource. This is read and write.
     */
    enum class Type {
        ASSET,
        RESOURCE
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
     * The default file separator of the current platform.
     */
    val separator: String

    /**
     * The native endianness of the current platform.
     */
    val nativeEndianness: Endianness

    /**
     * Lists all files in the given directory.
     *
     * @param file The directory to list.
     * @param type The type of [file].
     * @param block The block to execute for each file.
     */
    fun list(file: String, type: Type, block: (String) -> Unit)

    /**
     * Check if the given file exists.
     *
     * @param file The directory to list.
     * @param type The type of [file].
     *
     * @return True if the file exists, false otherwise.
     */
    fun exists(file: String, type: Type): Boolean

    /**
     * Deletes the given resource file.
     *
     * @param file The file to delete.
     */
    fun deleteResource(file: String)

    /**
     * Reads the given asset file.
     *
     * @param file The file to read.
     *
     * @return A stream to read the files' contents from.
     */
    fun readAsset(file: String): ReadStream

    /**
     * Reads the given resource file.
     *
     * @param file The file to read.
     *
     * @return A stream to read the files' contents from.
     */
    fun readResource(file: String): ReadStream

    /**
     * Writes the given resource file.
     *
     * @param file The file to write.
     *
     * @return A stream to write the files' contents to.
     */
    fun writeResource(file: String, append: Boolean): WriteStream
}

/**
 * Resolves a relative file path.
 * The path is relative to [from]. It can go up a directory by using "." or "..".
 *
 * @param from The path to resolve.
 * @param file The path to resolve to.
 *
 * @return The resolved path.
 */
fun Files.resolve(from: String, file: String): String {
    var path = from
    for (part in file.split(separator)) {
        if (part == "." || part == "..")
            path = path.substring(0, path.lastIndexOf(separator))
        else
            path += separator + part
    }
    return path
}

/**
 * Reads the given file with the given [type] and [charset] to a string.
 *
 * @param file The file to read.
 * @param type The type of [file].
 * @param charset The charset to use. Defaults to UTF-8.
 *
 * @return The contents of the file as a string.
 */
fun Files.readToString(file: String, type: Files.Type, charset: Charset = Charsets.UTF8) = when (type) {
    Files.Type.RESOURCE -> readResource(file)
    Files.Type.ASSET -> readAsset(file)
}.use {
    it.readString(charset)
}

/**
 * Reads the given file with the given [type]to a byte array.
 *
 * @param file The file to read.
 * @param type The type of [file].
 *
 * @return The contents of the file as a byte array.
 */
fun Files.readToBytes(file: String, type: Files.Type) = when (type) {
    Files.Type.RESOURCE -> readResource(file)
    Files.Type.ASSET -> readAsset(file)
}.use {
    it.readAllBytes()
}

/**
 * Builds a file path from the given parts using the current platform's file separator.
 *
 * @param parts The parts of the file path.
 *
 * @return The file path.
 */
fun buildFilePath(vararg parts: String) = parts.joinToString(Kore.files.separator)

/**
 * Convenience function to concatenate strings to a file path using the current platform's file separator.
 */
operator fun String.div(part: String) = buildFilePath(this, part)


package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.Charsets
import com.cozmicgames.utils.extensions.directory
import com.cozmicgames.utils.extensions.extension
import com.cozmicgames.utils.extensions.nameWithExtension
import com.cozmicgames.utils.extensions.nameWithoutExtension
import com.cozmicgames.utils.use

interface FileHandle {
    /**
     * The type of this file.
     * @see Files.Type
     */
    val type: Files.Type

    /**
     * The full path of this file.
     */
    val fullPath: String

    /**
     * Whether this file is writable.
     */
    val isWritable: Boolean

    /**
     * Whether this file exists.
     */
    val exists: Boolean

    /**
     * The size of this file.
     */
    val size: Long

    /**
     * The last modified time of this file, in milliseconds.
     */
    val lastModified: Long

    /**
     * Lists the contents of this file handle, if it is a directory.
     */
    fun list(block: (String) -> Unit)

    /**
     * Delete this file. If this is a directory, all sub-files and sub-directories will be deleted as well.
     */
    fun delete()

    /**
     * Creates a [ReadStream] for this file.
     */
    fun read(): ReadStream

    /**
     * Creates a [WriteStream] for this file. This requires [isWritable] to be true.
     *
     * @param append Whether to append to the file if it already exists. If false, the file will be overwritten.
     */
    fun write(append: Boolean): WriteStream

    /**
     * Creates a child file handle for this file.
     *
     * @param path The path to the child file.
     */
    fun child(path: String): FileHandle

    /**
     * Creates a sibling file handle for this file.
     *
     * @param path The path to the sibling file.
     */
    fun sibling(path: String): FileHandle

    /**
     * Creates a parent file handle for this file.
     */
    fun parent(): FileHandle

    /**
     * Copies this file to the specified file handle.
     *
     * @param file The file handle to copy to.
     */
    fun copyTo(file: FileHandle)

    /**
     * Moves this file to the specified file handle.
     *
     * @param file The file handle to move to.
     */
    fun moveTo(file: FileHandle)

    /**
     * Opens this as a zip archive, if it is one.
     */
    fun openZip(): ZipArchive?

    /**
     * Builds a zip archive and saves it to this file handle on [ZipBuilder.finish].
     */
    fun buildZip(): ZipBuilder

    override fun toString(): String

    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean
}

/**
 * Whether this file handle is a directory.
 */
val FileHandle.isDirectory get() = fullPath.endsWith(Kore.files.separator)

/**
 * Whether this file handle is a file.
 */
val FileHandle.isFile get() = !isDirectory

/**
 * The extension of this file handle.
 */
val FileHandle.extension get() = fullPath.extension

/**
 * The name of this file handle, with extension.
 */
val FileHandle.nameWithExtension get() = fullPath.nameWithExtension

/**
 * The name of this file handle, without extension.
 */
val FileHandle.nameWithoutExtension get() = fullPath.nameWithoutExtension

/**
 * The directory of this file handle.
 */
val FileHandle.directory get() = fullPath.directory

/**
 * Reads the contents of this file handle as a string.
 */
fun FileHandle.readToString(charset: Charset = Charsets.UTF8) = read().use {
    it.readString(charset)
}

/**
 * Reads the contents of this file handle as a [ByteArray].
 */
fun FileHandle.readToBytes() = read().use {
    it.readAllBytes()
}

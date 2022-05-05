package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.log
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.Charsets
import com.cozmicgames.utils.use

class FileHandle(val path: String, val type: Files.Type) {
    /**
     * True if the file can be written to.
     */
    val isWritable get() = type == Files.Type.RESOURCE

    /**
     * Check if the given file exists.
     */
    val exists get() = Kore.files.exists(path, type)

    fun list(block: (String) -> Unit) = Kore.files.list(path, type, block)

    /**
     * Deletes the file.
     * Only works if [type] is [Files.Type.RESOURCE].
     */
    fun delete() {
        if (type == Files.Type.ASSET) {
            Kore.log.error(this::class, "Cannot delete asset file $path")
            return
        }

        Kore.files.deleteResource(path)
    }

    /**
     * Reads the file.
     *
     * @return A stream to read the files' contents from.
     */
    fun read() = when (type) {
        Files.Type.ASSET -> Kore.files.readAsset(path)
        Files.Type.RESOURCE -> Kore.files.readResource(path)
    }

    /**
     * Writes to the file.
     * Only works if [type] is [Files.Type.RESOURCE].
     *
     * @return A stream to write the files' contents to.
     */
    fun write(append: Boolean): WriteStream? {
        if (type == Files.Type.ASSET) {
            Kore.log.error(this::class, "Cannot write to asset file $path")
            return null
        }

        return Kore.files.writeResource(path, append)
    }

    override fun toString(): String {
        return "FileHandle(path='$path', type=$type)"
    }

    override fun hashCode(): Int {
        return path.hashCode() + type.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is FileHandle) return false

        return other.path == path && other.type == type
    }
}

fun Files.resource(path: String): FileHandle {
    return FileHandle(path, Files.Type.RESOURCE)
}

fun Files.asset(path: String): FileHandle {
    return FileHandle(path, Files.Type.ASSET)
}

fun FileHandle.readToString(charset: Charset = Charsets.UTF8) = read().use {
    it.readString(charset)
}

fun FileHandle.readToBytes() = read().use {
    it.readAllBytes()
}

package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.Charsets
import com.cozmicgames.utils.use

fun buildFilePath(vararg parts: String) = parts.joinToString(Kore.files.separator)

operator fun String.div(part: String) = buildFilePath(this, part)

interface Files {
    enum class Type {
        ASSET, RESOURCE
    }

    enum class Endianness {
        LITTLE_ENDIAN, BIG_ENDIAN
    }

    val separator: String
    val nativeEndianness: Endianness

    fun list(file: String, type: Type, block: (String) -> Unit)
    fun exists(file: String, type: Type): Boolean
    fun deleteResource(file: String)
    fun readAsset(file: String): ReadStream
    fun readResource(file: String): ReadStream
    fun writeResource(file: String, append: Boolean): WriteStream
}

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

fun Files.readToString(file: String, type: Files.Type, charset: Charset = Charsets.UTF8) = when (type) {
    Files.Type.RESOURCE -> readResource(file)
    Files.Type.ASSET -> readAsset(file)
}.use {
    it.readString(charset)
}

fun Files.readToBytes(file: String, type: Files.Type) = when (type) {
    Files.Type.RESOURCE -> readResource(file)
    Files.Type.ASSET -> readAsset(file)
}.use {
    it.readAllBytes()
}

package com.cozmicgames.core.files

interface ZipBuilder {
    /**
     * Adds a file to the zip archive.
     *
     * @param path The path of the file within the zip archive.
     * @param data The byte array representing the contents of the file.
     */
    fun addFile(path: String, data: ByteArray): ZipBuilder

    /**
     * Finishes the zip archive.
     */
    fun finish()
}

fun ZipBuilder.write(path: String, block: (WriteStream) -> Unit) {
    val stream = ByteArrayWriteStream()
    block(stream)
    addFile(path, stream.toByteArray())
}

fun ZipBuilder.writeFile(file: FileHandle, path: String = file.fullPath) {
    addFile(path, file.readToBytes())
}

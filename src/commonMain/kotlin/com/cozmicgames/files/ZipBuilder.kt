package com.cozmicgames.files

interface ZipBuilder {
    fun addFile(path: String, data: ByteArray): ZipBuilder
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

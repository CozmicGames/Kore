package com.cozmicgames.files

interface ZipArchive {
    fun list(block: (FileHandle) -> Unit)

    operator fun get(path: String): FileHandle?
}
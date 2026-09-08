package com.cozmicgames.core.files

interface ZipArchive {
    /**
     * Lists all the files in the zip archive and executes the given [block] for each file.
     *
     * @param block The block to execute for each file.
     */
    fun list(block: (FileHandle) -> Unit)

    /**
     * Returns the [FileHandle] for the file at the given [path] in the zip archive, or null if the file does not exist.
     *
     * @param path The path of the file in the zip archive.
     * @return The [FileHandle] for the file at the given [path], or null if the file does not exist.
     */
    operator fun get(path: String): FileHandle?
}
package com.cozmicgames.files

import java.util.zip.ZipFile

class DesktopZipArchive(val file: ZipFile) : ZipArchive {
    override fun list(block: (FileHandle) -> Unit) {
        file.stream().forEach {
            block(DesktopZipFileHandle(file, it))
        }
    }

    override fun get(path: String): FileHandle? {
        val entry = file.getEntry(path) ?: return null
        return DesktopZipFileHandle(file, entry)
    }
}

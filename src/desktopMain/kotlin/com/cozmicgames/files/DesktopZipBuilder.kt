package com.cozmicgames.files

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DesktopZipBuilder(private val file: File) : ZipBuilder {
    private class Entry(val name: String, val data: ByteArray)

    private val entries = arrayListOf<Entry>()

    override fun addFile(path: String, data: ByteArray): ZipBuilder {
        entries.add(Entry(path, data))
        return this
    }

    override fun finish() {
        val zipFile = File(file.absolutePath + ".zip")
        val zip = ZipOutputStream(zipFile.outputStream())
        for (entry in entries) {
            zip.putNextEntry(ZipEntry(entry.name))
            zip.write(entry.data)
            zip.closeEntry()
        }
        zip.close()
    }
}
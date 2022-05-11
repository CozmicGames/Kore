package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.log
import java.io.File
import java.nio.ByteOrder
import java.util.zip.ZipFile


class DesktopFiles : Files {
    companion object {
        private val externalPath = System.getProperty("user.home") + File.separator
        private val localPath = File("").absolutePath + File.separator
    }

    override val separator get() = File.separator

    override val nativeEndianness = when (ByteOrder.nativeOrder()) {
        ByteOrder.LITTLE_ENDIAN -> Files.Endianness.LITTLE_ENDIAN
        ByteOrder.BIG_ENDIAN -> Files.Endianness.BIG_ENDIAN
        else -> throw Exception()
    }

    override fun asset(path: String): FileHandle {
        return DesktopAssetFileHandle(path)
    }

    override fun local(path: String): FileHandle {
        return DesktopFileHandle("$localPath$separator$path", Files.Type.LOCAL)
    }

    override fun external(path: String): FileHandle {
        return DesktopFileHandle("$externalPath$separator$path", Files.Type.EXTERNAL)
    }

    override fun absolute(path: String): FileHandle {
        return DesktopFileHandle(path, Files.Type.EXTERNAL)
    }
}

package com.cozmicgames.files

import java.io.File
import java.nio.ByteOrder


class DesktopFiles : Files {
    companion object {
        private val externalPath = System.getProperty("user.home") + File.separator
        private val localPath = File("").absolutePath + File.separator
    }

    override val nativeEndianness = when (ByteOrder.nativeOrder()) {
        ByteOrder.LITTLE_ENDIAN -> Files.Endianness.LITTLE_ENDIAN
        ByteOrder.BIG_ENDIAN -> Files.Endianness.BIG_ENDIAN
        else -> throw Exception()
    }

    override fun asset(path: String): FileHandle {
        return DesktopAssetFileHandle(path)
    }

    override fun local(path: String): FileHandle {
        return DesktopFileHandle("$localPath/$path", Files.Type.LOCAL)
    }

    override fun external(path: String): FileHandle {
        return DesktopFileHandle("$externalPath/$path", Files.Type.EXTERNAL)
    }

    override fun absolute(path: String): FileHandle {
        return DesktopFileHandle(path, Files.Type.EXTERNAL)
    }
}

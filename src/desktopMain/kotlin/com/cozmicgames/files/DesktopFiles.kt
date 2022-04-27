package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.application
import com.cozmicgames.log
import java.io.*
import java.nio.ByteOrder
import java.util.zip.ZipInputStream


class DesktopFiles: Files {
    companion object {
        fun getAssetURL(file: String) = Kore::class.java.getResource("/$file")
    }

    override val separator get() = File.pathSeparator

    override val nativeEndianness = when (ByteOrder.nativeOrder()) {
        ByteOrder.LITTLE_ENDIAN -> Files.Endianness.LITTLE_ENDIAN
        ByteOrder.BIG_ENDIAN -> Files.Endianness.BIG_ENDIAN
        else -> throw Exception()
    }

    override fun list(file: String, type: Files.Type, block: (String) -> Unit) {
        when (type) {
            Files.Type.ASSET -> {
                fun listInDirectory(zip: ZipInputStream) {
                    while (true) {
                        val entry = zip.nextEntry ?: break

                        if (!entry.isDirectory && entry.name.startsWith(file))
                            block(entry.name)
                    }
                }

                val jar = Kore.application.javaClass.protectionDomain.codeSource?.location ?: return
                val zip = ZipInputStream(jar.openStream())
                listInDirectory(zip)
                zip.close()
            }
            Files.Type.RESOURCE -> {
                fun listInDirectory(file: String) {
                    val f = File(file)
                    if (!f.exists() || !f.isDirectory)
                        return

                    f.list()?.forEach {
                        val ff = File(it)
                        if (ff.isDirectory)
                            listInDirectory(it)
                        else
                            block(ff.absolutePath)
                    }
                }

                listInDirectory(file)
            }
        }
    }

    override fun exists(file: String, type: Files.Type): Boolean {
        return when (type) {
            Files.Type.ASSET -> getAssetURL(file) != null
            Files.Type.RESOURCE -> File(file).exists()
        }
    }

    override fun deleteResource(file: String) {
        val f = File(file)
        if (f.exists())
            f.delete()
    }

    override fun readAsset(file: String): ReadStream {
        val url = getAssetURL(file)
        if (url == null)
            Kore.log.fail(this::class, "Asset file not found: $file")
        return DesktopReadStream(BufferedInputStream(url.openStream()))
    }

    override fun readResource(file: String): ReadStream {
        val f = File(file)
        if (!f.exists())
            Kore.log.fail(this::class, "Resource file not found: $file")
        return DesktopReadStream(BufferedInputStream(FileInputStream(f)))
    }

    override fun writeResource(file: String, append: Boolean): WriteStream {
        val f = File(file)
        if (!f.exists()) {
            f.parentFile?.mkdirs()
            f.createNewFile()
        }
        return DesktopWriteStream(BufferedOutputStream(FileOutputStream(f, append)))
    }
}

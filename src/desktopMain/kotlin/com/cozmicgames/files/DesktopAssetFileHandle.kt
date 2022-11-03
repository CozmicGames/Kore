package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.application
import com.cozmicgames.files
import com.cozmicgames.log
import com.cozmicgames.utils.extensions.directory
import java.io.*
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

class DesktopAssetFileHandle(override val fullPath: String) : FileHandle {
    companion object {
        private fun getAssetURL(file: String) = Kore::class.java.getResource("/$file")
    }

    private val url = getAssetURL(fullPath)

    override val type get() = Files.Type.ZIP

    override val exists get() = url != null

    override val isWritable get() = false

    override val size get() = requireNotNull(url).openStream().use { it.available() }.toLong()

    override val lastModified get() = 0L

    override fun list(block: (String) -> Unit) {
        fun listInDirectory(zip: ZipInputStream) {
            while (true) {
                val entry = zip.nextEntry ?: break

                if (entry.name.startsWith(fullPath))
                    block(entry.name)
            }
        }

        val jar = Kore.application.javaClass.protectionDomain.codeSource?.location ?: return
        val zip = ZipInputStream(jar.openStream())
        listInDirectory(zip)
        zip.close()
    }

    override fun delete() {
        Kore.log.fail(this::class, "Cannot delete $fullPath, it is an asset file")
    }

    override fun read(): ReadStream {
        if (url == null)
            Kore.log.fail(this::class, "Asset file not found: $fullPath")

        return DesktopReadStream(BufferedInputStream(requireNotNull(url).openStream()))
    }

    override fun write(append: Boolean): WriteStream {
        Kore.log.fail(this::class, "Cannot write to $fullPath, it is an asset file")
        throw UnsupportedOperationException()
    }

    override fun child(path: String): FileHandle {
        if (fullPath.isEmpty())
            return DesktopAssetFileHandle(path)

        return DesktopAssetFileHandle("$fullPath${Kore.files.separator}$path")
    }

    override fun sibling(path: String): FileHandle {
        if (fullPath.isEmpty())
            Kore.log.fail(this::class, "Cannot get a sibling of the root directory")

        return DesktopAssetFileHandle("${fullPath.directory}${Kore.files.separator}$path")
    }

    override fun parent(): FileHandle {
        var parent = fullPath.directory
        if (parent.isEmpty())
            parent = Kore.files.separator

        return DesktopAssetFileHandle(parent)
    }

    override fun copyTo(file: FileHandle) {
        fun copyFile(source: FileHandle, dest: FileHandle) {
            val sourceStream = source.read()
            val destStream = dest.write(false)
            destStream.writeBytes(sourceStream.readAllBytes())
            sourceStream.dispose()
            destStream.dispose()
        }

        fun copyDirectory(source: FileHandle, dest: FileHandle) {
            source.list {
                val sourceChild = source.child(it)
                val destChild = dest.child(it)

                if (sourceChild.isDirectory)
                    copyDirectory(sourceChild, destChild)
                else
                    copyFile(sourceChild, destChild)
            }
        }

        var dest = file

        if (!isDirectory) {
            if (dest.isDirectory)
                dest = dest.child(nameWithExtension)
            copyFile(this, dest)
            return
        }

        if (dest.exists) {
            if (!dest.isDirectory)
                Kore.log.fail(this::class, "Cannot copy to $dest, it is not a directory")
        }

        copyDirectory(this, dest.child(nameWithExtension))
    }

    override fun moveTo(file: FileHandle) {
        copyTo(file)
        delete()
    }

    override fun openZip(): ZipArchive {
        return DesktopZipArchive(ZipFile(File(url.toURI())))
    }

    override fun buildZip(): ZipBuilder {
        throw UnsupportedOperationException()
    }

    override fun toString(): String {
        return "FileHandle($fullPath, $type)"
    }

    override fun hashCode(): Int {
        return fullPath.hashCode() + type.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is FileHandle)
            return false

        return other.fullPath == fullPath && other.type == type
    }
}
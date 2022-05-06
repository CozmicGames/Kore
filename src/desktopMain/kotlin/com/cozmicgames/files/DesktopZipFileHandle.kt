package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.log
import com.cozmicgames.utils.extensions.directory
import java.io.*
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class DesktopZipFileHandle(private val zipFile: ZipFile, private val entry: ZipEntry) : FileHandle {
    override val fullPath get() = entry.name

    override val type get() = Files.Type.ZIP

    override val exists get() = true

    override val isWritable get() = false

    override val size get() = entry.size

    override val lastModified get() = entry.lastModifiedTime.to(TimeUnit.MILLISECONDS)

    override fun list(block: (String) -> Unit) {
        zipFile.stream().forEach {
            if (it.name.startsWith(fullPath)) {
                block(it.name.substring(fullPath.length))
            }
        }
    }

    override fun delete() {
        Kore.log.fail(this::class, "Cannot delete $fullPath, it is an asset file")
    }

    override fun read(): ReadStream {
        return DesktopReadStream(BufferedInputStream(zipFile.getInputStream(entry)))
    }

    override fun write(append: Boolean): WriteStream {
        Kore.log.fail(this::class, "Cannot write to $fullPath, it is an asset file")
        throw UnsupportedOperationException()
    }

    override fun child(path: String): FileHandle {
        if (fullPath.isEmpty())
            return DesktopZipFileHandle(zipFile, zipFile.getEntry(path))

        return DesktopZipFileHandle(zipFile, zipFile.getEntry("$fullPath${Kore.files.separator}$path"))
    }

    override fun sibling(path: String): FileHandle {
        if (fullPath.isEmpty())
            Kore.log.fail(this::class, "Cannot get a sibling of the root directory")

        return DesktopZipFileHandle(zipFile, zipFile.getEntry("${fullPath.directory}${Kore.files.separator}$path"))
    }

    override fun parent(): FileHandle {
        var parent = fullPath.directory
        if (parent.isEmpty())
            parent = Kore.files.separator

        return DesktopZipFileHandle(zipFile, zipFile.getEntry(parent))
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
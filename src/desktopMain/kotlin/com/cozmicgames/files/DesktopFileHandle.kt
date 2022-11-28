package com.cozmicgames.files

import com.cozmicgames.Kore
import com.cozmicgames.log
import java.io.*
import java.util.zip.ZipFile

class DesktopFileHandle(fullPath: String, override val type: Files.Type) : FileHandle {
    override val fullPath = fullPath
        get() = field.replace("\\", "/")

    internal val file = File(fullPath)

    override val exists get() = file.exists()

    override val isWritable get() = file.canWrite()

    override val size get() = file.length()

    override val lastModified get() = file.lastModified()

    override fun list(block: (String) -> Unit) {
        if (!file.exists() || !file.isDirectory)
            return

        file.list()?.forEach(block)
    }

    override fun delete() {
        if (!isWritable)
            Kore.log.fail(this::class, "Cannot delete $fullPath, it is not writable")

        if (exists)
            file.delete()
    }

    override fun read(): ReadStream {
        if (!file.exists())
            Kore.log.fail(this::class, "Resource file not found: $file")

        return DesktopReadStream(BufferedInputStream(FileInputStream(file)))
    }

    override fun write(append: Boolean): WriteStream {
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }

        return DesktopWriteStream(BufferedOutputStream(FileOutputStream(file, append)))
    }

    override fun child(path: String): FileHandle {
        if (fullPath.isEmpty())
            return DesktopFileHandle(path, type)

        return DesktopFileHandle(File(fullPath, path).absolutePath, type)
    }

    override fun sibling(path: String): FileHandle {
        if (fullPath.isEmpty())
            Kore.log.fail(this::class, "Cannot get a sibling of the root directory")

        return DesktopFileHandle(File(file.parent, path).absolutePath, type)
    }

    override fun parent(): FileHandle {
        var parentFile = file.parentFile
        if (parentFile == null)
            parentFile = File("/")

        return DesktopFileHandle(parentFile.absolutePath, type)
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
        if (file is DesktopFileHandle && this.file.renameTo(file.file))
            return

        copyTo(file)
        delete()
    }

    override fun openZip(): ZipArchive {
        return DesktopZipArchive(ZipFile(file))
    }

    override fun buildZip(): ZipBuilder {
        return DesktopZipBuilder(file)
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
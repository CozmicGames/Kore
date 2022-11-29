package com.cozmicgames.log

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.tinyfd.TinyFileDialogs
import org.lwjgl.util.tinyfd.TinyFileDialogs.*
import java.io.File
import java.io.FilenameFilter
import java.nio.ByteBuffer
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileFilter

class DesktopDialogs : Dialogs {
    override fun popup(title: String, message: String, type: Dialogs.Type) {
        tinyfd_notifyPopup(
            title, message, when (type) {
                Dialogs.Type.INFO -> "info"
                Dialogs.Type.WARNING -> "warning"
                Dialogs.Type.ERROR -> "error"
                Dialogs.Type.QUESTION -> "question"
            }
        )
    }

    override fun message(title: String, message: String, type: Dialogs.Type) {
        tinyfd_messageBox(
            title, message, "ok", when (type) {
                Dialogs.Type.INFO -> "info"
                Dialogs.Type.WARNING -> "warning"
                Dialogs.Type.ERROR -> "error"
                Dialogs.Type.QUESTION -> "question"
            }, true
        )
    }

    override fun confirm(title: String, message: String, optionType: Dialogs.OptionType, type: Dialogs.Type): Boolean {
        return tinyfd_messageBox(
            title, message, when (optionType) {
                Dialogs.OptionType.OK -> "ok"
                Dialogs.OptionType.OK_CANCEL -> "okcancel"
                Dialogs.OptionType.YES_NO -> "yesno"
                Dialogs.OptionType.YES_NO_CANCEL -> "yesnocancel"
            }, when (type) {
                Dialogs.Type.INFO -> "info"
                Dialogs.Type.WARNING -> "warning"
                Dialogs.Type.ERROR -> "error"
                Dialogs.Type.QUESTION -> "question"
            }, true
        )
    }

    override fun input(title: String, message: String): String? {
        return tinyfd_inputBox(title, message, "")
    }

    override fun save(title: String, default: String?, vararg filters: String): String? {
        return MemoryStack.stackPush().use {
            val filterBuffer = it.mallocPointer(filters.size)
            for (filter in filters) {
                filterBuffer.put(it.ASCII("*.$filter"))
            }
            tinyfd_saveFileDialog(title, default, filterBuffer.flip(), null)?.replace("\\", "/")
        }
    }

    override fun open(title: String, default: String?, vararg filters: String): String? {
        return MemoryStack.stackPush().use {
            val filterBuffer = it.mallocPointer(filters.size)
            for (filter in filters) {
                filterBuffer.put(it.ASCII("*.$filter"))
            }
            tinyfd_openFileDialog(title, default, filterBuffer.flip(), null, false)?.replace("\\", "/")
        }
    }

    override fun openMulti(title: String, default: String?, vararg filters: String): Array<String>? {
        return MemoryStack.stackPush().use {
            val filterBuffer = it.mallocPointer(filters.size)
            for (filter in filters) {
                filterBuffer.put(it.ASCII("*.$filter"))
            }
            tinyfd_openFileDialog(title, default, filterBuffer.flip(), null, true)?.split("|")?.map { it.replace("\\", "/") }?.toTypedArray()
        }
    }
}
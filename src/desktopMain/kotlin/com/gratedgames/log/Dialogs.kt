package com.gratedgames.log

import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileFilter

class DesktopDialogs : Dialogs {
    override fun message(title: String, message: String, type: Dialogs.Type) {
        JOptionPane.showMessageDialog(
            null, message, title, when (type) {
                Dialogs.Type.INFO -> JOptionPane.INFORMATION_MESSAGE
                Dialogs.Type.WARNING -> JOptionPane.WARNING_MESSAGE
                Dialogs.Type.ERROR -> JOptionPane.ERROR_MESSAGE
                Dialogs.Type.QUESTION -> JOptionPane.QUESTION_MESSAGE
            }
        )
    }

    override fun confirm(title: String, message: String, optionType: Dialogs.OptionType, type: Dialogs.Type): Boolean {
        return JOptionPane.showConfirmDialog(
            null, message, title, when (optionType) {
                Dialogs.OptionType.OK -> JOptionPane.OK_OPTION
                Dialogs.OptionType.OK_CANCEL -> JOptionPane.OK_CANCEL_OPTION
                Dialogs.OptionType.YES_NO -> JOptionPane.YES_NO_OPTION
                Dialogs.OptionType.YES_NO_CANCEL -> JOptionPane.YES_NO_CANCEL_OPTION
            }, when (type) {
                Dialogs.Type.INFO -> JOptionPane.INFORMATION_MESSAGE
                Dialogs.Type.WARNING -> JOptionPane.WARNING_MESSAGE
                Dialogs.Type.ERROR -> JOptionPane.ERROR_MESSAGE
                Dialogs.Type.QUESTION -> JOptionPane.QUESTION_MESSAGE
            }
        ) == when (optionType) {
            Dialogs.OptionType.YES_NO, Dialogs.OptionType.YES_NO_CANCEL -> JOptionPane.YES_OPTION
            Dialogs.OptionType.OK, Dialogs.OptionType.OK_CANCEL -> JOptionPane.OK_OPTION
        }
    }

    override fun input(title: String, message: String): String? {
        return JOptionPane.showInputDialog(null, message, title)
    }

    override fun save(default: String?, vararg filters: String): String? {
        val fc = JFileChooser()

        fc.fileFilter = object : FileFilter() {
            override fun accept(f: File?): Boolean {
                return f?.extension in filters
            }

            override fun getDescription(): String {
                return ""
            }
        }

        if (default != null)
            fc.selectedFile = File(default)

        return if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
            fc.selectedFile.absolutePath
        else null
    }

    override fun open(default: String?, vararg filters: String): String? {
        val fc = JFileChooser()

        fc.fileFilter = object : FileFilter() {
            override fun accept(f: File?): Boolean {
                return f?.extension in filters
            }

            override fun getDescription(): String {
                return ""
            }
        }

        if (default != null)
            fc.selectedFile = File(default)

        return if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            fc.selectedFile.absolutePath
        else null
    }
}
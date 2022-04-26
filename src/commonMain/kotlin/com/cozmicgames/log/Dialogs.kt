package com.cozmicgames.log

import com.cozmicgames.Kore

/**
 * [Dialogs] is the framework module for prompting dialogs.
 * It must be implemented by the platform specific implementation and bound to [Kore.context].
 */
interface Dialogs {
    /**
     * Describes different option types for dialogs.
     * OK: An OK button.
     * OK_CANCEL: An OK and Cancel button.
     * YES_NO: A Yes and No button.
     * YES_NO_CANCEL: A Yes, No and Cancel button.
     */
    enum class OptionType {
        OK,
        OK_CANCEL,
        YES_NO,
        YES_NO_CANCEL
    }

    /**
     * Describes different dialog types.
     * INFO: An information dialog.
     * WARNING: A warning dialog.
     * ERROR: An error dialog.
     * QUESTION: A question dialog.
     */
    enum class Type {
        INFO,
        WARNING,
        ERROR,
        QUESTION
    }

    /**
     * Shows a message dialog with the given title, message and option type.
     *
     * @param title The title of the dialog.
     * @param message The message of the dialog.
     * @param type The type of the dialog.
     *
     * @return The result of the dialog.
     */
    fun message(title: String, message: String, type: Type)

    /**
     * Shows a confirm dialog with the given title, message and option type.
     * The result of the dialog is returned as a boolean.
     * If the dialog is cancelled, the result is false.
     *
     * @param title The title of the dialog.
     * @param message The message of the dialog.
     * @param optionType The options of the dialog.
     * @param type The type of the dialog.
     *
     * @return The result of the dialog.
     */
    fun confirm(title: String, message: String, optionType: OptionType, type: Type): Boolean

    /**
     * Shows an input dialog with the given title and message.
     * The result of the dialog is returned as a string.
     *
     * @param title The title of the dialog.
     * @param message The message of the dialog.
     *
     * @return The result of the dialog or null if the dialog was cancelled.
     */
    fun input(title: String, message: String): String?

    /**
     * Shows a save dialog.
     * The result of the dialog is returned as a string.
     *
     * @param default The default path. Defaults to null.
     * @param filters The filters to use to select file extensions.
     *
     * @return The result of the dialog or null if the dialog was cancelled.
     */
    fun save(default: String? = null, vararg filters: String): String?

    /**
     * Shows an open dialog.
     * The result of the dialog is returned as a string.
     *
     * @param default The default path. Defaults to null.
     * @param filters The filters to use to select file extensions.
     *
     * @return The result of the dialog or null if the dialog was cancelled.
     */
    fun open(default: String? = null, vararg filters: String): String?
}
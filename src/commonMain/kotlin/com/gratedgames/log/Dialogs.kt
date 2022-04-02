package com.gratedgames.log

interface Dialogs {
    enum class OptionType {
        OK,
        OK_CANCEL,
        YES_NO,
        YES_NO_CANCEL
    }

    enum class Type {
        INFO,
        WARNING,
        ERROR,
        QUESTION
    }

    fun message(title: String, message: String, type: Type)
    fun confirm(title: String, message: String, optionType: OptionType, type: Type): Boolean
    fun input(title: String, message: String): String?
    fun save(default: String? = null, vararg filters: String): String?
    fun open(default: String? = null, vararg filters: String): String?
}
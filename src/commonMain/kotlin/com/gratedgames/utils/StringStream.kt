package com.gratedgames.utils

import com.gratedgames.utils.extensions.whitespaceCharacters

class StringStream(val string: String) {
    companion object {
        const val END = 0.toChar()
    }

    var line = 0
        private set

    var column = 0
        private set

    var index = 0
        private set

    fun getNextChar(): Char {
        index++
        column++
        if (index == string.length)
            return END
        val ch = string[index]
        if (ch == '\n') {
            line++
            column = 0
        }
        return ch
    }

    fun getNextWord(vararg delimiters: String = whitespaceCharacters()): String? {
        if (getCurrentChar() == END)
            return null

        return buildString {
            var char = getCurrentChar()

            while (char != END && "$char" in delimiters) {
                char = getNextChar()
            }

            while (char != END && "$char" !in delimiters) {
                append(char)
                char = getNextChar()
            }
        }
    }

    fun peekNextWord(vararg delimiters: String = whitespaceCharacters()): String? {
        if (getCurrentChar() == END)
            return null

        val index = index
        val line = line
        val column = column

        return buildString {
            var char = getCurrentChar()

            while (char != END && "$char" in delimiters) {
                char = getNextChar()
            }

            while (char != END && "$char" !in delimiters) {
                append(char)
                char = getNextChar()
            }

            this@StringStream.index = index
            this@StringStream.line = line
            this@StringStream.column = column
        }
    }

    fun getCurrentChar(): Char {
        return if (index >= string.length) END else string[index]
    }

    fun peekNextChar(): Char {
        return if (index + 1 >= string.length) END else string[index + 1]
    }

    fun getLine() = buildString {
        var ch: Char
        do {
            ch = getNextChar()
            append(ch)
        } while (ch != '\n' && ch != END)
    }

    fun getWords(vararg delimiters: String = whitespaceCharacters()): List<String> {
        val words = arrayListOf<String>()

        var word = getNextWord(*delimiters)
        while (word != null) {
            words += word
            word = getNextWord(*delimiters)
        }

        return words.toList()
    }
}

fun StringStream.toLines(): List<String> {
    val lines = arrayListOf<String>()
    while (getCurrentChar() != StringStream.END)
        lines += getLine()
    return lines
}

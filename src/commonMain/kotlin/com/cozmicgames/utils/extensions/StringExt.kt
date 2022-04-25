package com.cozmicgames.utils.extensions

import com.cozmicgames.utils.StringStream

private val formatRegex = Regex("%([-]?\\d+)?(\\w)")
private val commentsRegex = Regex("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)")
private val blankLinesRegex = Regex("(?m)^[ \t]*\r?\n")
private val whitespaceCharacters = arrayOf(" ", "\t", "\n", "\r")

fun whitespaceCharacters() = whitespaceCharacters

fun String.format(vararg params: Any): String {
    var paramIndex = 0
    return formatRegex.replace(this) { mr ->
        val param = params[paramIndex++]
        val size = mr.groupValues[1]
        val type = mr.groupValues[2]
        val str = when (type) {
            "d" -> (param as Number).toLong().toString()
            "X", "x" -> {
                val res = when (param) {
                    is Int -> param.toStringUnsigned(16)
                    else -> (param as Number).toLong().toStringUnsigned(16)
                }
                if (type == "X") res.uppercase() else res.lowercase()
            }
            else -> "$param"
        }
        val prefix = if (size.startsWith('0')) '0' else ' '
        val asize = size.toIntOrNull()
        var str2 = str
        if (asize != null) {
            while (str2.length < asize) {
                str2 = prefix + str2
            }
        }
        str2
    }
}

fun String.removeComments() = replace(commentsRegex, "")

fun String.removeBlankLines() = replace(blankLinesRegex, "")

inline val String.extension: String
    get() {
        val indexOfPoint = lastIndexOf('.')
        return (if (indexOfPoint == -1) "" else substring(indexOfPoint + 1, length)).lowercase()
    }

inline val String.nameWithExtension: String
    get() {
        val indexOfSlash = lastIndexOf('/')
        return substring(if (indexOfSlash == -1) 0 else indexOfSlash + 1, length)
    }

inline val String.nameWithoutExtension: String
    get() {
        val indexOfSlash = lastIndexOf('/')
        val indexOfPoint = lastIndexOf('.')
        return substring(if (indexOfSlash == -1) 0 else indexOfSlash + 1, if (indexOfPoint == -1) length else indexOfPoint)
    }

inline val String.directory: String
    get() {
        val indexOfSlash = lastIndexOf('/')
        return substring(0, if (indexOfSlash == -1) length else indexOfSlash + 1)
    }

fun String.words(vararg delimiters: String = whitespaceCharacters()) = StringStream(this).getWords(*delimiters)

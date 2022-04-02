package com.gratedgames.utils

import com.gratedgames.utils.extensions.whitespaceCharacters

class StringTokenizer(string: String, vararg delimiters: String = whitespaceCharacters()) {
    private val tokens = StringStream(string).getWords(*delimiters)
    private var index = 0

    val count by tokens::size

    val hasMoreTokens get() = index < count

    fun nextToken() = tokens[index++]
}
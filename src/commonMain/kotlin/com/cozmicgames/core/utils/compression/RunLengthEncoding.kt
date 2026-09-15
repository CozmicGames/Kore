package com.cozmicgames.core.utils.compression

import com.cozmicgames.core.utils.extensions.isOdd

fun encodeRunLength(data: ByteArray, output: (Byte, Byte) -> Unit) {
    var count = 1
    var currentByte = data[0]

    for (i in 1 until data.size) {
        if (data[i] == currentByte) {
            count++
        } else {
            output(currentByte, count.toByte())
            currentByte = data[i]
            count = 1
        }
    }

    output(currentByte, count.toByte())
}

fun encodeRunLength(data: ByteArray): ByteArray {
    val output = arrayListOf<Byte>()

    encodeRunLength(data) { byte, count ->
        output.add(byte)
        output.add(count)
    }

    return output.toByteArray()
}

fun decodeRunLength(data: ByteArray, output: (Byte) -> Unit) {
    if (data.size.isOdd)
        throw IllegalArgumentException("Data size must be even for run-length encoding")

    for (i in data.indices step 2) {
        val byte = data[i]
        val count = data[i + 1].toInt()

        repeat(count) {
            output(byte)
        }
    }
}

fun decodeRunLength(data: ByteArray): ByteArray {
    val output = arrayListOf<Byte>()

    decodeRunLength(data) { byte ->
        output.add(byte)
    }

    return output.toByteArray()
}

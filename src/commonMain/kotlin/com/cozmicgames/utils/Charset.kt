package com.cozmicgames.utils

import com.cozmicgames.utils.extensions.read16
import com.cozmicgames.utils.extensions.write16

abstract class Charset(val bytesPerChar: Int) {
    abstract fun encode(out: ByteArrayBuilder, src: CharSequence, start: Int = 0, end: Int = src.length)
    abstract fun decode(out: StringBuilder, src: ByteArray, start: Int = 0, end: Int = src.size)
}

object Charsets {
    val UTF8: Charset get() = _UTF8
    val ISO_8859_1: Charset get() = _ISO_8859_1
    val UTF16: Charset get() = _UTF16
    val ASCII: Charset get() = _ASCII
}

private object _UTF8 : Charset(1) {
    private fun createByte(codePoint: Int, shift: Int): Int = codePoint shr shift and 0x3F or 0x80

    override fun encode(out: ByteArrayBuilder, src: CharSequence, start: Int, end: Int) {
        for (n in start until end) {
            val codePoint = src[n].code

            if (codePoint and 0x7F.inv() == 0)
                out.append(codePoint.toByte())
            else {
                when {
                    codePoint and 0x7FF.inv() == 0 -> out.append((codePoint shr 6 and 0x1F or 0xC0).toByte())
                    codePoint and 0xFFFF.inv() == 0 -> {
                        out.append((codePoint shr 12 and 0x0F or 0xE0).toByte())
                        out.append((createByte(codePoint, 6)).toByte())
                    }
                    codePoint and -0x200000 == 0 -> {
                        out.append((codePoint shr 18 and 0x07 or 0xF0).toByte())
                        out.append((createByte(codePoint, 12)).toByte())
                        out.append((createByte(codePoint, 6)).toByte())
                    }
                }
                out.append((codePoint and 0x3F or 0x80).toByte())
            }
        }
    }

    override fun decode(out: StringBuilder, src: ByteArray, start: Int, end: Int) {
        require(start >= 0 && start < src.size && end >= 0 && end <= src.size)

        var i = start
        while (i < end) {
            val c = src[i].toInt() and 0xFF
            when (c shr 4) {
                in 0..7 -> {
                    out.append(c.toChar())
                    i += 1
                }
                in 12..13 -> {
                    out.append((c and 0x1F shl 6 or (src[i + 1].toInt() and 0x3F)).toChar())
                    i += 2
                }
                14 -> {
                    out.append((c and 0x0F shl 12 or (src[i + 1].toInt() and 0x3F shl 6) or (src[i + 2].toInt() and 0x3F)).toChar())
                    i += 3
                }
                else -> {
                    i += 1
                }
            }
        }
    }
}

open class SingleByteCharset(private val conv: String) : Charset(1) {
    val v = hashMapOf<Int, Int>().apply {
        for (n in conv.indices)
            this[conv[n].code] = n
    }

    override fun encode(out: ByteArrayBuilder, src: CharSequence, start: Int, end: Int) {
        for (n in start until end) {
            val c = src[n].code
            out.append(if (c in v) requireNotNull(v[c]).toByte() else '?'.code.toByte())
        }
    }

    override fun decode(out: StringBuilder, src: ByteArray, start: Int, end: Int) {
        for (n in start until end) {
            out.append(conv[src[n].toInt() and 0xFF])
        }
    }
}

private object _ISO_8859_1 : SingleByteCharset(buildString { for (n in 0 until 256) append(n.toChar()) })

private object _UTF16 : Charset(2) {
    override fun decode(out: StringBuilder, src: ByteArray, start: Int, end: Int) {
        for (n in start until end step 2) out.append(src.read16(n).toChar())
    }

    override fun encode(out: ByteArrayBuilder, src: CharSequence, start: Int, end: Int) {
        val temp = ByteArray(2)
        for (n in start until end) {
            temp.write16(0, src[n].code)
            out.append(*temp)
        }
    }
}

private object _ASCII : Charset(1) {
    override fun encode(out: ByteArrayBuilder, src: CharSequence, start: Int, end: Int) {
        for (n in start until end) out.append(src[n].code.toByte())
    }

    override fun decode(out: StringBuilder, src: ByteArray, start: Int, end: Int) {
        for (n in start until end) out.append(src[n].toInt().toChar())
    }
}

fun String.toByteArray(charset: Charset = Charsets.UTF8): ByteArray {
    val out = ByteArrayBuilder()
    charset.encode(out, this)
    return out.toByteArray()
}

fun ByteArray.toString(charset: Charset): String {
    val out = StringBuilder()
    charset.decode(out, this)
    return out.toString()
}

fun ByteArray.readString(offset: Int, size: Int, charset: Charset = Charsets.UTF8): String {
    return copyOfRange(offset, offset + size).toString(charset)
}
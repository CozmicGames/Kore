package com.gratedgames.files

import com.gratedgames.utils.ByteArrayBuilder
import com.gratedgames.utils.Charset
import com.gratedgames.utils.extensions.read16
import com.gratedgames.utils.extensions.read32
import java.io.InputStream

class DesktopReadStream(val stream: InputStream) : ReadStream {
    override val availableBytes get() = stream.available()

    override fun skip(length: Int) {
        stream.skip(length.toLong())
    }

    override fun readBytes(length: Int, dest: ByteArray, offset: Int) {
        stream.read(dest, offset, length)
    }

    override fun readByte(): Byte {
        return stream.read().toByte()
    }

    override fun readShort(): Short {
        return read16(0) { readByte() }.toShort()
    }

    override fun readInt(): Int {
        return read32(0) { readByte() }
    }

    override fun readFloat(): Float {
        return Float.fromBits(readInt())
    }

    override fun readString(charset: Charset): String {
        val builder = ByteArrayBuilder()
        val temp = ByteArray(1)
        while (true) {
            val read = stream.read(temp, 0, 1)
            if (read <= 0 || temp[0] == 0.toByte())
                break

            builder.append(temp[0])
        }

        return buildString {
            charset.decode(this, builder.toByteArray())
        }
    }

    override fun readString(length: Int, charset: Charset): String {
        val bytes = ByteArray(length)
        readBytes(length, bytes)
        return buildString {
            charset.decode(this, bytes)
        }
    }

    override fun dispose() {
        stream.close()
    }
}
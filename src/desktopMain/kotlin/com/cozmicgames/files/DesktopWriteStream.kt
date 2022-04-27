package com.cozmicgames.files

import com.cozmicgames.utils.ByteArrayBuilder
import com.cozmicgames.utils.Charset
import com.cozmicgames.utils.extensions.write16
import com.cozmicgames.utils.extensions.write32
import java.io.OutputStream

class DesktopWriteStream(val stream: OutputStream) : WriteStream {
    override val writtenBytes get() = counter
    private var counter = 0

    override fun writeBytes(values: ByteArray, offset: Int, length: Int) {
        stream.write(values, offset, length)
        counter += length
    }

    override fun writeByte(value: Byte) {
        counter++
        stream.write(value.toInt())
    }

    override fun writeShort(value: Short) {
        write16(0, value.toInt()) { _, v -> writeByte(v) }
    }

    override fun writeInt(value: Int) {
        write32(0, value) { _, v -> writeByte(v) }
    }

    override fun writeFloat(value: Float) {
        writeInt(value.toBits())
    }

    override fun writeString(value: String, charset: Charset) {
        val builder = ByteArrayBuilder()
        charset.encode(builder, value)
        writeBytes(builder.data, 0, builder.size)
    }

    override fun dispose() {
        stream.close()
    }
}
package com.cozmicgames.core.memory

import org.lwjgl.system.MemoryUtil

class DesktopMemoryAccess : MemoryAccess {
    override val totalMemory get() = Runtime.getRuntime().totalMemory().toInt()
    override val freeMemory get() = Runtime.getRuntime().freeMemory().toInt()

    override fun alloc(size: Int): Long {
        return MemoryUtil.nmemCalloc(size.toLong(), 1)
    }

    override fun free(address: Long) {
        MemoryUtil.nmemFree(address)
    }

    override fun set(address: Long, position: Int, value: Byte, length: Int) {
        MemoryUtil.memSet(address + position, value.toInt(), length.toLong())
    }

    override fun copy(fromAddress: Long, fromPosition: Int, toAddress: Long, toPosition: Int, length: Int) {
        MemoryUtil.memCopy(fromAddress + fromPosition, toAddress + toPosition, length.toLong())
    }

    override fun putByte(address: Long, position: Int, value: Byte) {
        MemoryUtil.memPutByte(address + position, value)
    }

    override fun getByte(address: Long, position: Int): Byte {
        return MemoryUtil.memGetByte(address + position)
    }

    override fun putShort(address: Long, position: Int, value: Short) {
        MemoryUtil.memPutShort(address + position, value)
    }

    override fun getShort(address: Long, position: Int): Short {
        return MemoryUtil.memGetShort(address + position)
    }

    override fun putInt(address: Long, position: Int, value: Int) {
        MemoryUtil.memPutInt(address + position, value)
    }

    override fun getInt(address: Long, position: Int): Int {
        return MemoryUtil.memGetInt(address + position)
    }

    override fun putLong(address: Long, position: Int, value: Long) {
        MemoryUtil.memPutLong(address + position, value)
    }

    override fun getLong(address: Long, position: Int): Long {
        return MemoryUtil.memGetLong(address + position)
    }

    override fun putFloat(address: Long, position: Int, value: Float) {
        MemoryUtil.memPutFloat(address + position, value)
    }

    override fun getFloat(address: Long, position: Int): Float {
        return MemoryUtil.memGetFloat(address + position)
    }

    override fun copyBytes(from: ByteArray, fromOffset: Int, toAddress: Long, toOffset: Int, length: Int) {
        MemoryUtil.memCopy(from, toAddress + toOffset, fromOffset, length)
    }

    override fun copyShorts(from: ShortArray, fromOffset: Int, toAddress: Long, toOffset: Int, length: Int) {
        MemoryUtil.memCopy(from, toAddress + toOffset, fromOffset, length)
    }

    override fun copyInts(from: IntArray, fromOffset: Int, toAddress: Long, toOffset: Int, length: Int) {
        MemoryUtil.memCopy(from, toAddress + toOffset, fromOffset, length)
    }

    override fun copyLongs(from: LongArray, fromOffset: Int, toAddress: Long, toOffset: Int, length: Int) {
        MemoryUtil.memCopy(from, toAddress + toOffset, fromOffset, length)
    }

    override fun copyFloats(from: FloatArray, fromOffset: Int, toAddress: Long, toOffset: Int, length: Int) {
        MemoryUtil.memCopy(from, toAddress + toOffset, fromOffset, length)
    }
}
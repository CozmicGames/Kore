package com.cozmicgames.memory

interface MemoryAccess {
    val totalMemory: Int
    val freeMemory: Int

    fun alloc(size: Int): Long
    fun free(address: Long)
    fun set(address: Long, position: Int, value: Byte, length: Int)
    fun copy(fromAddress: Long, fromPosition: Int, toAddress: Long, toPosition: Int, length: Int)
    fun putByte(address: Long, position: Int, value: Byte)
    fun getByte(address: Long, position: Int): Byte
    fun putShort(address: Long, position: Int, value: Short)
    fun getShort(address: Long, position: Int): Short
    fun putInt(address: Long, position: Int, value: Int)
    fun getInt(address: Long, position: Int): Int
    fun putLong(address: Long, position: Int, value: Long)
    fun getLong(address: Long, position: Int): Long
}

val MemoryAccess.usedMemory get() = totalMemory - freeMemory
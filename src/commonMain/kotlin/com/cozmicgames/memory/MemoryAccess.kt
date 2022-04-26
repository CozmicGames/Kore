package com.cozmicgames.memory

import com.cozmicgames.Kore

/**
 * [MemoryAccess] is the framework module for allocation, deallocation and accessing data in memory.
 * It must be implemented by the platform specific implementation and bound to [Kore.context].
 */
interface MemoryAccess {
    /**
     * The total size of memory of the process in bytes.
     */
    val totalMemory: Int

    /**
     * The free amount of memory of the process in bytes.
     */
    val freeMemory: Int

    /**
     * Allocates the specified amount of memory in bytes.
     *
     * @param size The amount of memory to allocate in bytes.
     *
     * @return The address of the allocated memory.
     */
    fun alloc(size: Int): Long

    /**
     * Frees the specified amount of memory in bytes.
     *
     * @param address The address of the memory to free.
     */
    fun free(address: Long)

    /**
     * Sets the byte value at the specified [address] for the specified [length].
     *
     * @param address The address of the memory to set.
     * @param value The value to set.
     * @param length The length to set the value at.
     */
    fun set(address: Long, position: Int, value: Byte, length: Int)

    /**
     * Copies the data from the specified [fromAddress] at the specified [fromPosition] to the specified [toAddress] at the specified [toPosition] for the specified [length].
     *
     * @param fromAddress The address of the memory to copy from.
     * @param fromPosition The position to copy from.
     * @param toAddress The address of the memory to copy to.
     * @param toPosition The position to copy to.
     * @param length The length to copy.
     */
    fun copy(fromAddress: Long, fromPosition: Int, toAddress: Long, toPosition: Int, length: Int)

    /**
     * Puts the specified byte [value] at the specified [address] at the specified [position].
     *
     * @param address The address of the memory to set.
     * @param position The position to set the value at.
     * @param value The value to set.
     */
    fun putByte(address: Long, position: Int, value: Byte)

    /**
     * Gets the byte value at the specified [address] at the specified [position].
     *
     * @param address The address of the memory to get.
     * @param position The position to get the value at.
     *
     * @return The byte value at the specified [address] at the specified [position].
     */
    fun getByte(address: Long, position: Int): Byte

    /**
     * Puts the specified short [value] at the specified [address] at the specified [position].
     *
     * @param address The address of the memory to set.
     * @param position The position to set the value at.
     * @param value The value to set.
     */
    fun putShort(address: Long, position: Int, value: Short)

    /**
     * Gets the short value at the specified [address] at the specified [position].
     *
     * @param address The address of the memory to get.
     * @param position The position to get the value at.
     *
     * @return The short value at the specified [address] at the specified [position].
     */
    fun getShort(address: Long, position: Int): Short

    /**
     * Puts the specified int [value] at the specified [address] at the specified [position].
     *
     * @param address The address of the memory to set.
     * @param position The position to set the value at.
     * @param value The value to set.
     */
    fun putInt(address: Long, position: Int, value: Int)

    /**
     * Gets the int value at the specified [address] at the specified [position].
     *
     * @param address The address of the memory to get.
     * @param position The position to get the value at.
     *
     * @return The int value at the specified [address] at the specified [position].
     */
    fun getInt(address: Long, position: Int): Int

    /**
     * Puts the specified long [value] at the specified [address] at the specified [position].
     *
     * @param address The address of the memory to set.
     * @param position The position to set the value at.
     * @param value The value to set.
     */
    fun putLong(address: Long, position: Int, value: Long)

    /**
     * Gets the long value at the specified [address] at the specified [position].
     *
     * @param address The address of the memory to get.
     * @param position The position to get the value at.
     *
     * @return The long value at the specified [address] at the specified [position].
     */
    fun getLong(address: Long, position: Int): Long
}

/**
 * The amount of memory in bytes used by the application.
 */
val MemoryAccess.usedMemory get() = totalMemory - freeMemory
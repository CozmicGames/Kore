package com.cozmicgames.core.memory

import com.cozmicgames.core.Kore
import com.cozmicgames.core.memoryAccess
import com.cozmicgames.core.utils.Disposable

/**
 * Memory describes a native memory region.
 * It is used to pass data to graphics APIs.
 * It needs to be disposed when no longer needed, it is not automatically disposed.
 *
 * The primary constructor is private for partitioning [Memory], use the secondary constructor to create a Memory.
 *
 * @param address The address of the memory region.
 * @param size The size of the memory region.
 * @param offset The offset of the memory region.
 * @param isPartition Whether the memory is a partition of another memory region.
 */
class Memory private constructor(val address: Long, val size: Int, private val offset: Int, val isPartition: Boolean = false) : Disposable {
    /**
     * Allocates a new memory block of the specified size and instantiates this with it.
     */
    constructor(size: Int) : this(Kore.memoryAccess.alloc(size), size, 0)

    companion object {
        /**
         * The size of a single byte.
         */
        const val SIZEOF_BYTE = 1

        /**
         * The size of a single short.
         */
        const val SIZEOF_SHORT = 2 * SIZEOF_BYTE

        /**
         * The size of a single int.
         */
        const val SIZEOF_INT = 4 * SIZEOF_BYTE

        /**
         * The size of a single long.
         */
        const val SIZEOF_LONG = 8 * SIZEOF_BYTE

        /**
         * The size of a single float. It's the same as a single int because [Memory] uses [Float.toRawBits] and [Float.fromBits].
         */
        const val SIZEOF_FLOAT = SIZEOF_INT

        /**
         * @see [MemoryAccess.totalMemory]
         */
        val total get() = Kore.memoryAccess.totalMemory

        /**
         * @see [MemoryAccess.freeMemory]
         */
        val free get() = Kore.memoryAccess.freeMemory

        /**
         * @see [usedMemory]
         */
        val used get() = Kore.memoryAccess.usedMemory
    }

    /**
     * Fills the memory region from [offset] with the specified value for the specified [size].
     *
     * @param value The value to fill the memory region with.
     * @param offset The offset into the memory region.
     * @param size The size of the memory region to fill.
     */
    fun fill(value: Byte, offset: Int = 0, size: Int = this.size - offset) = Kore.memoryAccess.set(address, offset, value, size)

    /**
     * Gets the byte value at the specified [position].
     *
     * @param position The position to get the value at.
     *
     * @return The byte value at the specified [position].
     */
    fun getByte(position: Int) = Kore.memoryAccess.getByte(address, position + offset)

    /**
     * Gets the short value at the specified [position].
     *
     * @param position The position to get the value at.
     *
     * @return The short value at the specified [position].
     */
    fun getShort(position: Int) = Kore.memoryAccess.getShort(address, position + offset)

    /**
     * Gets the int value at the specified [position].
     *
     * @param position The position to get the value at.
     *
     * @return The int value at the specified [position].
     */
    fun getInt(position: Int) = Kore.memoryAccess.getInt(address, position + offset)

    /**
     * Gets the long value at the specified [position].
     *
     * @param position The position to get the value at.
     *
     * @return The long value at the specified [position].
     */
    fun getLong(position: Int) = Kore.memoryAccess.getLong(address, position + offset)

    /**
     * Gets the float value at the specified [position].
     *
     * @param position The position to get the value at.
     *
     * @return The float value at the specified [position].
     */
    fun getFloat(position: Int) = Kore.memoryAccess.getFloat(address, position + offset)

    /**
     * Puts the specified byte [value] at the specified [position].
     *
     * @param position The position to set the value at.
     * @param value The value to set.
     */
    fun setByte(position: Int, value: Byte) = Kore.memoryAccess.putByte(address, position + offset, value)

    /**
     * Puts the specified short [value] at the specified [position].
     *
     * @param position The position to set the value at.
     * @param value The value to set.
     */
    fun setShort(position: Int, value: Short) = Kore.memoryAccess.putShort(address, position + offset, value)

    /**
     * Puts the specified int [value] at the specified [position].
     *
     * @param position The position to set the value at.
     * @param value The value to set.
     */
    fun setInt(position: Int, value: Int) = Kore.memoryAccess.putInt(address, position + offset, value)

    /**
     * Puts the specified long [value] at the specified [position].
     *
     * @param position The position to set the value at.
     * @param value The value to set.
     */
    fun setLong(position: Int, value: Long) = Kore.memoryAccess.putLong(address, position + offset, value)

    /**
     * Puts the specified float [value] at the specified [position].
     *
     * @param position The position to set the value at.
     * @param value The value to set.
     */
    fun setFloat(position: Int, value: Float) = Kore.memoryAccess.putFloat(address, position + offset, value)

    /**
     *  Copies the specified [length] of the [data] array from [offset] to this memory region at the specified [destOffset].
     *
     *  @param data The source byte array to copy from.
     *  @param offset The offset into the source byte array to copy from.
     *  @param length The size of the memory region to copy.
     *  @param destOffset The offset into this memory region to copy to.
     */
    fun setBytes(data: ByteArray, offset: Int = 0, length: Int = data.size, destOffset: Int = 0) {
        require(size - destOffset >= length)
        Kore.memoryAccess.copyBytes(data, offset, address, destOffset, length)
    }

    /**
     *  Copies the specified [length] of the [data] array from [offset] to this memory region at the specified [destOffset].
     *
     *  @param data The source short array to copy from.
     *  @param offset The offset into the source short array to copy from.
     *  @param length The size of the memory region to copy.
     *  @param destOffset The offset into this memory region to copy to.
     */
    fun setShorts(data: ShortArray, offset: Int = 0, length: Int = data.size, destOffset: Int = 0) {
        require(size - destOffset >= length)
        Kore.memoryAccess.copyShorts(data, offset, address, destOffset, length)
    }

    /**
     *  Copies the specified [length] of the [data] array from [offset] to this memory region at the specified [destOffset].
     *
     *  @param data The source int array to copy from.
     *  @param offset The offset into the source int array to copy from.
     *  @param length The size of the memory region to copy.
     *  @param destOffset The offset into this memory region to copy to.
     */
    fun setInts(data: IntArray, offset: Int = 0, length: Int = data.size, destOffset: Int = 0) {
        require(size - destOffset >= length)
        Kore.memoryAccess.copyInts(data, offset, address, destOffset, length)
    }

    /**
     *  Copies the specified [length] of the [data] array from [offset] to this memory region at the specified [destOffset].
     *
     *  @param data The source long array to copy from.
     *  @param offset The offset into the source long array to copy from.
     *  @param length The size of the memory region to copy.
     *  @param destOffset The offset into this memory region to copy to.
     */
    fun setLongs(data: LongArray, offset: Int = 0, length: Int = data.size, destOffset: Int = 0) {
        require(size - destOffset >= length)
        Kore.memoryAccess.copyLongs(data, offset, address, destOffset, length)
    }

    /**
     *  Copies the specified [length] of the [data] array from [offset] to this memory region at the specified [destOffset].
     *
     *  @param data The source float array to copy from.
     *  @param offset The offset into the source float array to copy from.
     *  @param length The size of the memory region to copy.
     *  @param destOffset The offset into this memory region to copy to.
     */
    fun setFloats(data: FloatArray, offset: Int = 0, length: Int = data.size, destOffset: Int = 0) {
        require(size - destOffset >= length)
        Kore.memoryAccess.copyFloats(data, offset, address, destOffset, length)
    }

    /**
     * Copies the specified [length] of the memory region from [offset] to [memory] at the specified [destOffset].
     *
     * @param memory The destination to copy the memory region to.
     * @param length The size of the memory region to copy.
     * @param offset The offset into the memory region to copy from.
     * @param destOffset The offset into the destination to copy to.
     */
    fun copyTo(memory: Memory, length: Int = size, offset: Int = 0, destOffset: Int = 0) {
        require(memory.size - destOffset >= length)
        Kore.memoryAccess.copy(address, this.offset + offset, memory.address, destOffset, length)
    }

    /**
     * Creates a partition of this [Memory] instance with the specified [size] and [offset].
     * The partition will be backed by the same memory as this instance.
     *
     * @param offset The offset of the partition.
     * @param size The size of the partition.
     *
     * @return The partition.
     */
    fun partition(offset: Int, size: Int) = Memory(address, size, offset, true)

    /**
     * Disposes of this [Memory] instance.
     */
    override fun dispose() {
        if (isPartition)
            return

        Kore.memoryAccess.free(address)
    }
}

/**
 * Creates a new [Memory] instance with the specified [values].
 * It's size will be the size of the [values] array times [Memory.SIZEOF_BYTE].
 *
 * @param values The values to initialize the memory with.
 *
 * @return The new [Memory] instance.
 */
fun Memory.Companion.of(vararg values: Byte) = Memory(values.size * SIZEOF_BYTE).also {
    it.setBytes(values)
}

/**
 * Creates a new [Memory] instance with the specified [values].
 * It's size will be the size of the [values] array times [Memory.SIZEOF_SHORT].
 *
 * @param values The values to initialize the memory with.
 *
 * @return The new [Memory] instance.
 */
fun Memory.Companion.of(vararg values: Short) = Memory(values.size * SIZEOF_SHORT).also {
    it.setShorts(values)
}

/**
 * Creates a new [Memory] instance with the specified [values].
 * It's size will be the size of the [values] array times [Memory.SIZEOF_INT].
 *
 * @param values The values to initialize the memory with.
 *
 * @return The new [Memory] instance.
 */
fun Memory.Companion.of(vararg values: Int) = Memory(values.size * SIZEOF_INT).also {
    it.setInts(values)
}

/**
 * Creates a new [Memory] instance with the specified [values].
 * It's size will be the size of the [values] array times [Memory.SIZEOF_LONG].
 *
 * @param values The values to initialize the memory with.
 *
 * @return The new [Memory] instance.
 */
fun Memory.Companion.of(vararg values: Long) = Memory(values.size * SIZEOF_LONG).also {
    it.setLongs(values)
}

/**
 * Creates a new [Memory] instance with the specified [values].
 * It's size will be the size of the [values] array times [Memory.SIZEOF_FLOAT].
 *
 * @param values The values to initialize the memory with.
 *
 * @return The new [Memory] instance.
 */
fun Memory.Companion.of(vararg values: Float) = Memory(values.size * SIZEOF_FLOAT).also {
    it.setFloats(values)
}

/**
 * Clears the memory region and fills it with 0.
 *
 * @param offset The offset to start clearing from.
 * @param size The size of the memory region to clear.
 */
fun Memory.clear(offset: Int = 0, size: Int = this.size) = fill(0, offset, size)

/**
 * Allocates a new [Memory] instance with the specified [size].
 *
 * @param size The size of the memory region to allocate.
 *
 * @return The new [Memory] instance.
 */
fun alloc(size: Int) = Memory(size)

/**
 * Allocates a new [Memory] instance with the specified [size].
 * The memory will be allocated as a partition of the pre-allocated memory in [MemoryStack].
 *
 * @param size The size of the memory region to allocate.
 *
 * @return The new [Memory] instance.
 */
fun stackAlloc(size: Int) = MemoryStack.alloc(size)

/**
 * Returns this value to represent bytes.
 * This does not perform any conversion, it simply returns the value.
 */
inline val Int.bytes get() = this

/**
 * Converts this value to represent kilobytes.
 */
inline val Int.kilobytes get() = bytes shl 10

/**
 * Converts this value to represent megabytes.
 */
inline val Int.megabytes get() = kilobytes shl 10

/**
 * Converts this value to represent gigabytes.
 */
inline val Int.gigabytes get() = megabytes shl 10

/**
 * An interface for memory allocators.
 * Implementations of this interface can be used to allocate and free memory.
 */
interface MemoryAllocator {
    fun beginScope() {}
    fun endScope() {}
    fun alloc(size: Int): Memory
    fun free(memory: Memory)
}

/**
 * Executes the given [block] of code within a memory allocation scope.
 * The scope is started with [beginScope] and ended with [endScope].
 * This is useful for stack allocators that need to manage memory scopes.
 *
 * @param block The block of code to execute within the memory allocation scope.
 */
fun MemoryAllocator.use(block: MemoryAllocator.() -> Unit) {
    beginScope()
    try {
        block()
    } finally {
        endScope()
    }
}

/**
 * A default implementation of [MemoryAllocator] that uses [alloc] and [Memory.dispose] to allocate and free memory.
 */
object DefaultMemoryAllocator : MemoryAllocator {
    override fun alloc(size: Int): Memory = alloc(size)
    override fun free(memory: Memory) = memory.dispose()
}

/**
 * A stack allocator that uses [stackAlloc] to allocate memory.
 * This allocator does not free memory, as it is managed by the [MemoryStack].
 */
object StackMemoryAllocator : MemoryAllocator {
    override fun beginScope() = MemoryStack.push()
    override fun endScope() = MemoryStack.pop()
    override fun alloc(size: Int): Memory = stackAlloc(size)
    override fun free(memory: Memory) {}
}

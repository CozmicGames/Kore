package com.cozmicgames.memory

import com.cozmicgames.utils.Disposable
import kotlin.reflect.KProperty

/**
 * A struct is a class that can have fields backed by a [Memory] instance.
 * The fields can be implemented as delegate properties by classes inheriting from [Struct] by using the provided methods.
 * Setting a field will update the backing memory, and getting a field will read from the backing memory.
 * This is useful for storing data by conventional fields, that need to be passed to the graphics API.
 * Structs that are created by themselves must be disposed.
 */
abstract class Struct() : Disposable {
    /**
     * Creates a new struct using the specified [allocator].
     *
     * @param allocator The allocator to use for allocating the backing memory.
     */
    constructor(allocator: Allocator) : this() {
        internalMemorySupplier = { allocator(size) }
    }

    /**
     * Creates a new struct which will be allocated by [stackAlloc] or [alloc], depending on [isStackAllocated].
     *
     * @param isStackAllocated Whether the struct should be allocated on the stack.
     */
    constructor(isStackAllocated: Boolean) : this(if (isStackAllocated) ::stackAlloc else ::alloc)

    /**
     * The size of the struct in bytes.
     */
    var size = 0
        private set

    /**
     * The backing memory for the struct.
     */
    val memory get() = requireNotNull(internalMemory)

    private var ownsMemory = false

    internal var getOffset: () -> Int = { 0 }

    private val memorySupplier get() = requireNotNull(internalMemorySupplier)

    internal var internalMemorySupplier: (() -> Memory)? = null
        get() {
            if (field == null) {
                field = { alloc(size) }
                ownsMemory = true
            }
            return field
        }

    private var internalMemory: Memory? = null
        get() {
            if (field == null)
                field = memorySupplier()
            return field
        }

    private fun <T : Any, P : StructProperty<T>> register(property: P): P {
        size += property.size
        return property
    }

    /**
     * Adds padding to the struct.
     *
     * @param size The size of the padding in bytes.
     */
    protected fun padding(size: Int) {
        this.size += size
    }

    /**
     * Adds a byte property to the struct which can be used as a delegate.
     *
     * @return The property.
     */
    protected fun byte() = register(ByteProperty(this))

    /**
     * Adds a short property to the struct which can be used as a delegate.
     *
     * @return The property.
     */
    protected fun short() = register(ShortProperty(this))

    /**
     * Adds an int property to the struct which can be used as a delegate.
     *
     * @return The property.
     */
    protected fun int() = register(IntProperty(this))

    /**
     * Adds a long property to the struct which can be used as a delegate.
     *
     * @return The property.
     */
    protected fun long() = register(LongProperty(this))

    /**
     * Adds a float property to the struct which can be used as a delegate.
     *
     * @return The property.
     */
    protected fun float() = register(FloatProperty(this))

    /**
     * Adds a boolean property to the struct which can be used as a delegate.
     *
     * @return The property.
     */
    protected fun boolean() = register(BooleanProperty(this))

    /**
     * Adds a struct property to the struct which can be used as a delegate.
     * The struct must not be created with an allocator.
     *
     * @return The property.
     */
    protected fun <T : Struct> struct(struct: T) = register(OtherStructProperty(this, struct))

    /**
     * Adds an array property to the struct which can be used as a delegate.
     * The array must not be created with an allocator.
     *
     * @return The property.
     */
    protected fun <T : Struct> array(array: StructArray<T>) = register(StructArrayProperty(this, array))

    /**
     * Disposes the struct.
     * This will free the backing memory if it was allocated by the struct.
     */
    override fun dispose() {
        internalMemory?.dispose()
    }
}

abstract class StructProperty<T : Any>(protected val struct: Struct, val size: Int) {
    private val offsetInStruct = struct.size
    protected val offset get() = struct.getOffset() + offsetInStruct

    abstract operator fun getValue(thisRef: Any, property: KProperty<*>): T
}

abstract class MutableStructProperty<T : Any>(struct: Struct, size: Int) : StructProperty<T>(struct, size) {
    abstract operator fun setValue(thisRef: Any, property: KProperty<*>, value: T)
}

/**
 * A property which can be used to access a byte in the struct.
 */
class ByteProperty(struct: Struct) : MutableStructProperty<Byte>(struct, Memory.SIZEOF_BYTE) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Byte {
        return struct.memory.getByte(offset)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Byte) {
        struct.memory.setByte(offset, value)
    }
}

/**
 * A property which can be used to access a short in the struct.
 */
class ShortProperty(struct: Struct) : MutableStructProperty<Short>(struct, Memory.SIZEOF_SHORT) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Short {
        return struct.memory.getShort(offset)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Short) {
        struct.memory.setShort(offset, value)
    }
}

/**
 * A property which can be used to access an int in the struct.
 */
class IntProperty(struct: Struct) : MutableStructProperty<Int>(struct, Memory.SIZEOF_INT) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Int {
        return struct.memory.getInt(offset)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        struct.memory.setInt(offset, value)
    }
}

/**
 * A property which can be used to access a long in the struct.
 */
class LongProperty(struct: Struct) : MutableStructProperty<Long>(struct, Memory.SIZEOF_LONG) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return struct.memory.getLong(offset)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        struct.memory.setLong(offset, value)
    }
}

/**
 * A property which can be used to access a float in the struct.
 */
class FloatProperty(struct: Struct) : MutableStructProperty<Float>(struct, Memory.SIZEOF_FLOAT) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Float {
        return struct.memory.getFloat(offset)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Float) {
        struct.memory.setFloat(offset, value)
    }
}

/**
 * A property which can be used to access a boolean in the struct.
 */
class BooleanProperty(struct: Struct) : MutableStructProperty<Boolean>(struct, Memory.SIZEOF_BYTE) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return struct.memory.getByte(offset) > 0
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        struct.memory.setByte(offset, if (value) 1 else 0)
    }
}

/**
 * A property which can be used to access another struct in the struct.
 */
class OtherStructProperty<T : Struct>(struct: Struct, private val valueStruct: T) : StructProperty<T>(struct, valueStruct.size) {
    init {
        valueStruct.getOffset = { offset }
        valueStruct.internalMemorySupplier = { struct.memory }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return valueStruct
    }
}

/**
 * A property which can be used to access an array in the struct.
 */
class StructArrayProperty<T : Struct>(struct: Struct, val array: StructArray<T>) : StructProperty<StructArray<T>>(struct, array.totalSize) {
    init {
        array.getOffset = { offset }
        array.internalMemorySupplier = { struct.memory }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): StructArray<T> {
        return array
    }
}
package com.gratedgames.memory

import com.gratedgames.utils.Disposable
import kotlin.reflect.KProperty

abstract class Struct() : Disposable {
    constructor(allocator: Allocator) : this() {
        internalMemorySupplier = { allocator(size) }
    }

    constructor(isStackAllocated: Boolean) : this(if (isStackAllocated) ::stackAlloc else ::alloc)

    private var ownsMemory = false

    internal var getOffset: () -> Int = { 0 }

    var size = 0
        private set

    private val memorySupplier get() = requireNotNull(internalMemorySupplier)

    internal var internalMemorySupplier: (() -> Memory)? = null
        get() {
            if (field == null) {
                field = { alloc(size) }
                ownsMemory = true
            }
            return field
        }

    val memory get() = requireNotNull(internalMemory)

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

    protected fun padding(size: Int): PaddingProperty {
        this.size += size
        return PaddingProperty()
    }

    protected fun byte() = register(ByteProperty(this))
    protected fun short() = register(ShortProperty(this))
    protected fun int() = register(IntProperty(this))
    protected fun long() = register(LongProperty(this))
    protected fun float() = register(FloatProperty(this))
    protected fun boolean() = register(BooleanProperty(this))
    protected fun <T : Struct> struct(struct: T) = register(OtherStructProperty(this, struct))
    protected fun <T : Struct> array(array: StructArray<T>) = register(StructArrayProperty(this, array))

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

class PaddingProperty {
    operator fun getValue(thisRef: Any, property: KProperty<*>) = Any()
}

class ByteProperty(struct: Struct) : MutableStructProperty<Byte>(struct, Memory.SIZEOF_BYTE) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Byte {
        return struct.memory.getByte(offset)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Byte) {
        struct.memory.setByte(offset, value)
    }
}

class ShortProperty(struct: Struct) : MutableStructProperty<Short>(struct, Memory.SIZEOF_SHORT) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Short {
        return struct.memory.getShort(offset)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Short) {
        struct.memory.setShort(offset, value)
    }
}

class IntProperty(struct: Struct) : MutableStructProperty<Int>(struct, Memory.SIZEOF_INT) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Int {
        return struct.memory.getInt(offset)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        struct.memory.setInt(offset, value)
    }
}

class LongProperty(struct: Struct) : MutableStructProperty<Long>(struct, Memory.SIZEOF_LONG) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return struct.memory.getLong(offset)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        struct.memory.setLong(offset, value)
    }
}

class FloatProperty(struct: Struct) : MutableStructProperty<Float>(struct, Memory.SIZEOF_FLOAT) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Float {
        return struct.memory.getFloat(offset)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Float) {
        struct.memory.setFloat(offset, value)
    }
}

class BooleanProperty(struct: Struct) : MutableStructProperty<Boolean>(struct, Memory.SIZEOF_BYTE) {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return struct.memory.getByte(offset) > 0
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        struct.memory.setByte(offset, if (value) 1 else 0)
    }
}

class OtherStructProperty<T : Struct>(struct: Struct, val valueStruct: T) : StructProperty<T>(struct, valueStruct.size) {
    init {
        valueStruct.getOffset = { offset }
        valueStruct.internalMemorySupplier = { struct.memory }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return valueStruct
    }
}

class StructArrayProperty<T : Struct>(struct: Struct, val array: StructArray<T>) : StructProperty<StructArray<T>>(struct, array.totalSize) {
    init {
        array.getOffset = { offset }
        array.internalMemorySupplier = { struct.memory }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): StructArray<T> {
        return array
    }
}
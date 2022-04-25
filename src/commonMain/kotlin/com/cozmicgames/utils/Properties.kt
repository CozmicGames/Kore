package com.cozmicgames.utils

import com.cozmicgames.utils.extensions.enumValueOfOrNull
import com.cozmicgames.utils.maths.*
import kotlin.reflect.KProperty

open class Properties {
    abstract class Delegate<T>(val defaultValue: () -> T) {
        var isDefaultSet = false

        abstract fun get(properties: Properties, name: String): T

        abstract fun set(properties: Properties, name: String, value: T)

        operator fun getValue(thisRef: Any, property: KProperty<*>): T {
            val properties = (thisRef as Properties)

            if (property.name !in properties && !isDefaultSet) {
                set(properties, property.name, defaultValue())
                isDefaultSet = true
            }

            return get(properties, property.name)
        }

        operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            set(thisRef as Properties, property.name, value)
        }
    }

    enum class Type {
        INT,
        FLOAT,
        BOOLEAN,
        STRING,
        PROPERTIES
    }

    private class Value(val name: String, val type: Type, val value: Any, val isArray: Boolean)

    private val values = hashMapOf<String, Value>()

    private fun setArrayValue(name: String, type: Type, value: Any) {
        values[name] = Value(name, type, value, true)
    }

    private fun setSingleValue(name: String, type: Type, value: Any) {
        values[name] = Value(name, type, value, false)
    }

    private fun getValue(name: String) = values[name]

    operator fun contains(name: String) = getValue(name) != null

    fun getType(name: String) = getValue(name)?.type

    fun isArray(name: String) = getValue(name)?.isArray == true

    fun setInt(name: String, value: Int) = setSingleValue(name, Type.INT, value)

    fun setFloat(name: String, value: Float) = setSingleValue(name, Type.FLOAT, value)

    fun setBoolean(name: String, value: Boolean) = setSingleValue(name, Type.BOOLEAN, value)

    fun setString(name: String, value: String) = setSingleValue(name, Type.STRING, value)

    fun setProperties(name: String, value: Properties) = setSingleValue(name, Type.PROPERTIES, value)

    fun setIntArray(name: String, value: Array<Int>) = setArrayValue(name, Type.INT, value)

    fun setFloatArray(name: String, value: Array<Float>) = setArrayValue(name, Type.FLOAT, value)

    fun setBooleanArray(name: String, value: Array<Boolean>) = setArrayValue(name, Type.BOOLEAN, value)

    fun setStringArray(name: String, value: Array<String>) = setArrayValue(name, Type.STRING, value)

    fun setPropertiesArray(name: String, value: Array<Properties>) = setArrayValue(name, Type.PROPERTIES, value)

    fun getInt(name: String): Int? {
        val value = getValue(name) ?: return null
        return value.value as? Int
    }

    fun getFloat(name: String): Float? {
        val value = getValue(name) ?: return null
        return value.value as? Float
    }

    fun getBoolean(name: String): Boolean? {
        val value = getValue(name) ?: return null
        return value.value as? Boolean
    }

    fun getString(name: String): String? {
        val value = getValue(name) ?: return null
        return value.value as? String
    }

    fun getProperties(name: String): Properties? {
        val value = getValue(name) ?: return null
        return value.value as? Properties
    }

    @Suppress("UNCHECKED_CAST")
    fun getIntArray(name: String): Array<Int>? {
        val value = getValue(name) ?: return null
        return value.value as? Array<Int>
    }

    @Suppress("UNCHECKED_CAST")
    fun getFloatArray(name: String): Array<Float>? {
        val value = getValue(name) ?: return null
        return value.value as? Array<Float>
    }

    @Suppress("UNCHECKED_CAST")
    fun getBooleanArray(name: String): Array<Boolean>? {
        val value = getValue(name) ?: return null
        return value.value as? Array<Boolean>
    }

    @Suppress("UNCHECKED_CAST")
    fun getStringArray(name: String): Array<String>? {
        val value = getValue(name) ?: return null
        return value.value as? Array<String>
    }

    @Suppress("UNCHECKED_CAST")
    fun getPropertiesArray(name: String): Array<Properties>? {
        val value = getValue(name) ?: return null
        return value.value as? Array<Properties>
    }

    fun write(): String = buildString { write(this) }

    fun write(builder: StringBuilder, level: Int = 0) {
        val indentation = buildString { repeat(level) { append('\t') } }

        values.forEach { (_, value) ->
            builder.append(indentation)
            builder.append(value.name)
            builder.append(":")
            builder.append(value.type.name.lowercase())
            builder.append("=")

            if (value.isArray) {
                val array = value.value as Array<*>
                when (value.type) {
                    Type.STRING -> builder.append(array.joinToString("\", \"", "[\"", "\"]"))
                    Type.PROPERTIES -> {
                        builder.appendLine("[")
                        var count = 0
                        array.forEach {
                            builder.appendLine("{")

                            (it as Properties).write(builder, level + 1)

                            if (count == array.lastIndex)
                                builder.appendLine("}")
                            else
                                builder.appendLine("},")

                            count++
                        }

                        builder.append("]")
                    }
                    else -> builder.append(array.joinToString(", ", "[", "]"))
                }
            } else {
                when (value.type) {
                    Type.STRING -> builder.append("\"${value.value}\"")
                    Type.PROPERTIES -> {
                        builder.appendLine("{")
                        (value.value as Properties).write(builder, level + 1)
                        builder.append(indentation)
                        builder.append("}")
                    }
                    else -> builder.append(value.value.toString())
                }
            }

            builder.appendLine()
        }
    }

    fun read(text: String) {
        val lines = text.lines()
        var lineIndex = 0

        while (lineIndex < lines.size) {
            val line = lines[lineIndex]

            if (line.isBlank()) {
                lineIndex++
                continue
            }

            val indexOfColon = line.indexOf(":")
            if (indexOfColon < 0)
                throw Exception("No ':' found in line '$line' ($lineIndex)")

            val indexOfEquals = line.indexOf("=")
            if (indexOfEquals < 0)
                throw Exception("No '=' found in line '$line' ($lineIndex)")

            val name = line.substring(0, indexOfColon).trim()
            val typeString = line.substring(indexOfColon + 1, indexOfEquals).trim()

            val type = enumValueOfOrNull<Type>(typeString.uppercase()) ?: throw Exception("No value found for '$typeString' ($lineIndex)")

            if (type == Type.PROPERTIES) {
                when (val indicatorChar = line.substring(indexOfEquals + 1).trim()) {
                    "{" -> {
                        val valueString = buildString {
                            while (true) {
                                lineIndex++

                                if (lineIndex >= lines.size)
                                    throw Exception("No closing bracket found for value '$name' ($lineIndex)")

                                val propertiesLine = lines[lineIndex].trim()

                                if (propertiesLine.startsWith("}"))
                                    break

                                appendLine(propertiesLine)
                            }
                        }

                        val properties = Properties()
                        properties.read(valueString)
                        setSingleValue(name, Type.PROPERTIES, properties)
                    }
                    "[" -> {
                        val list = arrayListOf<Properties>()
                        val builder = StringBuilder()
                        var isProperties = false
                        var hasProperties = false
                        lineIndex++

                        while (true) {
                            if (lineIndex >= lines.size)
                                throw Exception("No closing array bracket found for value '$name' ($lineIndex)")

                            val propertiesLine = lines[lineIndex].trim()

                            if (propertiesLine.startsWith("}")) {
                                if (!isProperties)
                                    throw Exception("No opening bracket found for value '$name' ($lineIndex)")

                                if (builder.isEmpty() && !hasProperties)
                                    throw Exception("No closing bracket found for value '$name' ($lineIndex)")

                                val properties = Properties()
                                properties.read(builder.toString())
                                list += properties
                                builder.clear()
                                hasProperties = false
                                isProperties = false

                                if (!propertiesLine.endsWith(",") || propertiesLine.endsWith("]"))
                                    break
                            }

                            if (isProperties)
                                builder.appendLine(propertiesLine)

                            if (propertiesLine == "{") {
                                isProperties = !isProperties
                                hasProperties = true
                            }

                            if (propertiesLine == "]") {
                                if (isProperties)
                                    throw Exception("No closing bracket found for value '$name' ($lineIndex)")

                                break
                            }

                            lineIndex++
                        }

                        setArrayValue(name, Type.PROPERTIES, list.toTypedArray())
                        lineIndex++
                    }
                    else -> throw Exception("Unknown indicator for '$name': '$indicatorChar ($lineIndex)")
                }
            } else {
                var valueString = line.substring(indexOfEquals + 1).trim()

                if (valueString.startsWith("[")) {
                    if (!valueString.endsWith("]"))
                        throw Exception("Arrays must end with ']' ($lineIndex)")

                    valueString = valueString.removeSurrounding("[", "]")

                    val array = when (type) {
                        Type.INT -> {
                            val list = arrayListOf<Int>()
                            var index = 0
                            val builder = StringBuilder()

                            while (index < valueString.length) {
                                val char = valueString[index]

                                if (char != ',')
                                    builder.append(char)

                                if (char == ',' || (index == valueString.lastIndex) && builder.isNotEmpty()) {
                                    val value = builder.toString().trim().toIntOrNull() ?: throw Exception("Failed conversion to int: ${builder.toString().trim()} ($lineIndex)")

                                    list += value
                                    builder.clear()
                                }

                                index++
                            }

                            list.toTypedArray()
                        }
                        Type.FLOAT -> {
                            val list = arrayListOf<Float>()
                            var index = 0
                            val builder = StringBuilder()

                            while (index < valueString.length) {
                                val char = valueString[index]

                                if (char != ',')
                                    builder.append(char)

                                if (char == ',' || (index == valueString.lastIndex) && builder.isNotEmpty()) {
                                    val value = builder.toString().trim().toFloatOrNull() ?: throw Exception("Failed conversion to float: ${builder.toString().trim()} ($lineIndex)")

                                    list += value
                                    builder.clear()
                                }


                                index++
                            }

                            list.toTypedArray()
                        }
                        Type.BOOLEAN -> {
                            val list = arrayListOf<Boolean>()
                            var index = 0
                            val builder = StringBuilder()

                            while (index < valueString.length) {
                                val char = valueString[index]

                                if (char != ',')
                                    builder.append(char)

                                if (char == ',' || (index == valueString.lastIndex) && builder.isNotEmpty()) {
                                    val value = builder.toString().trim().lowercase().toBooleanStrictOrNull() ?: throw Exception("Failed conversion to boolean: ${builder.toString().trim()} ($lineIndex)")

                                    list += value
                                    builder.clear()
                                }

                                index++
                            }

                            list.toTypedArray()
                        }
                        Type.STRING -> {
                            val list = arrayListOf<String>()
                            var index = 0
                            val builder = StringBuilder()
                            var isString = false
                            var hasString = false

                            while (index < valueString.length) {
                                val char = valueString[index]

                                if (char == '"') {
                                    isString = !isString
                                    hasString = true
                                } else if (isString)
                                    builder.append(char)

                                if (char == ',' || (index == valueString.lastIndex) && builder.isNotEmpty()) {
                                    if (isString)
                                        throw Exception("Strings must be surrounded by \" ($lineIndex)")

                                    if (builder.isEmpty() && !hasString)
                                        throw Exception("Invalid string array value ($lineIndex)")

                                    val value = builder.toString()

                                    list += value
                                    builder.clear()
                                    hasString = false
                                }

                                index++
                            }

                            list.toTypedArray()
                        }
                        else -> throw Exception("Unreachable")
                    }

                    setArrayValue(name, type, array)
                } else {
                    val value = when (type) {
                        Type.INT -> valueString.toInt()
                        Type.FLOAT -> valueString.toFloat()
                        Type.BOOLEAN -> valueString.toBoolean()
                        Type.STRING -> valueString.removeSurrounding("\"")
                        else -> throw Exception("Unreachable")
                    }

                    setSingleValue(name, type, value)
                }
            }

            lineIndex++
        }
    }

    fun clear() {
        values.clear()
    }
}

fun Properties.setVector2(name: String, vector: Vector2) = setFloatArray(name, vector.data)

fun Properties.setVector3(name: String, vector: Vector3) = setFloatArray(name, vector.data)

fun Properties.setVector4(name: String, vector: Vector4) = setFloatArray(name, vector.data)

fun Properties.setQuaternion(name: String, quaternion: Quaternion) = setFloatArray(name, quaternion.data)

fun Properties.getVector2(name: String, vector: Vector2 = Vector2()) = vector.also { getFloatArray(name)?.copyInto(it.data) }

fun Properties.getVector3(name: String, vector: Vector3 = Vector3()) = vector.also { getFloatArray(name)?.copyInto(it.data) }

fun Properties.getVector4(name: String, vector: Vector4 = Vector4()) = vector.also { getFloatArray(name)?.copyInto(it.data) }

fun Properties.setVector2i(name: String, vector: Vector2i) = setIntArray(name, vector.data)

fun Properties.setVector3i(name: String, vector: Vector3i) = setIntArray(name, vector.data)

fun Properties.setVector4i(name: String, vector: Vector4i) = setIntArray(name, vector.data)

fun Properties.getVector2i(name: String, vector: Vector2i = Vector2i()) = vector.also { getIntArray(name)?.copyInto(it.data) }

fun Properties.getVector3i(name: String, vector: Vector3i = Vector3i()) = vector.also { getIntArray(name)?.copyInto(it.data) }

fun Properties.getVector4i(name: String, vector: Vector4i = Vector4i()) = vector.also { getIntArray(name)?.copyInto(it.data) }

open class IntDelegate(defaultValue: () -> Int) : Properties.Delegate<Int>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getInt(name))

    override fun set(properties: Properties, name: String, value: Int) = properties.setInt(name, value)
}

open class FloatDelegate(defaultValue: () -> Float) : Properties.Delegate<Float>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getFloat(name))

    override fun set(properties: Properties, name: String, value: Float) = properties.setFloat(name, value)
}

open class BooleanDelegate(defaultValue: () -> Boolean) : Properties.Delegate<Boolean>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getBoolean(name))

    override fun set(properties: Properties, name: String, value: Boolean) = properties.setBoolean(name, value)
}

open class StringDelegate(defaultValue: () -> String) : Properties.Delegate<String>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getString(name))

    override fun set(properties: Properties, name: String, value: String) = properties.setString(name, value)
}

open class PropertiesDelegate(defaultValue: () -> Properties) : Properties.Delegate<Properties>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getProperties(name))

    override fun set(properties: Properties, name: String, value: Properties) = properties.setProperties(name, value)
}

open class IntArrayDelegate(defaultValue: () -> Array<Int>) : Properties.Delegate<Array<Int>>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getIntArray(name))

    override fun set(properties: Properties, name: String, value: Array<Int>) = properties.setIntArray(name, value)
}

open class FloatArrayDelegate(defaultValue: () -> Array<Float>) : Properties.Delegate<Array<Float>>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getFloatArray(name))

    override fun set(properties: Properties, name: String, value: Array<Float>) = properties.setFloatArray(name, value)
}

open class BooleanArrayDelegate(defaultValue: () -> Array<Boolean>) : Properties.Delegate<Array<Boolean>>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getBooleanArray(name))

    override fun set(properties: Properties, name: String, value: Array<Boolean>) = properties.setBooleanArray(name, value)
}

open class StringArrayDelegate(defaultValue: () -> Array<String>) : Properties.Delegate<Array<String>>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getStringArray(name))

    override fun set(properties: Properties, name: String, value: Array<String>) = properties.setStringArray(name, value)
}

open class PropertiesArrayDelegate(defaultValue: () -> Array<Properties>) : Properties.Delegate<Array<Properties>>(defaultValue) {
    override fun get(properties: Properties, name: String) = requireNotNull(properties.getPropertiesArray(name))

    override fun set(properties: Properties, name: String, value: Array<Properties>) = properties.setPropertiesArray(name, value)
}

abstract class VectorDelegate<T, V : Vector<T, V>>(val setDefaults: (V) -> Unit, val cachedValue: V) {
    var isDefaultSet = false

    abstract fun get(properties: Properties, name: String): Array<T>

    abstract fun set(properties: Properties, name: String, value: Array<T>)

    operator fun getValue(thisRef: Any, property: KProperty<*>): V {
        if (!isDefaultSet) {
            setDefaults(cachedValue)
            set(thisRef as Properties, property.name, cachedValue.data)
            isDefaultSet = true
        }

        get(thisRef as Properties, property.name).copyInto(cachedValue.data)
        return cachedValue
    }

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        set(thisRef as Properties, property.name, value.data)
    }
}

open class FloatVectorDelegate<V : FloatVector<V>>(setDefaults: (V) -> Unit, cachedValue: V) : VectorDelegate<Float, V>(setDefaults, cachedValue) {
    override fun set(properties: Properties, name: String, value: Array<Float>) = properties.setFloatArray(name, value)

    override fun get(properties: Properties, name: String) = requireNotNull(properties.getFloatArray(name))
}

open class IntVectorDelegate<V : IntVector<V>>(setDefaults: (V) -> Unit, cachedValue: V) : VectorDelegate<Int, V>(setDefaults, cachedValue) {
    override fun set(properties: Properties, name: String, value: Array<Int>) = properties.setIntArray(name, value)

    override fun get(properties: Properties, name: String) = requireNotNull(properties.getIntArray(name))
}

open class Vector2Delegate(setDefaults: (Vector2) -> Unit) : FloatVectorDelegate<Vector2>(setDefaults, Vector2())

open class Vector3Delegate(setDefaults: (Vector3) -> Unit) : FloatVectorDelegate<Vector3>(setDefaults, Vector3())

open class Vector4Delegate(setDefaults: (Vector4) -> Unit) : FloatVectorDelegate<Vector4>(setDefaults, Vector4())

open class Vector2iDelegate(setDefaults: (Vector2i) -> Unit) : IntVectorDelegate<Vector2i>(setDefaults, Vector2i())

open class Vector3iDelegate(setDefaults: (Vector3i) -> Unit) : IntVectorDelegate<Vector3i>(setDefaults, Vector3i())

open class Vector4iDelegate(setDefaults: (Vector4i) -> Unit) : IntVectorDelegate<Vector4i>(setDefaults, Vector4i())

fun int(defaultValue: () -> Int) = IntDelegate(defaultValue)

fun float(defaultValue: () -> Float) = FloatDelegate(defaultValue)

fun boolean(defaultValue: () -> Boolean) = BooleanDelegate(defaultValue)

fun string(defaultValue: () -> String) = StringDelegate(defaultValue)

fun properties(defaultValue: () -> Properties) = PropertiesDelegate(defaultValue)

fun intArray(defaultValue: () -> Array<Int>) = IntArrayDelegate(defaultValue)

fun floatArray(defaultValue: () -> Array<Float>) = FloatArrayDelegate(defaultValue)

fun booleanArray(defaultValue: () -> Array<Boolean>) = BooleanArrayDelegate(defaultValue)

fun stringArray(defaultValue: () -> Array<String>) = StringArrayDelegate(defaultValue)

fun propertiesArray(defaultValue: () -> Array<Properties>) = PropertiesArrayDelegate(defaultValue)

fun vector2(setDefaults: (Vector2) -> Unit) = Vector2Delegate(setDefaults)

fun vector3(setDefaults: (Vector3) -> Unit) = Vector3Delegate(setDefaults)

fun vector4(setDefaults: (Vector4) -> Unit) = Vector4Delegate(setDefaults)

fun vector2i(setDefaults: (Vector2i) -> Unit) = Vector2iDelegate(setDefaults)

fun vector3i(setDefaults: (Vector3i) -> Unit) = Vector3iDelegate(setDefaults)

fun vector4i(setDefaults: (Vector4i) -> Unit) = Vector4iDelegate(setDefaults)

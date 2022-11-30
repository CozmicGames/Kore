package com.cozmicgames.utils

import com.cozmicgames.Kore
import com.cozmicgames.log
import com.cozmicgames.utils.extensions.enumValueOfOrNull
import com.cozmicgames.utils.extensions.stringOrNull
import com.cozmicgames.utils.maths.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

open class Properties {
    private companion object {
        val prettyPrintJson = Json {
            prettyPrint = true
            allowSpecialFloatingPointValues = true
        }

        val normalPrintJson = Json {
            prettyPrint = false
            allowSpecialFloatingPointValues = true
        }
    }

    abstract class Delegate<T>(val defaultValue: () -> T) {
        private var isDefaultSet = false

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

    data class Value(val name: String, val type: Type, val value: Any, val isArray: Boolean)

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

    @Suppress("UNCHECKED_CAST")
    fun write(prettyPrint: Boolean = true): String {
        fun writeProperties(properties: Properties, builder: JsonObjectBuilder) {
            properties.values.forEach { (_, value) ->
                builder.putJsonObject(value.name) {
                    put("type", value.type.name)

                    if (value.isArray) {
                        when (value.type) {
                            Type.INT -> {
                                putJsonArray("array") {
                                    (value.value as Array<Int>).forEach {
                                        add(it)
                                    }
                                }
                            }
                            Type.FLOAT -> {
                                putJsonArray("array") {
                                    (value.value as Array<Float>).forEach {
                                        add(it)
                                    }
                                }
                            }
                            Type.BOOLEAN -> {
                                putJsonArray("array") {
                                    (value.value as Array<Boolean>).forEach {
                                        add(it)
                                    }
                                }
                            }
                            Type.STRING -> {
                                putJsonArray("array") {
                                    (value.value as Array<String>).forEach {
                                        add(it)
                                    }
                                }
                            }
                            Type.PROPERTIES -> {
                                putJsonArray("array") {
                                    (value.value as Array<Properties>).forEach {
                                        addJsonObject {
                                            writeProperties(it, this)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        when (value.type) {
                            Type.INT -> put("value", value.value as Int)
                            Type.FLOAT -> put("value", value.value as Float)
                            Type.BOOLEAN -> put("value", value.value as Boolean)
                            Type.STRING -> put("value", value.value as String)
                            Type.PROPERTIES -> putJsonObject("value") {
                                writeProperties(value.value as Properties, this)
                            }
                        }
                    }
                }
            }
        }

        val obj = buildJsonObject {
            writeProperties(this@Properties, this)
        }

        return if (prettyPrint)
            prettyPrintJson.encodeToString(obj)
        else
            normalPrintJson.encodeToString(obj)
    }

    fun read(text: String) {
        fun JsonPrimitive.string() = toString().removeSurrounding("\"")

        values.clear()

        val element = Json.parseToJsonElement(text)

        val obj = try {
            element.jsonObject
        } catch (e: Exception) {
            Kore.log.error(this::class, "Properties text does not represent a json object.")
            return
        }

        fun readProperties(obj: JsonObject, properties: Properties) {
            for ((name, valueElement) in obj) {
                val valueObj = try {
                    valueElement.jsonObject
                } catch (e: Exception) {
                    Kore.log.error(this::class, "Properties value does not represent a json object ($name).")
                    continue
                }

                val typePrimitive = try {
                    requireNotNull(valueObj["type"]).jsonPrimitive
                } catch (e: Exception) {
                    Kore.log.error(this::class, "Properties value type is invalid ($name, ${valueObj["type"]}).")
                    continue
                }

                val typeString = typePrimitive.stringOrNull

                if (typeString == null) {
                    Kore.log.error(this::class, "Properties value type is invalid ($name, $typePrimitive).")
                    continue
                }

                val type = enumValueOfOrNull<Type>(typePrimitive.string())

                if (type == null) {
                    Kore.log.error(this::class, "Properties value type is invalid ($name, $typePrimitive).")
                    continue
                }

                val isArray = "array" in valueObj

                when (type) {
                    Type.INT -> if (isArray) {
                        val array = try {
                            requireNotNull(valueObj["array"]).jsonArray
                        } catch (e: Exception) {
                            Kore.log.error(this::class, "Properties array is invalid ($name, ${valueObj["array"]}).")
                            continue
                        }

                        val list = arrayListOf<Int>()

                        for (arrayElement in array) {
                            val valuePrimitive = try {
                                arrayElement.jsonPrimitive
                            } catch (e: Exception) {
                                Kore.log.error(this::class, "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            val value = valuePrimitive.intOrNull

                            if (value == null) {
                                Kore.log.error(this::class, "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            list += value
                        }

                        properties.setArrayValue(name, type, list.toTypedArray())
                    } else {
                        val valuePrimitive = try {
                            requireNotNull(valueObj["value"]).jsonPrimitive
                        } catch (e: Exception) {
                            Kore.log.error(this::class, "Properties value is invalid ($name, ${valueObj["value"]}).")
                            continue
                        }

                        val value = valuePrimitive.intOrNull

                        if (value == null) {
                            Kore.log.error(this::class, "Properties value is invalid ($name, $valuePrimitive).")
                            continue
                        }

                        properties.setSingleValue(name, type, value)
                    }
                    Type.FLOAT -> if (isArray) {
                        val array = try {
                            requireNotNull(valueObj["array"]).jsonArray
                        } catch (e: Exception) {
                            Kore.log.error(this::class, "Properties array is invalid ($name, ${valueObj["array"]}).")
                            continue
                        }

                        val list = arrayListOf<Float>()

                        for (arrayElement in array) {
                            val valuePrimitive = try {
                                arrayElement.jsonPrimitive
                            } catch (e: Exception) {
                                Kore.log.error(this::class, "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            val value = valuePrimitive.floatOrNull

                            if (value == null) {
                                Kore.log.error(this::class, "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            list += value
                        }

                        properties.setArrayValue(name, type, list.toTypedArray())
                    } else {
                        val valuePrimitive = try {
                            requireNotNull(valueObj["value"]).jsonPrimitive
                        } catch (e: Exception) {
                            Kore.log.error(this::class, "Properties value is invalid ($name, ${valueObj["value"]}).")
                            continue
                        }

                        val value = valuePrimitive.floatOrNull

                        if (value == null) {
                            Kore.log.error(this::class, "Properties value is invalid ($name, $valuePrimitive).")
                            continue
                        }

                        properties.setSingleValue(name, type, value)
                    }
                    Type.BOOLEAN -> if (isArray) {
                        val array = try {
                            requireNotNull(valueObj["array"]).jsonArray
                        } catch (e: Exception) {
                            Kore.log.error(this::class, "Properties array is invalid ($name, ${valueObj["array"]}).")
                            continue
                        }

                        val list = arrayListOf<Boolean>()

                        for (arrayElement in array) {
                            val valuePrimitive = try {
                                arrayElement.jsonPrimitive
                            } catch (e: Exception) {
                                Kore.log.error(this::class, "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            val value = valuePrimitive.booleanOrNull

                            if (value == null) {
                                Kore.log.error(this::class, "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            list += value
                        }

                        properties.setArrayValue(name, type, list.toTypedArray())
                    } else {
                        val valuePrimitive = try {
                            requireNotNull(valueObj["value"]).jsonPrimitive
                        } catch (e: Exception) {
                            Kore.log.error(this::class, "Properties value is invalid ($name, ${valueObj["value"]}).")
                            continue
                        }

                        val value = valuePrimitive.booleanOrNull

                        if (value == null) {
                            Kore.log.error(this::class, "Properties value is invalid ($name, $valuePrimitive).")
                            continue
                        }

                        properties.setSingleValue(name, type, value)
                    }
                    Type.STRING -> if (isArray) {
                        val array = try {
                            requireNotNull(valueObj["array"]).jsonArray
                        } catch (e: Exception) {
                            Kore.log.error(this::class, "Properties array is invalid ($name, ${valueObj["array"]}).")
                            continue
                        }

                        val list = arrayListOf<String>()

                        for (arrayElement in array) {
                            val valuePrimitive = try {
                                arrayElement.jsonPrimitive
                            } catch (e: Exception) {
                                Kore.log.error(this::class, "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            val value = valuePrimitive.stringOrNull

                            if (value == null) {
                                Kore.log.error(this::class, "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            list += value
                        }

                        properties.setArrayValue(name, type, list.toTypedArray())
                    } else {
                        val valuePrimitive = try {
                            requireNotNull(valueObj["value"]).jsonPrimitive
                        } catch (e: Exception) {
                            Kore.log.error(this::class, "Properties value is invalid ($name, ${valueObj["value"]}).")
                            continue
                        }

                        val value = valuePrimitive.stringOrNull

                        if (value == null) {
                            Kore.log.error(this::class, "Properties value is invalid ($name, $valuePrimitive).")
                            continue
                        }

                        properties.setSingleValue(name, type, value)
                    }
                    Type.PROPERTIES -> if (isArray) {
                        val array = try {
                            requireNotNull(valueObj["array"]).jsonArray
                        } catch (e: Exception) {
                            Kore.log.error(this::class, "Properties array is invalid ($name, ${valueObj["array"]}).")
                            continue
                        }

                        val list = arrayListOf<Properties>()

                        for (arrayElement in array) {
                            val objValue = try {
                                arrayElement.jsonObject
                            } catch (e: Exception) {
                                Kore.log.error(this::class, "Properties array value is invalid ($name, $arrayElement).")
                                continue
                            }

                            val valueProperties = Properties()
                            readProperties(objValue, valueProperties)

                            list += valueProperties
                        }

                        properties.setArrayValue(name, type, list.toTypedArray())
                    } else {
                        val objValue = try {
                            requireNotNull(valueObj["value"]).jsonObject
                        } catch (e: Exception) {
                            Kore.log.error(this::class, "Properties value is invalid ($name, ${valueObj["value"]}).")
                            continue
                        }

                        val valueProperties = Properties()
                        readProperties(objValue, valueProperties)

                        properties.setSingleValue(name, type, valueProperties)
                    }
                }
            }
        }

        readProperties(obj, this)
    }

    fun clear() {
        values.clear()
    }

    fun set(properties: Properties) {
        values.putAll(properties.values)
    }

    override fun hashCode(): Int {
        return values.values.contentHashCode()
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

abstract class VectorDelegate<T, V : Vector<T, V>>(val setDefaults: (V) -> Unit, private val cachedValue: V) {
    private var isDefaultSet = false

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

open class ColorDelegate(setDefaults: (Color) -> Unit) : FloatVectorDelegate<Color>(setDefaults, Color())

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

fun color(setDefaults: (Color) -> Unit) = ColorDelegate(setDefaults)

fun Properties.readIntProperty(property: KMutableProperty0<Int>) {
    val value = getInt(property.name) ?: return
    property.set(value)
}

fun Properties.readFloatProperty(property: KMutableProperty0<Float>) {
    val value = getFloat(property.name) ?: return
    property.set(value)
}

fun Properties.readBooleanProperty(property: KMutableProperty0<Boolean>) {
    val value = getBoolean(property.name) ?: return
    property.set(value)
}

fun Properties.readStringProperty(property: KMutableProperty0<String>) {
    val value = getString(property.name) ?: return
    property.set(value)
}

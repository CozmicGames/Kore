package com.cozmicgames.graphics.gpu.pipeline

import com.cozmicgames.Kore
import com.cozmicgames.graphics.gpu.Pipeline
import com.cozmicgames.graphics.gpu.VertexLayout
import com.cozmicgames.graphics.gpu.layout.PackedVertexComponentBuilder
import com.cozmicgames.log
import com.cozmicgames.utils.Hex
import com.cozmicgames.utils.extensions.read32

sealed class DefinitionSection(val type: Type) {
    enum class Type {
        LAYOUT,
        TYPES,
        STATE,
        UNIFORMS,
        COMMON,
        VERTEX,
        GEOMETRY,
        FRAGMENT,
        COMPUTE
    }

    abstract fun parse(text: String)
}

class LayoutSection() : DefinitionSection(Type.LAYOUT) {
    constructor(text: String) : this() {
        parse(text)
    }

    var layout: VertexLayout? = null

    override fun parse(text: String) {
        fun parsePackedSection(text: String, builder: PackedVertexComponentBuilder) {
            val lines = text.lines()
            val iterator = lines.iterator()

            while (iterator.hasNext()) {
                val line = iterator.next().trim()

                val parts = line.split(" ").filterNot { it.isBlank() || it.isEmpty() }

                if (parts.size < 3) {
                    Kore.log.error(this::class, "Failed to parse packed layout attribute, packed attributes must specify their name and size: $line")
                    continue
                }

                val name = parts[1]
                val size = parts[2].toIntOrNull()

                if (size == null) {
                    Kore.log.error(this::class, "Failed to parse packed layout attribute, packed attributes must specify their name and size: $line")
                    continue
                }

                when {
                    parts[0].lowercase() == "byte" -> builder.packedByte(name, size)
                    parts[0].lowercase() == "short" -> builder.packedShort(name, size)
                    parts[0].lowercase() == "int" -> builder.packedInt(name, size)
                    parts[0].lowercase() == "ivec2" -> builder.packedIVec2(name, size)
                    parts[0].lowercase() == "ivec3" -> builder.packedIVec3(name, size)
                    parts[0].lowercase() == "ivec4" -> builder.packedIVec4(name, size)
                }
            }
        }

        val lines = text.lines()
        val iterator = lines.iterator()

        layout = VertexLayout {
            outer@ while (iterator.hasNext()) {
                val line = iterator.next().trim()
                if (line.isBlank() || line.isEmpty())
                    continue

                val parts = line.split(" ").filterNot { it.isBlank() || it.isEmpty() }

                if (parts[0].lowercase() == "packed") {
                    if (parts.getOrNull(1) != "{") {
                        Kore.log.error(this::class, "Failed to parse layout, packed attributes require an opening bracket: $line")
                        while (iterator.hasNext() && iterator.next() != "}")
                            continue@outer
                    }

                    val packedText = buildString {
                        while (true) {
                            val packedLine = iterator.next().trim()

                            if (packedLine.startsWith("}"))
                                break

                            if (!iterator.hasNext()) {
                                Kore.log.error(this::class, "Failed to parse layout, packed attributes require a closing bracket")
                                break
                            }

                            appendLine(packedLine)
                        }
                    }

                    packed {
                        parsePackedSection(packedText, this)
                    }
                } else if (parts[0].lowercase() == "normalized") {
                    if (parts.size < 3) {
                        Kore.log.error(this::class, "Failed to parse layout attribute, normalized attributes must specify their name and type: $line")
                        continue
                    }

                    val name = parts[2]
                    val type = enumValueOf<VertexLayout.AttributeType>(parts[3].uppercase())
                    when {
                        parts[1].lowercase() == "float" -> float(name, true, type)
                        parts[1].lowercase() == "vec2" -> vec2(name, true, type)
                        parts[1].lowercase() == "vec3" -> vec3(name, true, type)
                        parts[1].lowercase() == "vec4" -> vec4(name, true, type)
                    }
                } else {
                    if (parts.size < 2) {
                        Kore.log.error(this::class, "Failed to parse layout attribute, attributes must specify their name: $line")
                        continue
                    }

                    when (parts[0].lowercase()) {
                        "float" -> float(parts[1])
                        "vec2" -> vec2(parts[1])
                        "vec3" -> vec3(parts[1])
                        "vec4" -> vec4(parts[1])
                        "int" -> int(parts[1])
                        "short" -> short(parts[1])
                        "ivec2" -> {
                            if (parts.size > 2)
                                ivec2(parts[1], enumValueOf(parts[2].uppercase()))
                            else
                                ivec2(parts[1])
                        }
                        "ivec3" -> {
                            if (parts.size > 2)
                                ivec3(parts[1], enumValueOf(parts[2].uppercase()))
                            else
                                ivec3(parts[1])
                        }
                        "ivec4" -> {
                            if (parts.size > 2)
                                ivec4(parts[1], enumValueOf(parts[2].uppercase()))
                            else
                                ivec4(parts[1])
                        }
                    }
                }
            }
        }
    }
}

class TypesSection() : DefinitionSection(Type.TYPES) {
    constructor(text: String) : this() {
        parse(text)
    }

    companion object {
        val ARRAY_REGEX = Regex.fromLiteral("\\[([0-9]+)\\]")
    }

    val types = arrayListOf<TypeDefinition>()

    override fun parse(text: String) {
        val lines = text.lines()
        val iterator = lines.iterator()
        val sizes = TypeSizes()

        var struct: TypeDefinition? = null

        while (iterator.hasNext()) {
            val line = iterator.next().trim()

            if (line.isBlank() || line.isEmpty())
                continue

            val parts = line.split(" ").filterNot { it.isBlank() || it.isEmpty() }

            when {
                parts[0] == "struct" && parts[2] == "{" -> {
                    if (struct != null)
                        Kore.log.error(this::class, "Nested struct definition is not allowed")

                    struct = TypeDefinition(parts[1])
                }
                parts[0] == "}" -> {
                    if (struct == null)
                        Kore.log.error(this::class, "No current struct definition")

                    struct?.let {
                        types += it
                        sizes.register(it.name, it.dataSize)
                    }

                    struct = null
                }
                else -> {
                    if (struct == null)
                        Kore.log.error(this::class, "No current struct definition")

                    val type = parts[0]
                    val result = ARRAY_REGEX.find(parts[1])
                    if (result != null)
                        requireNotNull(struct).content += TypeDefinition.ArrayProperty(type, parts[1].removeSuffix(";").removeSuffix(result.value), result.groupValues[1].toInt(), sizes)
                    else
                        requireNotNull(struct).content += TypeDefinition.Property(type, parts[1].removeSuffix(";"), sizes)
                }
            }
        }
    }
}

class UniformsSection() : DefinitionSection(Type.UNIFORMS) {
    constructor(text: String) : this() {
        parse(text)
    }

    companion object {
        private val ARRAY_REGEX = Regex.fromLiteral("\\[[0-9]+\\]")
    }

    val uniforms = arrayListOf<UniformDefinition<*>>()

    override fun parse(text: String) {
        fun parseBuffer(name: String, text: String) {
            val content = arrayListOf<BufferUniformDefinition.Property>()

            val lines = text.lines()
            val iterator = lines.iterator()

            while (iterator.hasNext()) {
                val line = iterator.next().trim()

                if (line.isBlank() || line.isEmpty())
                    continue

                val parts = line.split(" ").filterNot { it.isBlank() || it.isEmpty() }

                val result = TypesSection.ARRAY_REGEX.find(parts[1])
                if (result != null)
                    content += BufferUniformDefinition.ArrayProperty(parts[0], parts[1].removeSuffix(";").removeSuffix(result.value), result.groupValues[1].toInt())
                else
                    content += BufferUniformDefinition.Property(parts[0], parts[1])
            }

            uniforms += BufferUniformDefinition(name, content.toTypedArray())
        }

        val lines = text.lines()
        val iterator = lines.iterator()

        outer@ while (iterator.hasNext()) {
            val line = iterator.next().trim()

            if (line.isBlank() || line.isEmpty())
                continue

            val parts = line.split(" ").filterNot { it.isBlank() || it.isEmpty() }

            if (parts[0] == "buffer") {
                val name = parts.getOrNull(1)
                if (name == null) {
                    Kore.log.error(this::class, "Failed to parse uniforms, buffers require a name: $line")
                    while (iterator.hasNext() && iterator.next() != "}")
                        continue@outer
                }

                if (parts.getOrNull(2) != "{") {
                    Kore.log.error(this::class, "Failed to parse uniforms, buffers require an opening bracket: $line")
                    while (iterator.hasNext() && iterator.next() != "}")
                        continue@outer
                }

                val bufferText = buildString {
                    while (true) {
                        val bufferLine = iterator.next().trim()

                        if (bufferLine.startsWith("}"))
                            break

                        if (!iterator.hasNext()) {
                            Kore.log.error(this::class, "Failed to parse uniforms, buffers require a closing bracket")
                            break
                        }

                        appendLine(bufferLine)
                    }
                }

                parseBuffer(requireNotNull(name), bufferText)
            } else {
                if (parts.size < 2) {
                    Kore.log.error(this::class, "Failed to parse uniform, uniforms must specify their name: $line")
                    continue
                }

                val name = parts[1]
                val size = ARRAY_REGEX.find(name)?.value?.toInt() ?: 1

                when (parts[0]) {
                    "float" -> uniforms += FloatUniformDefinition(name, size)
                    "vec2" -> uniforms += Vec2UniformDefinition(name, size)
                    "vec3" -> uniforms += Vec3UniformDefinition(name, size)
                    "vec4" -> uniforms += Vec4UniformDefinition(name, size)
                    "int" -> uniforms += IntUniformDefinition(name, size)
                    "ivec2" -> uniforms += IVec2UniformDefinition(name, size)
                    "ivec3" -> uniforms += IVec3UniformDefinition(name, size)
                    "ivec4" -> uniforms += IVec4UniformDefinition(name, size)
                    "bool" -> uniforms += BooleanUniformDefinition(name, size)
                    "sampler2D" -> uniforms += Texture2DUniformDefinition(name, size)
                    "sampler3D" -> uniforms += Texture3DUniformDefinition(name, size)
                    "samplerCube" -> uniforms += TextureCubeUniformDefinition(name, size)
                    "image2D" -> uniforms += Image2DUniformDefinition(name, enumValueOf(parts.getOrElse(2) { "RGBA8_UNORM" }.uppercase()))
                    "image3D" -> uniforms += Image3DUniformDefinition(name, enumValueOf(parts.getOrElse(2) { "RGBA8_UNORM" }.uppercase()))
                    "mat4" -> uniforms += MatrixUniformDefinition(name, size)
                }
            }
        }
    }
}

class StateSection() : DefinitionSection(Type.STATE) {
    constructor(text: String) : this() {
        parse(text)
    }

    var colorMask: Pipeline.ColorMask? = null
    var depthMask: Boolean? = null
    var stencilMask: Int? = null
    var depthState: Pipeline.DepthState? = null
    var stencilState: Pipeline.StencilState? = null
    var blendState: Pipeline.BlendState? = null
    var cullState: Pipeline.CullState? = null

    override fun parse(text: String) {
        val lines = text.lines()
        val iterator = lines.iterator()

        while (iterator.hasNext()) {
            val line = iterator.next().trim()

            if (line.isBlank() || line.isEmpty())
                continue

            val parts = line.split(" ").filterNot { it.isBlank() || it.isEmpty() }

            when (parts[0].lowercase()) {
                "cull" -> cullState = Pipeline.CullState(parts.find { it.lowercase() == "front" } != null, parts.find { it.lowercase() == "back" } != null)
                "blend" -> {
                    if (parts.size < 4)
                        Kore.log.error(this::class, "Failed to parse blend state: $line")
                    else {
                        val equation = enumValueOf<Pipeline.BlendState.Equation>(parts[1].uppercase())
                        val srcFactor = enumValueOf<Pipeline.BlendState.Factor>(parts[2].uppercase())
                        val destFactor = enumValueOf<Pipeline.BlendState.Factor>(parts[3].uppercase())
                        blendState = Pipeline.BlendState(equation, srcFactor, destFactor)
                    }
                }
                "colormask" -> {
                    if (parts.size < 5)
                        Kore.log.error(this::class, "Failed to parse colormask state: $line")
                    else
                        colorMask = Pipeline.ColorMask(parts[1].toBoolean(), parts[2].toBoolean(), parts[3].toBoolean(), parts[4].toBoolean())
                }
                "depthmask" -> {
                    if (parts.size < 2)
                        Kore.log.error(this::class, "Failed to parse depthmask state: $line")
                    else
                        depthMask = parts[1].toBoolean()
                }
                "depth" -> {
                    if (parts.size < 2)
                        Kore.log.error(this::class, "Failed to parse depthstate: $line")
                    else {
                        val func = enumValueOf<Pipeline.DepthState.Func>(parts[1].uppercase())
                        val min = parts.getOrNull(2)?.toFloat() ?: 0.0f
                        val max = parts.getOrNull(3)?.toFloat() ?: Float.MAX_VALUE
                        depthState = Pipeline.DepthState(func, min, max)
                    }
                }
                "stencilmask" -> {
                    if (parts.size < 2)
                        Kore.log.error(this::class, "Failed to parse stencilmask state: $line")
                    else {
                        val bytes = Hex.decode(parts[1])
                        stencilMask = read32(0) { bytes.getOrElse(it) { 0 } }
                    }
                }
                "stencil" -> {
                    if (parts.size < 7)
                        Kore.log.error(this::class, "Failed to parse stencil state: $line")
                    else {
                        val func = enumValueOf<Pipeline.StencilState.Func>(parts[1].uppercase())
                        val refBytes = Hex.decode(parts[2])
                        val failBytes = Hex.decode(parts[3])
                        val ref = read32(0) { refBytes.getOrElse(it) { 0 } }
                        val fail = read32(0) { failBytes.getOrElse(it) { 0 } }
                        val stencilFail = enumValueOf<Pipeline.StencilState.Operation>(parts[4].uppercase())
                        val depthFail = enumValueOf<Pipeline.StencilState.Operation>(parts[5].uppercase())
                        val depthPass = enumValueOf<Pipeline.StencilState.Operation>(parts[6].uppercase())
                        stencilState = Pipeline.StencilState(func, ref, fail, stencilFail, depthFail, depthPass)
                    }
                }
            }
        }
    }
}

class SourceSection(type: Type) : DefinitionSection(type) {
    constructor(text: String, type: Type) : this(type) {
        parse(text)
    }

    var source = ""

    override fun parse(text: String) {
        source = text
    }
}

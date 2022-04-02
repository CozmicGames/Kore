package com.gratedgames.graphics.gpu.pipeline

import com.gratedgames.memory.Memory

class TypeSizes {
    private val typeSizes = hashMapOf<String, Int>()

    init {
        typeSizes["bool"] = Memory.SIZEOF_INT
        typeSizes["float"] = Memory.SIZEOF_FLOAT
        typeSizes["vec2"] = Memory.SIZEOF_FLOAT * 2
        typeSizes["vec3"] = Memory.SIZEOF_FLOAT * 3
        typeSizes["vec4"] = Memory.SIZEOF_FLOAT * 4
        typeSizes["mat2"] = Memory.SIZEOF_FLOAT * 4
        typeSizes["mat3"] = Memory.SIZEOF_FLOAT * 9
        typeSizes["mat4"] = Memory.SIZEOF_FLOAT * 16
        typeSizes["int"] = Memory.SIZEOF_INT
        typeSizes["ivec2"] = Memory.SIZEOF_INT * 2
        typeSizes["ivec3"] = Memory.SIZEOF_INT * 3
        typeSizes["ivec4"] = Memory.SIZEOF_INT * 4
        typeSizes["uint"] = Memory.SIZEOF_INT
        typeSizes["bvec2"] = Memory.SIZEOF_INT * 2
        typeSizes["bvec3"] = Memory.SIZEOF_INT * 3
        typeSizes["bvec4"] = Memory.SIZEOF_INT * 4
    }

    fun getSize(name: String) = typeSizes[name]

    fun register(name: String, size: Int) {
        typeSizes[name] = size
    }
}
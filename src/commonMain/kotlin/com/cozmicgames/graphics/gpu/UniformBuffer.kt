package com.cozmicgames.graphics.gpu

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.memory.Memory
import com.cozmicgames.memory.Struct
import com.cozmicgames.utils.maths.*

class UniformBuffer(isStackAllocated: Boolean = false) : Struct(isStackAllocated) {
    enum class Layout {
        STD140,
        STD430,
        TIGHTLY_PACKED
    }

    class Float : Struct() {
        var value by float()

        init {
            if (Kore.graphics.uniformBufferLayout == Layout.STD140)
                padding(16 - Memory.SIZEOF_FLOAT)
        }
    }

    class Vec2 : Struct() {
        var x by float()
        var y by float()

        init {
            if (Kore.graphics.uniformBufferLayout == Layout.STD140)
                padding(16 - Memory.SIZEOF_FLOAT * 2)
        }

        fun set(v: Vector2) {
            x = v.x
            y = v.y
        }

        fun get(v: Vector2 = Vector2()): Vector2 {
            v.x = x
            v.y = y
            return v
        }
    }

    class Vec3 : Struct() {
        var x by float()
        var y by float()
        var z by float()

        init {
            if (Kore.graphics.uniformBufferLayout != Layout.TIGHTLY_PACKED)
                padding(16 - Memory.SIZEOF_FLOAT)
        }

        fun set(v: Vector3) {
            x = v.x
            y = v.y
            z = v.z
        }

        fun get(v: Vector3 = Vector3()): Vector3 {
            v.x = x
            v.y = y
            v.z = z
            return v
        }
    }

    class Vec4 : Struct() {
        var x by float()
        var y by float()
        var z by float()
        var w by float()

        fun set(v: Vector4) {
            x = v.x
            y = v.y
            z = v.z
            w = v.w
        }

        fun get(v: Vector4 = Vector4()): Vector4 {
            v.x = x
            v.y = y
            v.z = z
            v.w = w
            return v
        }
    }

    class Int : Struct() {
        var value by int()

        init {
            if (Kore.graphics.uniformBufferLayout == Layout.STD140)
                padding(16 - Memory.SIZEOF_INT)
        }
    }

    class IVec2 : Struct() {
        var x by int()
        var y by int()

        init {
            if (Kore.graphics.uniformBufferLayout == Layout.STD140)
                padding(16 - Memory.SIZEOF_INT * 2)
        }

        fun set(v: Vector2i) {
            x = v.x
            y = v.y
        }

        fun get(v: Vector2i = Vector2i()): Vector2i {
            v.x = x
            v.y = y
            return v
        }
    }

    class IVec3 : Struct() {
        var x by int()
        var y by int()
        var z by int()

        init {
            if (Kore.graphics.uniformBufferLayout != Layout.TIGHTLY_PACKED)
                padding(16 - Memory.SIZEOF_INT)
        }

        fun set(v: Vector3i) {
            x = v.x
            y = v.y
            z = v.z
        }

        fun get(v: Vector3i = Vector3i()): Vector3i {
            v.x = x
            v.y = y
            v.z = z
            return v
        }
    }

    class IVec4 : Struct() {
        var x by int()
        var y by int()
        var z by int()
        var w by int()

        fun set(v: Vector4i) {
            x = v.x
            y = v.y
            z = v.z
            w = v.w
        }

        fun get(v: Vector4i = Vector4i()): Vector4i {
            v.x = x
            v.y = y
            v.z = z
            v.w = w
            return v
        }
    }

    class Mat4 : Struct() {
        var m00 by float()
        var m01 by float()
        var m02 by float()
        var m03 by float()
        var m10 by float()
        var m11 by float()
        var m12 by float()
        var m13 by float()
        var m20 by float()
        var m21 by float()
        var m22 by float()
        var m23 by float()
        var m30 by float()
        var m31 by float()
        var m32 by float()
        var m33 by float()

        fun set(matrix: Matrix4x4) {
            m00 = matrix.m00
            m01 = matrix.m01
            m02 = matrix.m02
            m03 = matrix.m03
            m10 = matrix.m10
            m11 = matrix.m11
            m12 = matrix.m12
            m13 = matrix.m13
            m20 = matrix.m20
            m21 = matrix.m21
            m22 = matrix.m22
            m23 = matrix.m23
            m30 = matrix.m30
            m31 = matrix.m31
            m32 = matrix.m32
            m33 = matrix.m33
        }

        fun get(matrix: Matrix4x4 = Matrix4x4()): Matrix4x4 {
            matrix.m00 = m00
            matrix.m01 = m01
            matrix.m02 = m02
            matrix.m03 = m03
            matrix.m10 = m10
            matrix.m11 = m11
            matrix.m12 = m12
            matrix.m13 = m13
            matrix.m20 = m20
            matrix.m21 = m21
            matrix.m22 = m22
            matrix.m23 = m23
            matrix.m30 = m30
            matrix.m31 = m31
            matrix.m32 = m32
            matrix.m33 = m33
            return matrix
        }
    }
}

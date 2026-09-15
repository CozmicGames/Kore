package com.cozmicgames.core.utils.maths

fun packFloatToHalfFloat(value: Float): Short {
    val bits = value.toBits()
    val sign = (bits ushr 16) and 0x8000
    val valExp = ((bits ushr 23) and 0xFF) - 112

    if (valExp <= 0) {
        if (valExp < -10)
            return sign.toShort()

        var mantissa = (bits and 0x007FFFFF) or 0x00800000
        val shift = 1 - valExp
        mantissa = if (shift < 24)
            mantissa ushr shift
        else
            0

        return (sign or (mantissa ushr 13)).toShort()
    } else if (valExp == 143) {
        if ((bits and 0x007FFFFF) != 0)
            return (sign or 0x7E00 or ((bits and 0x007FFFFF) ushr 13)).toShort() // NaN

        return (sign or 0x7C00).toShort() // Infinity
    } else {
        if (valExp > 30)
            return (sign or 0x7C00).toShort() // Overflow to infinity

        return (sign or (valExp shl 10) or ((bits and 0x007FFFFF) ushr 13)).toShort()
    }
}

fun unpackHalfFloatToFloat(packedShort: Short): Float {
    val bits = packedShort.toInt() and 0xFFFF
    val sign = (bits and 0x8000) shl 16
    val exp = (bits and 0x7C00) ushr 10
    val mantissa = bits and 0x03FF

    val finalBits = if (exp == 0) {
        if (mantissa == 0)
            // Signed Zero
            sign
        else {
            // Subnormal / Denormalized half-float -> Normal 32-bit float
            var m = mantissa
            var e = 0
            while ((m and 0x0400) == 0) {
                m = m shl 1
                e++
            }
            val newExp = (127 - 15 - e + 1) shl 23
            val newMantissa = (m and 0x03FF) shl 13
            sign or newExp or newMantissa
        }
    } else if (exp == 0x1F) {
        if (mantissa == 0)
            // Infinity
            sign or 0x7F800000
        else
            // NaN
            sign or 0x7F800000 or (mantissa shl 13)
    } else {
        // Normalized number
        val newExp = (exp + 112) shl 23
        val newMantissa = mantissa shl 13
        sign or newExp or newMantissa
    }

    return Float.fromBits(finalBits)
}
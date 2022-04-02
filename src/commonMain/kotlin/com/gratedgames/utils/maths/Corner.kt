package com.gratedgames.utils.maths

interface Corner {
    val ordinal: Int
}

enum class Corners : Corner {
    UPPER_LEFT,
    LOWER_LEFT,
    UPPER_RIGHT,
    LOWER_RIGHT;

    companion object {
        const val NONE = 0
        val ALL = combine(*values())

        fun combine(vararg corners: Corner): Int {
            var flags = 0
            corners.forEach {
                flags = flags or (1 shl it.ordinal)
            }
            return flags
        }
    }
}

operator fun Int.contains(corner: Corner) = (this and (1 shl corner.ordinal)) != 0

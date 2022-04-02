package com.gratedgames.utils

fun hashCodeOf(vararg values: Any): Int {
    if (values.isEmpty())
        return 0

    var hash = 1
    values.forEach {
        hash = 31 * hash + it.hashCode()
    }

    return hash
}
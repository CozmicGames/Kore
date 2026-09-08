package com.cozmicgames.core.utils

class Version(val major: Int, val minor: Int, val patch: Int, val type: Type) : Comparable<Version> {
    enum class Type {
        ALPHA,
        BETA,
        RELEASE
    }

    override fun compareTo(other: Version): Int {
        if (major != other.major)
            return major - other.major
        if (minor != other.minor)
            return minor - other.minor
        if (patch != other.patch)
            return patch - other.patch
        if (type != other.type)
            return type.compareTo(other.type)
        return 0
    }

    override fun toString(): String {
        return "$major.$minor.$patch - $type"
    }
}
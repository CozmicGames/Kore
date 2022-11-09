package com.cozmicgames.utils

import kotlin.random.Random

class UUID(data: CharArray) {
    companion object {
        private val CHARS = "0123456789abcdefghijklmnopqrstuvwxyz"
        private val SIZE = 16
        private val REGEX = Regex("[a-z0-9]{$SIZE}")

        fun randomUUID(random: Random = Random): UUID {
            return UUID(CharArray(SIZE) { CHARS.random(random) })
        }

        operator fun invoke(string: String): UUID {
            if (REGEX.matchEntire(string.lowercase()) == null)
                throw RuntimeException("Invalid uuid string: $string")

            return UUID(string.lowercase().toCharArray())
        }
    }

    private val data = data.joinToString(separator = "")

    override fun toString(): String {
        return data
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UUID

        if (data != other.data) return false

        return true
    }
}
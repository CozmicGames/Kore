package com.gratedgames.utils

internal expect class PlatformTime() {
    fun getCurrent(): Double
}

inline fun durationOf(unit: TimeUnit = TimeUnit.SECONDS, block: () -> Unit): Double {
    val begin = Time.current
    block()
    return Time.convert(Time.current - begin, TimeUnit.SECONDS, unit)
}

val Number.seconds get() = this.toDouble() / TimeUnit.SECONDS.factor
val Number.milliseconds get() = this.toDouble() / TimeUnit.MILLISECONDS.factor
val Number.microseconds get() = this.toDouble() / TimeUnit.MICROSECONDS.factor
val Number.nanoseconds get() = this.toDouble() / TimeUnit.NANOSECONDS.factor

enum class TimeUnit(val factor: Double) {
    SECONDS(1.0),
    MILLISECONDS(1000.0),
    MICROSECONDS(1000000.0),
    NANOSECONDS(1000000000.0)
}

object Time {
    private val platformTime = PlatformTime()

    val current get() = platformTime.getCurrent()

    fun convert(value: Double, from: TimeUnit, to: TimeUnit) = value / from.factor * to.factor
}
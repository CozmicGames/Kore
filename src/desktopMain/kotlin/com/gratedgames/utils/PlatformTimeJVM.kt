package com.gratedgames.utils

internal actual class PlatformTime actual constructor() {
    actual fun getCurrent() = Time.convert(System.nanoTime().toDouble(), TimeUnit.NANOSECONDS, TimeUnit.SECONDS)
}
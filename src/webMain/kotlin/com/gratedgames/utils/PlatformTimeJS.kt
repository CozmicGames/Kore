package com.gratedgames.utils

import kotlin.js.Date

internal actual class PlatformTime actual constructor() {
    actual fun getCurrent() = Date().getTime()
}
package com.cozmicgames.core

import com.cozmicgames.core.utils.Version

class ApplicationInfo {
    companion object {
        operator fun invoke(block: ApplicationInfo.() -> Unit): ApplicationInfo {
            val info = ApplicationInfo()
            block(info)
            return info
        }
    }

    var applicationName: String = ""
    var applicationVersion: Version? = null
    var engineName: String? = null
    var engineVersion: Version? = null
    var isDebug: Boolean = false
}
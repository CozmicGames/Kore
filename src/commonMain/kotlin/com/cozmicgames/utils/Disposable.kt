package com.cozmicgames.utils

interface Disposable {
    fun dispose()
}

inline fun <T : Disposable, R> T.use(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        dispose()
    }
}

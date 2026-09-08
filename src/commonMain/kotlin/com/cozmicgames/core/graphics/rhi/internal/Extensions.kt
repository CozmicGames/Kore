package com.cozmicgames.core.graphics.rhi.internal

import com.cozmicgames.core.graphics.rhi.GPUDevice
import com.cozmicgames.core.graphics.rhi.GPUDevice.DebugHandler

internal fun GPUDevice.checkError(value: Boolean, message: () -> String) {
    if (!value)
        debugHandler?.onDebugMessage(DebugHandler.Severity.ERROR, message())
}

internal fun GPUDevice.checkFail(value: Boolean, message: () -> String) {
    if (!value)
        debugHandler?.onDebugMessage(DebugHandler.Severity.FAIL, message())
}

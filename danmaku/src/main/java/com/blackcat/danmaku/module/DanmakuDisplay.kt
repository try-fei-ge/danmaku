package com.blackcat.danmaku.module

import androidx.annotation.WorkerThread

@WorkerThread
class DanmakuDisplay {
    internal var changeEnable = false
    companion object {
        var NO_FACE_VERSION = Int.MIN_VALUE
    }

    var faceVersion : Int = NO_FACE_VERSION
        set(value) {
            if (changeEnable) field = value
        }

    var faceWidth : Int = 0
        set(value) {
            if (changeEnable) field = value
        }

    var faceHeight : Int = 0
        set(value) {
            if (changeEnable) field = value
        }

    fun isMeasured() : Boolean {
        return faceVersion != NO_FACE_VERSION
    }
}
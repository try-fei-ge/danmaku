package com.blackcat.danmaku

import android.util.Log

internal class Print {
    companion object {
        fun printE(tag: String, msg: String) {
            if (DanmakuView.LOG_ENABLE) {
                Log.e(tag, msg)
            }
        }

        fun throwAny(throwable: Throwable) {
            Log.e("Run", "throw " + throwable)
            throw throwable
        }
    }
}
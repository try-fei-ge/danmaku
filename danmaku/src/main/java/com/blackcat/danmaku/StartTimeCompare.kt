package com.blackcat.danmaku

import android.util.Log
import com.blackcat.danmaku.module.Danmaku

class StartTimeCompare : Comparator<Danmaku<*, *>> {
    override fun compare(p0: Danmaku<*, *>?, p1: Danmaku<*, *>?): Int {
        if (p0 == p1) return 0
        if (p0 == null) return -1
        if (p1 == null) return 1
        val interval = p0.startTime - p1.startTime
        return when {
            interval > 0 -> 1
            interval < 0 -> -1
            else -> 0
        }
    }
}
package com.blackcat.danmaku.module.track

import android.text.TextPaint
import com.blackcat.danmaku.module.ShareElement

class TrackShareElement() : ShareElement() {
    val paintMap : HashMap<Float, TextPaint> = HashMap()

    override fun isFinal(): Boolean {
        return true
    }
}
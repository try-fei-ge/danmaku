package com.blackcat.danmaku.face

import android.graphics.Canvas
import androidx.annotation.MainThread
import java.util.*

class FrameFace {
    var time: Long = 0
    var displayFaceVersion : Int = 0
    val danmakuSetLayers : LinkedList<LinkedList<DanmakuFrame>> = LinkedList()

    @MainThread
    fun draw(canvas: Canvas) {
        for (layer in danmakuSetLayers) {
            for (frame in layer) {
                frame.danmaku?.run {
                    draw(canvas, frame.deliver, frame.bound)
                }
            }
        }
    }
}
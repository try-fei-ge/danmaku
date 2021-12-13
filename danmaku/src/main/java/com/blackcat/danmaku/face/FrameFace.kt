package com.blackcat.danmaku.face

import android.graphics.Canvas
import android.util.Log
import androidx.annotation.MainThread
import com.blackcat.danmaku.module.Danmaku
import com.blackcat.danmaku.module.DanmakuDisplay
import java.util.*

class FrameFace {
    var time: Long = 0
    var displayFaceVersion : Int = 0
    val danmakuSetLayers : LinkedList<LinkedList<DanmakuFrame>> = LinkedList()
    @Volatile internal var isDrawing = false

    companion object {
        var lastDrawLeft = 0F
    }
    @MainThread
    fun draw(canvas: Canvas) {
        for (layer in danmakuSetLayers) {
            for (frame in layer) {
                frame.danmaku?.run {
                    Log.e("Run", "draw1 " + lastDrawLeft + " " + frame.bound.left + " " + (frame.bound.left - lastDrawLeft))
                    draw(canvas, frame.deliver, frame.bound)
                    lastDrawLeft = frame.bound.left
                }
            }
        }
    }
}
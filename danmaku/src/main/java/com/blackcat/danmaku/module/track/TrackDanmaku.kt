package com.blackcat.danmaku.module.track

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import com.blackcat.danmaku.module.Danmaku
import com.blackcat.danmaku.module.DanmakuDisplay
import kotlin.math.ceil

class TrackDanmaku(charSequence: CharSequence, val number: Int, textSize: Float)
    : Danmaku<TrackShareElement, TrackDeliver>(TRACK, charSequence, textSize) {
    companion object {
        const val MIN_VX = 0.3f
        const val MAX_VX = 0.35f
    }

    private var textColor: Int = Color.BLACK
    private var vx: Float = MIN_VX
    private var duration : Long = 0
    private var showDuration : Long = 0
    var track : Track ?= null

    fun getShowDuration() : Long {
        return showDuration
    }

    fun getDuration() : Long {
        return duration
    }

    fun getVx() : Float {
        return vx
    }

    override fun onEnterScreen() {
       Log.e("Run", "enter " + content)
    }

    override fun onLeaveScreen() {
        Log.e("Run", "leave " + content)
    }

    override fun onMeasure(
        danmakuDisplay: DanmakuDisplay,
        shareElement: TrackShareElement?
    ): TrackDeliver? {
        return shareElement?.let {
            var paint : TextPaint? = it.paintMap[textSize]
            if (paint == null) {
                paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
                paint.textSize = textSize
                it.paintMap[textSize] = paint
            }
            val staticLayout = StaticLayout(content, paint,
                ceil(StaticLayout.getDesiredWidth(content, paint)).toInt(),
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true)
            width = staticLayout.width
            height = staticLayout.height
            vx = when {
                danmakuDisplay.faceWidth == 0 -> {
                    MIN_VX
                }
                width > danmakuDisplay.faceWidth -> {
                    MAX_VX
                }
                else -> {
                    width / danmakuDisplay.faceWidth * (MAX_VX - MIN_VX) + MIN_VX
                }
            }
            showDuration = ((danmakuDisplay.faceWidth + width) / vx).toLong()
            duration = (width / vx).toLong()
            TrackDeliver(staticLayout)
        }
    }

    override fun onLayout(time: Long, bound: RectF, deliver: TrackDeliver?, danmakuDisplay: DanmakuDisplay) : Boolean {
        val useTrack : Track = track ?: return false
        val realTime = time - startTime
        bound.top = useTrack.contourLine - height / 2f
        bound.bottom = bound.top + height
        bound.left = danmakuDisplay.faceWidth - realTime * vx
        bound.right = bound.left + width
        return !(bound.right <= 0 || bound.left >= danmakuDisplay.faceWidth)
    }

    override fun inScreen(time: Long, danmakuDisplay: DanmakuDisplay): Boolean {
        if (!isMeasured(danmakuDisplay)) return false
        val realTime = time - startTime
        val left = danmakuDisplay.faceWidth - realTime * vx
        val right = left + width
        return !(right <= 0 || left >= danmakuDisplay.faceWidth)
    }

    override fun onDraw(
        canvas: Canvas,
        deliver: TrackDeliver?,
        shareElement: TrackShareElement?,
        bound: RectF
    ) {
        deliver?.let {
            val count = canvas.save()
            canvas.translate(bound.left, bound.top)
            it.staticLayout.paint.color = textColor
            it.staticLayout.draw(canvas)
            canvas.restoreToCount(count)
        }
    }
}
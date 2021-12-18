package com.blackcat.danmaku

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.blackcat.danmaku.face.FrameFace
import com.blackcat.danmaku.invalidate.FrameCall
import com.blackcat.danmaku.invalidate.RefreshMachine
import com.blackcat.danmaku.module.Danmaku
import com.blackcat.danmaku.module.DanmakuContainer
import com.blackcat.danmaku.module.DanmakuDisplay
import com.blackcat.danmaku.module.DrawSchedule

class DanmakuView : View {
    companion object STATIC_PARAM {
        val TAG : String = "DanmakuView"
        var LOG_ENABLE : Boolean = true
    }

    private val danmakuTimer = DanmakuTimerImpl()
    private val danmakuTimerWarp by lazy {
        object : DanmakuTimer {
            override fun start() {
                Print.printE(TAG, "无效调用 - timer.start()")
            }

            override fun stop() {
                Print.printE(TAG, "无效调用 - timer.stop()")
            }

            override fun getCurrentTime(): Long {
                return danmakuTimer.getCurrentTime()
            }
        }
    }
    private val danmakuContext = DanmakuContext(DanmakuDisplay())
    private val danmakuSchedule : DrawSchedule = DrawSchedule(danmakuContext, this)
    private var drawFrame : FrameFace ?= null

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) refresh()
    }

    override fun onDraw(canvas: Canvas?) {
        drawFrame?.let { if (canvas != null) it.draw(canvas) }
    }

    fun config(
        danmakuContainerInit : (() -> List<DanmakuContainer<*>>?)? = null
    ) {
        danmakuSchedule.danmakuContainerInit = danmakuContainerInit
    }

    fun prepare() {
        danmakuSchedule.prepare()
    }

    fun start() {
        if (danmakuSchedule.isPrepared()) danmakuTimer.start()
    }

    fun seek(duration: Int) {
        var time = danmakuTimer.getCurrentTime() + duration
        time = if (time < 0) 0L else time
        if (time != danmakuTimer.getCurrentTime()) {
            danmakuTimer.seekTo(time)
            refresh()
        }
    }

    fun stop() {
        if (danmakuSchedule.isPrepared()) danmakuTimer.stop()
    }

    fun release() {
        danmakuSchedule.release()
        danmakuTimer.resetTime()
        refresh()
    }

    fun addDanmaku(danmaku: Danmaku<*, *>?) {
        if (danmaku == null) return
        danmakuSchedule.addDanmaku(danmaku)
    }

    fun getTimer() : DanmakuTimer {
        return danmakuTimerWarp
    }

    private fun refresh() {
        drawFrame = null
        danmakuSchedule.displayUpdate(width, height)
        invalidate()
    }

    private fun drawFrame(time: Long) {
        danmakuSchedule.run {
            this.getFrame(drawFrame)?.let {
                drawFrame = it
                invalidate()
            }
            this.timeUpdate(time)
        }
    }

    private inner class DanmakuTimerImpl : FrameCall, DanmakuTimer {
        @Volatile private var currentTime: Long = 0
        private var lastFrameTime: Long = DanmakuContext.NO_TIME
        private var isWorking: Boolean = false

        fun resetTime() {
            stop()
            currentTime = 0
        }

        fun seekTo(time: Long) {
            if (time == currentTime) return
            currentTime = time
            this@DanmakuView.drawFrame(this.currentTime)
        }

        override fun doFrame(currentTime: Long) {
            val interval = if (lastFrameTime == DanmakuContext.NO_TIME) 0 else currentTime - lastFrameTime
            this.currentTime += interval
            lastFrameTime = currentTime
            this@DanmakuView.drawFrame(this.currentTime)
        }

        override fun start() {
            if (isWorking) return
            isWorking = true
            lastFrameTime = DanmakuContext.NO_TIME
            invalidate()
            RefreshMachine.instance.registerFrame(this)
        }

        override fun stop() {
            if (isWorking) {
                isWorking = false
                RefreshMachine.instance.unregisterFrame(this)
            }
        }

        override fun getCurrentTime(): Long {
            return currentTime
        }
    }
}
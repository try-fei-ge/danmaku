package com.blackcat.danmaku

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.View
import com.blackcat.danmaku.face.FrameFace
import com.blackcat.danmaku.invalidate.FrameCall
import com.blackcat.danmaku.invalidate.RefreshMachine
import com.blackcat.danmaku.module.Danmaku
import com.blackcat.danmaku.module.DanmakuContainer
import com.blackcat.danmaku.module.DanmakuDisplay
import com.blackcat.danmaku.module.DrawSchedule
import com.blackcat.danmaku.module.track.Track

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
    private val danmakuContext = DanmakuContext(DanmakuDisplay(), getTimer())
    private val danmakuSchedule : DrawSchedule = DrawSchedule(danmakuContext, this)

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            danmakuSchedule.displayUpdate(width, height)
        }
    }
    var lastDrawTime = 0L
    override fun onDraw(canvas: Canvas?) {
        danmakuSchedule.getFrame()?.let {
            Log.e("Run", "draw2 " + it.time + " " + lastDrawTime + " " + (lastDrawTime - it.time))
            if (canvas != null) it.draw(canvas)
            lastDrawTime = it.time
        }
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

    fun stop() {
        if (danmakuSchedule.isPrepared()) danmakuTimer.stop()
    }

    fun release() {
        danmakuSchedule.release()
        danmakuTimer.resetTime()
    }

    fun addDanmaku(danmaku: Danmaku<*, *>?) {
        if (danmaku == null) return
        danmakuSchedule.addDanmaku(danmaku)
    }

    fun getTimer() : DanmakuTimer {
        return danmakuTimerWarp
    }

    private fun drawFrame() {
        danmakuSchedule.run {
            timeUpdate()
        }
    }

    private inner class DanmakuTimerImpl : FrameCall, DanmakuTimer {
        @Volatile private var currentTime: Long = 0
        private var lastFrameTime: Long = -1
        private var isWorking: Boolean = false

        fun resetTime() {
            currentTime = 0
        }

        override fun doFrame(currentTime: Long) {
            val interval = if (lastFrameTime == -1L) 0 else currentTime - lastFrameTime
            this.currentTime += interval
            lastFrameTime = currentTime
            this@DanmakuView.drawFrame()
        }

        override fun start() {
            if (isWorking) return
            isWorking = true
            lastFrameTime = -1
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
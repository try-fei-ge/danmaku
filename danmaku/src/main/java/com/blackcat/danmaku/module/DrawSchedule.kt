package com.blackcat.danmaku.module

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.core.view.isInvisible
import com.blackcat.danmaku.DanmakuContainerInit
import com.blackcat.danmaku.DanmakuContext
import com.blackcat.danmaku.DanmakuView
import com.blackcat.danmaku.face.DanmakuFrame
import com.blackcat.danmaku.face.FrameFace

internal class DrawSchedule constructor(val danmakuContext: DanmakuContext, val danmakuView: DanmakuView) {
    private var isPrepared: Boolean = false
    private val mainHandler : Handler = Handler(Looper.getMainLooper())
    private var handler: DrawHandler ?= null
    private var handlerThread: HandlerThread ?= null
    private var pendingMeasure : PendingMeasure ?= null
    @Volatile internal var danmakuContainerInit : (() -> List<DanmakuContainer<*>>?) ?= null

    fun prepare() {
        if (isPrepared) return
        object : HandlerThread("danmaku") {
            override fun run() {
                danmakuContext.danmakuContainerList.set(danmakuContainerInit?.invoke())
                try {
                    super.run()
                } finally {
                    danmakuContext.danmakuContainerList.remove()
                }
            }
        }.let {
            it.start()
            handlerThread = it
            handler = DrawHandler(it.looper, danmakuContext)
            it.isAlive
        }
        val pending = pendingMeasure
        pending?.let { displayUpdate(it.width, it.height)}
        pendingMeasure = null
        isPrepared = true
    }

    fun release() {
        if (isPrepared) return
        handler?.run {
            removeCallbacksAndMessages(null)
        }
        handler!!.removeCallbacksAndMessages(null)
        handlerThread!!.quit()
        handlerThread = null
        handler = null
        isPrepared = false
        danmakuView.invalidate()
    }

    fun getFrame(time: Long, failureFace: FrameFace?) : FrameFace? {
        if (!isPrepared) return null
        return handler!!.exchangeArea.getDrawFrame(time, failureFace)
    }

    fun refresh() {
        val display = danmakuContext.danmakuDisplay
        if (display.isMeasured()) displayUpdate(display.faceWidth, display.faceHeight)
    }

    fun isPrepared() : Boolean {
        return isPrepared
    }

    fun timeUpdate(time: Long) {
        if (isPrepared) {
            handler!!.run {
                sendMessage(obtainMessage(DrawHandler.TIME_UPDATE, time))
            }
        }
    }

    fun displayUpdate(width: Int, height: Int) {
        if (isPrepared) {
            handler!!.run {
                removeMessages(DrawHandler.DISPLAY_UPDATE)
                sendMessageAtFrontOfQueue(obtainMessage(DrawHandler.DISPLAY_UPDATE, width, height))
            }
        } else {
            pendingMeasure = PendingMeasure(width, height)
        }
    }

    fun addDanmaku(danmaku: Danmaku<*, *>) {
        if (isPrepared) {
            handler!!.run {
                sendMessage(obtainMessage(DrawHandler.ADD, danmaku))
            }
        }
    }
}

private data class PendingMeasure(
    val width: Int,
    val height: Int
)
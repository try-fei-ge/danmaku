package com.blackcat.danmaku.module

import android.os.HandlerThread
import com.blackcat.danmaku.DanmakuContext
import com.blackcat.danmaku.DanmakuView
import com.blackcat.danmaku.face.FrameFace

internal class DrawSchedule constructor(val danmakuContext: DanmakuContext, val danmakuView: DanmakuView) {
    private var isPrepared: Boolean = false
    private var handler: DrawHandler ?= null
    private var handlerThread: HandlerThread ?= null
    private var pendingMeasure : PendingMeasure ?= null
    @Volatile private var prepareTime = 0L
    internal var danmakuContainerInit : (() -> List<DanmakuContainer<*>>?) ?= null

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
            handler = DrawHandler(it.looper, danmakuContext) { prepareTime }
            it.isAlive
        }
        isPrepared = true
        val pending = pendingMeasure
        pending?.let { displayUpdate(it.width, it.height)}
        pendingMeasure = null
    }

    fun release() {
        if (!isPrepared) return
        handler?.run {
            removeCallbacksAndMessages(null)
        }
        handler!!.removeCallbacksAndMessages(null)
        handlerThread!!.quit()
        handlerThread = null
        handler = null
        prepareTime = 0L
        isPrepared = false
        danmakuView.invalidate()
    }

    fun getFrame(failureFace: FrameFace?) : FrameFace? {
        if (!isPrepared) return null
        return handler!!.exchangeArea.getDrawFrame(prepareTime, failureFace)
    }

    fun isPrepared() : Boolean {
        return isPrepared
    }

    fun timeUpdate(time: Long) {
        if (isPrepared) {
            handler!!.run {
                prepareTime = time
                sendMessage(obtainMessage(DrawHandler.TIME_UPDATE))
            }
        }
    }

    fun displayUpdate(width: Int, height: Int) {
        if (isPrepared) {
            handler!!.run {
                exchangeArea.clearFrame()
                removeMessages(DrawHandler.TIME_UPDATE)
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
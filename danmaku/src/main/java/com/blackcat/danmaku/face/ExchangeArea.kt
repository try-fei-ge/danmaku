package com.blackcat.danmaku.face

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import java.util.concurrent.ConcurrentLinkedQueue

internal class ExchangeArea {
    private val busyQueue : ConcurrentLinkedQueue<FrameFace> = ConcurrentLinkedQueue()
    private val idleQueue : ConcurrentLinkedQueue<FrameFace> = ConcurrentLinkedQueue()
    var arriveCallback: ((Boolean) -> Unit) ?= null
    private var drawFrame : FrameFace ?= null
        @Synchronized set
        @Synchronized get

    @MainThread
    fun getDrawFrame() : FrameFace? {
        return drawFrame
    }

    @MainThread
    fun syncNext() : Boolean {
        if (busyQueue.size > 0) {
            drawFrame = busyQueue.poll()
            return true
        }
        return false
    }

    @MainThread
    fun trySyncNext() {
        if (drawFrame?.isDrawing == true) {
            busyQueue.peek()?.let {
                val frame = drawFrame
                idleQueue.offer(frame)
                drawFrame = it
                busyQueue.poll()
                arriveCallback?.run { invoke(false) }
            }
        }
    }

    @WorkerThread
    fun joinWorkFrame(frameFace: FrameFace) {
        if (drawFrame == null || drawFrame!!.isDrawing) {
            val newFrame = busyQueue.poll() ?: frameFace
            newFrame.isDrawing = false
            drawFrame = newFrame
        } else {
            frameFace.isDrawing = false
            busyQueue.offer(frameFace)
        }
        arriveCallback?.run { invoke(false) }
    }

    @MainThread
    fun recyclerFrame(frameFace: FrameFace) {
        idleQueue.offer(frameFace)
    }

    @WorkerThread
    fun getClearFrame() : FrameFace {
        return idleQueue.poll() ?: FrameFace()
    }

    fun clearFrame() {
        drawFrame = null
        busyQueue.clear()
        arriveCallback?.run { invoke(true) }
    }
}
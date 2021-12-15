package com.blackcat.danmaku.face

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import java.util.concurrent.ConcurrentLinkedQueue

internal class ExchangeArea {
    private val busyQueue : ConcurrentLinkedQueue<FrameFace> = ConcurrentLinkedQueue()
    private val idleQueue : ConcurrentLinkedQueue<FrameFace> = ConcurrentLinkedQueue()

    @MainThread
    fun getDrawFrame(time: Long, failureFace: FrameFace?) : FrameFace? {
        var preFrame : FrameFace? = failureFace
        var newFrame : FrameFace?
        while (true) {
            newFrame = busyQueue.peek()
            if (newFrame == null) {
                newFrame = preFrame
                break
            }
            if (newFrame.time == time) {
                busyQueue.poll()
                break
            }
            if (newFrame.time > time) {
                newFrame = preFrame
                break
            }
            busyQueue.poll()
            preFrame = newFrame
        }
        return newFrame
    }

    @WorkerThread
    fun joinWorkFrame(frameFace: FrameFace) {
        busyQueue.offer(frameFace)
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
        busyQueue.clear()
    }
}
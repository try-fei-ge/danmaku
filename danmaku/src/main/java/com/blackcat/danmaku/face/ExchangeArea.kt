package com.blackcat.danmaku.face

import java.util.concurrent.ConcurrentLinkedQueue

internal class ExchangeArea {
    private val busyQueue : ConcurrentLinkedQueue<FrameFace> = ConcurrentLinkedQueue()
    private val idleQueue : ConcurrentLinkedQueue<FrameFace> = ConcurrentLinkedQueue()

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
                preFrame?.let { recyclerFrame(it) }
                busyQueue.poll()
                break
            }
            if (newFrame.time > time) {
                newFrame = preFrame
                break
            }
            busyQueue.poll()
            preFrame?.let { recyclerFrame(it) }
            preFrame = newFrame
        }
        return newFrame
    }

    fun joinWorkFrame(frameFace: FrameFace) {
        busyQueue.offer(frameFace)
    }

    fun recyclerFrame(frameFace: FrameFace) {
        idleQueue.offer(frameFace)
    }

    fun getClearFrame() : FrameFace {
        return idleQueue.poll() ?: FrameFace()
    }

    fun clearFrame() {
        busyQueue.clear()
    }
}
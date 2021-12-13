package com.blackcat.danmaku.face

import android.graphics.RectF
import android.util.Log
import com.blackcat.danmaku.DanmakuContext
import com.blackcat.danmaku.module.Danmaku
import com.blackcat.danmaku.module.DanmakuDisplay
import java.util.*
import kotlin.collections.HashMap

class DanmakuScreen {
    private val danmakuLayers : LinkedList<LinkedList<Danmaku<*, *>>> = LinkedList()
    private val danmakuFrameSetLayer : LinkedList<DanmakuFrame> = LinkedList()
    private var danmakuSetLayer : LinkedList<Danmaku<*, *>> ?= null
    private var frameSetLayer : LinkedList<DanmakuFrame> ?= null
    private var layerIndex = 0
    private var frame : FrameFace ?= null
    private var danmakuDisplay : DanmakuDisplay ?= null
    private val tempBound = RectF()
    var screenTime = DanmakuContext.NO_TIME

    fun clearScreen() {
        screenTime = DanmakuContext.NO_TIME
    }

    fun newEdit(frame: FrameFace, currentTime: Long, danmakuDisplay: DanmakuDisplay) {
        this.frame = frame
        this.screenTime = currentTime
        this.danmakuDisplay = danmakuDisplay
        layerIndex = -1

        val frameFaceIterator = frame.danmakuSetLayers.listIterator()
        val layerIterator = danmakuLayers.iterator()
        while (layerIterator.hasNext()) {
            val layer = layerIterator.next()
            val iterator = layer.iterator()
            var frameIterator : MutableListIterator<DanmakuFrame> ?= null
            while (iterator.hasNext()) {
                val danmaku = iterator.next()
                if (danmaku.danmakuContainer == null) {
                    danmaku.onLeaveScreen()
                    iterator.remove()
                    continue
                }
                if (!danmaku.isMeasured(danmakuDisplay)) {
                    danmaku.measure(danmakuDisplay)
                }
                if (frameIterator == null) {
                    frameIterator = if (frameFaceIterator.hasNext()) {
                        frameFaceIterator.next().listIterator()
                    } else {
                        val list = LinkedList<DanmakuFrame>()
                        frameFaceIterator.add(list)
                        list.listIterator()
                    }
                }
                val danmakuFrame : DanmakuFrame? = if (frameIterator.hasNext()) {
                        val danmakuFrame = frameIterator.next()
                        if (danmaku.layout(currentTime, danmakuFrame.bound, danmakuDisplay)) {
                            danmakuFrame
                        } else {
                            danmaku.onLeaveScreen()
                            frameIterator.remove()
                            iterator.remove()
                            null
                        }
                    } else {
                        if (danmaku.layout(currentTime, tempBound, danmakuDisplay)) {
                            val danmakuFrame = DanmakuFrame()
                            danmakuFrame.bound.set(tempBound)
                            frameIterator.add(danmakuFrame)
                            danmakuFrame
                        } else {
                            danmaku.onLeaveScreen()
                            iterator.remove()
                            null
                        }
                    }
                danmakuFrame?.let {
                    it.danmaku = danmaku
                    it.deliver = danmaku.deliver
                }
            }
            frameIterator?.let {
                while (it.hasNext()) {
                    danmakuFrameSetLayer.add(it.next())
                    it.remove()
                }
            }
        }
        while (frameFaceIterator.hasNext()) {
            frameFaceIterator.next().let {
                for (danmakuFrame in it) danmakuFrameSetLayer.add(danmakuFrame)
            }
            frameFaceIterator.remove()
        }
    }

    fun nextLayer() {
        layerIndex += 1
        if (layerIndex < danmakuLayers.size) {
            danmakuSetLayer = danmakuLayers[layerIndex]
        } else {
            while (layerIndex >= danmakuLayers.size) {
                danmakuSetLayer = LinkedList()
                danmakuLayers.add(danmakuSetLayer!!)
            }
        }
        frame?.run {
            if (layerIndex < danmakuSetLayers.size) {
                frameSetLayer = danmakuSetLayers[layerIndex]
            } else {
                while (layerIndex >= danmakuSetLayers.size) {
                    frameSetLayer = LinkedList()
                    danmakuSetLayers.add(frameSetLayer!!)
                }
            }
        }
    }

    fun addDanmaku(danmakuList: List<Danmaku<*, *>>) {
        if (danmakuSetLayer == null || frameSetLayer == null || danmakuDisplay == null) return
        val display = this.danmakuDisplay!!
        for (danmaku in danmakuList) {
            if (danmaku.danmakuContainer == null) continue
            if (!danmaku.isMeasured(display)) {
                danmaku.measure(display)
            }
            var frame : DanmakuFrame? = danmakuFrameSetLayer.poll()
            if (frame == null) frame = DanmakuFrame()
            if (danmaku.layout(screenTime, frame.bound, display)) {
                frame.deliver = danmaku.deliver
                frame.danmaku = danmaku
                danmaku.onEnterScreen()
                danmakuSetLayer!!.add(danmaku)
                frameSetLayer!!.add(frame)
            }
        }
    }

    fun endEdit(){
        frame = null
        danmakuSetLayer = null
        frameSetLayer = null
        danmakuDisplay = null
        danmakuFrameSetLayer.clear()
    }
}
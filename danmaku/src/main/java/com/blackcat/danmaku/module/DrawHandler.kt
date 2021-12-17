package com.blackcat.danmaku.module

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.blackcat.danmaku.DanmakuContext
import com.blackcat.danmaku.Print
import com.blackcat.danmaku.face.DanmakuScreen
import com.blackcat.danmaku.face.ExchangeArea
import java.util.*

internal class DrawHandler(looper: Looper, val danmakuContext: DanmakuContext, val timeFetch : (() -> Long)) : Handler(looper) {
    companion object WHAT_OF {
        const val ADD = 1
        const val TIME_UPDATE = 2
        const val DISPLAY_UPDATE = 3
    }

    private val danmakuScreen = DanmakuScreen()
    val exchangeArea = ExchangeArea()

    override fun handleMessage(msg: Message) {
        try {
            when(msg.what) {
                ADD -> {
                    val danmaku: Danmaku<*, *> = msg.obj as Danmaku<*, *>
                    val danmakuContainerList = danmakuContext.danmakuContainerList.get()
                    danmakuContainerList?.let {
                        for (container in it) {
                            if (container.supportDanmaku(danmaku)) {
                                container.addDanmaku(danmaku)
                            }
                        }
                    }
                    if (hasMessages(TIME_UPDATE)) {
                        removeMessages(TIME_UPDATE)
                        makeFrame(timeFetch.invoke())
                    }
                }
                DISPLAY_UPDATE -> {
                    val display = danmakuContext.danmakuDisplay
                    display.changeEnable = true
                    if (display.faceWidth != msg.arg1 || display.faceHeight != msg.arg2) {
                        display.faceWidth = msg.arg1
                        display.faceHeight = msg.arg2
                        display.faceVersion += 1
                        danmakuContext.danmakuContainerList.get()?.let {
                            for (danmakuContainer in it) danmakuContainer.onDisplayUpdate(display)
                        }
                    }
                    display.changeEnable = false
                    danmakuScreen.clearScreen()
                    exchangeArea.clearFrame()
                    val time = timeFetch.invoke() - 1
                    makeFrame(if (time < 0) 0 else time)
                    if (hasMessages(TIME_UPDATE)) {
                        removeMessages(TIME_UPDATE)
                        makeFrame(timeFetch.invoke())
                    }
                }
                TIME_UPDATE -> {
                    makeFrame(timeFetch.invoke())
                }
            }
        } catch (throwable : Throwable) {
            Print.throwAny(throwable)
        }
    }

    private fun makeFrame(currentTime: Long) {
        if (currentTime <= danmakuScreen.screenTime) return
        val display = danmakuContext.danmakuDisplay
        val danmakuContainerList = danmakuContext.danmakuContainerList.get()
        if (!display.isMeasured() || danmakuContainerList == null || danmakuContainerList.isEmpty()) {
            danmakuScreen.screenTime = currentTime
            return
        }
        val lastTime = danmakuScreen.screenTime
        val init = lastTime == DanmakuContext.NO_TIME
        val frameFace = exchangeArea.getClearFrame()
        danmakuScreen.newEdit(frameFace, currentTime, display)
        val tempList = LinkedList<Danmaku<*, *>>()
        for (danmakuContainer in danmakuContainerList) {
            danmakuScreen.nextLayer()
            if (init) danmakuContainer.fetchDanmakuInit(currentTime, display, tempList)
            else danmakuContainer.fetchDanmaku(lastTime, currentTime, display, tempList)
            danmakuScreen.addDanmaku(tempList)
            tempList.clear()
        }
        frameFace.time = danmakuScreen.screenTime
        frameFace.displayFaceVersion = display.faceVersion
        danmakuScreen.endEdit()
        if (hasMessages(DISPLAY_UPDATE)) exchangeArea.recyclerFrame(frameFace)
        else {
            if (init) exchangeArea.recyclerFrame(frameFace)
            else exchangeArea.joinWorkFrame(frameFace)
        }
    }
}
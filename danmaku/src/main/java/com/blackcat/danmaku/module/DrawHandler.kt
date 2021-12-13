package com.blackcat.danmaku.module

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.blackcat.danmaku.DanmakuContext
import com.blackcat.danmaku.Print
import com.blackcat.danmaku.face.DanmakuScreen
import com.blackcat.danmaku.face.ExchangeArea
import java.util.*
import kotlin.math.abs

internal class DrawHandler(looper: Looper, val danmakuContext: DanmakuContext) : Handler(looper) {
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
                        var index = 0
                        for (container in it) {
                            if (container.supportDanmaku(danmaku)) {
                                container.addDanmaku(danmaku)
                            }
                            index += 1
                        }
                    }
                    checkFrameProtect()
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
                    makeFrame()
                }
                TIME_UPDATE -> {
                    makeFrame()
                }
            }
        } catch (throwable : Throwable) {
            Print.throwAny(throwable)
        }
    }

    private fun makeFrame() {
        val display = danmakuContext.danmakuDisplay
        val currentTime = danmakuContext.danmakuTimer.getCurrentTime()
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
            if (!init || !hasMessages(TIME_UPDATE)) exchangeArea.joinWorkFrame(frameFace)
        }
    }

    private fun checkFrameProtect() {
        if (danmakuScreen.screenTime == DanmakuContext.NO_TIME || danmakuContext.danmakuFrameProtected == DanmakuContext.NO_TIME) return
        if (abs(danmakuContext.danmakuTimer.getCurrentTime() - danmakuScreen.screenTime) > danmakuContext.danmakuFrameProtected) {
            makeFrame()
        }
    }
}
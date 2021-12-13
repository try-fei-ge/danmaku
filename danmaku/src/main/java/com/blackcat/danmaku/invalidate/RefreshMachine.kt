package com.blackcat.danmaku.invalidate

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.Choreographer
import kotlin.collections.ArrayList

class RefreshMachine private constructor() {
    private var currentTime: Long = SystemClock.uptimeMillis()
    private val frameProvider: FrameProvider
    private val frameCallSet: ArrayList<FrameCall> = ArrayList()

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RefreshMachine()
        }
    }

    init {
        frameProvider = if (Build.VERSION.SDK_INT >= 16) FrameProvider16() else FrameProvider14()
    }

    fun registerFrame(frameCall: FrameCall) {
        if (!frameCallSet.contains(frameCall)) {
            frameCallSet.add(frameCall)
            frameProvider.postFrameCallback()
        }
    }

    fun unregisterFrame(frameCall: FrameCall) {
        frameCallSet.remove(frameCall)
    }

    private fun dispatchFrame() {
        currentTime = SystemClock.uptimeMillis()
        for (frameCall in frameCallSet) frameCall.doFrame(currentTime)
        if (frameCallSet.size > 0) frameProvider.postFrameCallback()
    }

    private inner class FrameProvider14 : FrameProvider {
        private val updateRunnable = Runnable {
            dispatchFrame()
        }
        private val updateHandler = Handler(Looper.getMainLooper())
        private var lastFrameTime = -1L

        override fun postFrameCallback() {
            val delay = 15L - (SystemClock.uptimeMillis() - lastFrameTime)
            updateHandler.postDelayed(updateRunnable, Math.max(0L, delay))
        }
    }

    private inner class FrameProvider16 : FrameProvider {
        private val choreographer = Choreographer.getInstance()
        private val choreographerCallback = Choreographer.FrameCallback {
                dispatchFrame()
        }

        override fun postFrameCallback() {
            choreographer.postFrameCallback(choreographerCallback)
        }
    }
}

private interface FrameProvider {
    fun postFrameCallback()
}

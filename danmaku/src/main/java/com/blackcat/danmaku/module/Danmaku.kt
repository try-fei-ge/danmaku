package com.blackcat.danmaku.module

import android.graphics.Canvas
import android.graphics.RectF
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread

abstract class Danmaku<S : ShareElement, D : Deliver<S>> constructor(
    val type: Int,
    val content: CharSequence,
    val textSize: Float,
) {
    companion object TYPE_OF {
        val EMPTY = 0
        val TRACK = 1
    }
    var startTime: Long = 0
        get() = field + startOffset
    var startOffset: Long = 0

    private var measureVersion = DanmakuDisplay.NO_FACE_VERSION
    private var enableSizeChange : Boolean = false
    var width  = 0
        set(value) {
            if (enableSizeChange) field = value
        }
    var height = 0
        set(value) {
            if (enableSizeChange) field = value
        }
    internal var danmakuContainer : DanmakuContainer<*> ?= null
    internal var deliver : D ?= null

    @WorkerThread
    internal fun measure(danmakuDisplay: DanmakuDisplay) {
        val myShare = try {
            @Suppress("UNCHECKED_CAST")
            if (danmakuContainer == null) null else {
                danmakuContainer?.let {
                    it.verifyShareElement(danmakuDisplay.faceVersion)
                    it.shareElement as S
                }
            }
        } catch (cast : ClassCastException) {
            null
        }
        enableSizeChange = true
        deliver = onMeasure(danmakuDisplay, myShare)
        deliver?.shareElement = myShare
        enableSizeChange = false
        measureVersion = danmakuDisplay.faceVersion
    }

    internal fun layout(time: Long, bound: RectF, danmakuDisplay: DanmakuDisplay) : Boolean {
        return onLayout(time, bound, deliver, danmakuDisplay)
    }

    internal fun draw(canvas: Canvas, deliver : Deliver<*>?, bound: RectF) {
        val myDeliver = try {
            @Suppress("UNCHECKED_CAST")
            if (deliver == null) null else deliver as D
        } catch (cast : ClassCastException) {
            null
        }

        onDraw(canvas, myDeliver, myDeliver?.shareElement, bound)
    }

    fun isMeasured(danmakuDisplay: DanmakuDisplay) : Boolean {
        return measureVersion != DanmakuDisplay.NO_FACE_VERSION && measureVersion == danmakuDisplay.faceVersion
    }

    protected fun enableSizeChange() : Boolean {
        return enableSizeChange
    }

    @WorkerThread
    open fun onEnterScreen() {}

    @WorkerThread
    open fun onLeaveScreen() {}

    @WorkerThread
    abstract fun onMeasure(danmakuDisplay: DanmakuDisplay, shareElement: S?) : D?

    @WorkerThread
    abstract fun onLayout(time: Long, bound: RectF, deliver: D?, danmakuDisplay: DanmakuDisplay) : Boolean

    @MainThread
    abstract fun onDraw(canvas: Canvas, deliver : D?, shareElement: S?, bound: RectF)

    abstract fun inScreen(time: Long, danmakuDisplay: DanmakuDisplay) : Boolean
}
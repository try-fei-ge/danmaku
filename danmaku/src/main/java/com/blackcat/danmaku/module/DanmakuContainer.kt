package com.blackcat.danmaku.module

import androidx.annotation.CallSuper
import androidx.annotation.WorkerThread
import java.util.*

@WorkerThread
abstract class DanmakuContainer<S : ShareElement> {
    var shareElement : S? = null
    var shareElementIsInit = false
    private val freezeList : LinkedList<Danmaku<*, *>> by lazy { LinkedList() }

    /**
     * 创建弹幕间共享元素
     */
    protected open fun createShareElement() : S? {
        return null
    }

    internal fun verifyShareElement(disPlayVersion: Int) {
        if (!shareElementIsInit) {
            shareElementIsInit = true
            shareElement = createShareElement()?.apply { faceVersion = disPlayVersion }
            return
        }
        if (shareElement == null || shareElement!!.isFinal()) return
        if (shareElement!!.faceVersion != disPlayVersion) {
            shareElement = createShareElement()?.apply { faceVersion = disPlayVersion }
        }
    }

    /**
     * 暂未支持
     */
    internal fun freeze(danmaku: Danmaku<*, *>) {
        freezeList.add(danmaku)
        onDanmakuFreeze(danmaku)
    }

    /**
     * 暂未支持
     */
    internal fun releaseFreeze(danmaku: Danmaku<*, *>) {
        if (freezeList.remove(danmaku)) onDanmakuReleaseFreeze(danmaku)
    }

    /**
     * 暂未支持
     */
    internal fun releaseFreezeAll() {
        for (danmaku in freezeList) {
            onDanmakuReleaseFreeze(danmaku)
        }
        freezeList.clear()
    }

    /**
     * 当弹幕由冻结状态至普通状态时回调
     */
    open fun onDanmakuFreeze(danmaku: Danmaku<*, *>) {}

    /**
     * 当弹幕由普通状态到冻结状态时回调
     */
    open fun onDanmakuReleaseFreeze(danmaku: Danmaku<*, *>) {}

    /**
     * 当屏幕尺寸发生变化时回调
     */
    open fun onDisplayUpdate(danmakuDisplay: DanmakuDisplay) {}

    /**
     * 当弹幕添加时回掉
     */
    @CallSuper
    open fun addDanmaku(danmaku: Danmaku<*, *>) {
        danmaku.danmakuContainer = this
    }

    /**
     * 返回是否支持当前弹幕
     */
    abstract fun supportDanmaku(danmaku: Danmaku<*, *>) : Boolean

    /**
     * 拿取指定时间段内的弹幕
     */
    abstract fun fetchDanmaku(startTime: Long, endTime : Long, danmakuDisplay: DanmakuDisplay, list: LinkedList<Danmaku<*, *>>)

    /**
     * 拿取指定时间以前的弹幕，在屏幕发生变化或时间发生不连续时调用，此时屏幕处于清空状态可以允许更耗时的计算，更新屏幕展示
     */
    open fun fetchDanmakuInit(currentTime: Long, danmakuDisplay: DanmakuDisplay, list: LinkedList<Danmaku<*, *>>) {
        fetchDanmaku(0, currentTime, danmakuDisplay, list)
    }
}

/**
 * 用于在不同弹幕之间进行元素共享的容器，默认这些共享元素将受到屏幕尺寸的影响，当屏幕尺寸改变后将由[DanmakuContainer]
 * 创建新的，可以使用[isFinal]指定不受屏幕尺寸影响
 */
abstract class ShareElement {
    internal var faceVersion = DanmakuDisplay.NO_FACE_VERSION

    open fun isFinal() : Boolean {
        return false
    }
}

/**
 * 在[Danmaku.onMeasure]时被创建，用于将测量后的结果交付给[Danmaku.onLayout]和[Danmaku.onDraw]
 */
abstract class Deliver<S : ShareElement> {
    internal var shareElement : S ?= null
}
package com.blackcat.danmaku.module

import androidx.annotation.CallSuper
import androidx.annotation.WorkerThread
import java.util.*

@WorkerThread
abstract class DanmakuContainer<S : ShareElement> {
    var shareElement : S? = null
    var shareElementIsInit = false
    private val freezeList : LinkedList<Danmaku<*, *>> by lazy { LinkedList() }

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

    internal fun freeze(danmaku: Danmaku<*, *>) {
        freezeList.add(danmaku)
        onDanmakuFreeze(danmaku)
    }

    internal fun releaseFreeze(danmaku: Danmaku<*, *>) {
        if (freezeList.remove(danmaku)) onDanmakuReleaseFreeze(danmaku)
    }

    internal fun releaseFreezeAll() {
        for (danmaku in freezeList) {
            onDanmakuReleaseFreeze(danmaku)
        }
        freezeList.clear()
    }

    open fun onDanmakuFreeze(danmaku: Danmaku<*, *>) {}

    open fun onDanmakuReleaseFreeze(danmaku: Danmaku<*, *>) {}

    open fun onDisplayUpdate(danmakuDisplay: DanmakuDisplay) {}

    @CallSuper
    open fun addDanmaku(danmaku: Danmaku<*, *>) {
        danmaku.danmakuContainer = this
    }

    abstract fun supportDanmaku(danmaku: Danmaku<*, *>) : Boolean

    abstract fun fetchDanmaku(startTime: Long, endTime : Long, danmakuDisplay: DanmakuDisplay, list: LinkedList<Danmaku<*, *>>)

    open fun fetchDanmakuInit(currentTime: Long, danmakuDisplay: DanmakuDisplay, list: LinkedList<Danmaku<*, *>>) {
        fetchDanmaku(0, currentTime, danmakuDisplay, list)
    }
}

abstract class ShareElement {
    internal var faceVersion = DanmakuDisplay.NO_FACE_VERSION

    open fun isFinal() : Boolean {
        return false
    }
}

abstract class Deliver<S : ShareElement> {
    internal var shareElement : S ?= null
}
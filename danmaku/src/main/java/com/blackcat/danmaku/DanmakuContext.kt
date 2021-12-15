package com.blackcat.danmaku

import com.blackcat.danmaku.module.DanmakuContainer
import com.blackcat.danmaku.module.DanmakuDisplay
import kotlin.collections.HashMap

class DanmakuContext internal constructor(
    val danmakuDisplay : DanmakuDisplay) {
    companion object {
        const val NO_TIME = -1L
    }

    internal val danmakuContainerList : ThreadLocal<List<DanmakuContainer<*>>> = ThreadLocal()

    val danmakuConfig : HashMap<Any, Any> = HashMap()
}
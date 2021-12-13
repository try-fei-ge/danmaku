package com.blackcat.danmaku

import androidx.annotation.WorkerThread
import com.blackcat.danmaku.module.DanmakuContainer

interface DanmakuContainerInit {
    @WorkerThread
    fun getDanmakuContainer() : List<DanmakuContainer<*>>
}
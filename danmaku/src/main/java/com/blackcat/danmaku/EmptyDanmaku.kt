package com.blackcat.danmaku

import android.graphics.Canvas
import android.graphics.RectF
import com.blackcat.danmaku.module.Danmaku
import com.blackcat.danmaku.module.DanmakuDisplay
import com.blackcat.danmaku.module.Deliver
import com.blackcat.danmaku.module.ShareElement

class EmptyDanmaku : Danmaku<EmptyShareElement, EmptyDeliver>(EMPTY, "", 0F) {
    override fun onMeasure(
        danmakuDisplay: DanmakuDisplay,
        shareElement: EmptyShareElement?
    ): EmptyDeliver? {
        return null
    }

    override fun onLayout(time: Long, bound: RectF, deliver: EmptyDeliver?, danmakuDisplay: DanmakuDisplay): Boolean {
        return false
    }

    override fun onDraw(
        canvas: Canvas,
        deliver: EmptyDeliver?,
        shareElement: EmptyShareElement?,
        bound: RectF
    ) {}

    override fun inScreen(time: Long, danmakuDisplay: DanmakuDisplay): Boolean {
        return false
    }
}

class EmptyShareElement : ShareElement()

class EmptyDeliver : Deliver<EmptyShareElement>()
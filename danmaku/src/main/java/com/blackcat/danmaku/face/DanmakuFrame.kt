package com.blackcat.danmaku.face

import android.graphics.RectF
import com.blackcat.danmaku.module.Danmaku
import com.blackcat.danmaku.module.Deliver

class DanmakuFrame {
    var danmaku: Danmaku<*, *> ?= null
    var bound: RectF = RectF()
    var deliver: Deliver<*> ?= null
}
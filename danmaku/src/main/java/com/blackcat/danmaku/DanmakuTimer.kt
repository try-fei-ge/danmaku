package com.blackcat.danmaku

interface DanmakuTimer {
    fun start()

    fun stop()

    fun getCurrentTime() : Long
}
package com.blackcat.danmaku.module.track

import java.util.*

class Track(val number: Int, val contourLine : Int, val minTimeInterval : Long) {
    internal var idleTime = 0L
    internal val danmakuRecord = DanmakuRecord()
    internal val idleDanmakuList = LinkedList<TrackDanmaku>()

    override fun equals(other: Any?): Boolean {
        if (other !is Track) return false
        return number == other.number
    }

    override fun hashCode(): Int {
        return number
    }
}

class DanmakuRecord {
    var lastStartTime = 0L
    var lastDuration = 0L
    var lastShowDuration = 0L
    var lastVx = TrackDanmaku.MIN_VX

    fun record(danmaku: TrackDanmaku) {
        lastStartTime = danmaku.startTime
        lastDuration = danmaku.getDuration()
        lastShowDuration = danmaku.getShowDuration()
        lastVx = danmaku.getVx()
    }

    fun reset() {
        lastStartTime = 0L
        lastDuration = 0L
        lastShowDuration = 0L
        lastVx = TrackDanmaku.MIN_VX
    }
}
package com.blackcat.danmaku.module.track

import android.util.Log
import android.util.SparseArray
import com.blackcat.danmaku.EmptyDanmaku
import com.blackcat.danmaku.StartTimeCompare
import com.blackcat.danmaku.module.Danmaku
import com.blackcat.danmaku.module.DanmakuContainer
import com.blackcat.danmaku.module.DanmakuDisplay
import java.util.*

class TrackDanmakuContainer : DanmakuContainer<TrackShareElement>() {
    private val trackSet = SparseArray<Track>()
    private val danmakuTree = TreeSet(StartTimeCompare())
    private val spearStart by lazy { EmptyDanmaku() }
    private val spearEnd by lazy { EmptyDanmaku() }

    fun putTrack(number: Int, track: Track) {
        trackSet.put(number, track)
    }

    override fun onDanmakuFreeze(danmaku: Danmaku<*, *>) {}

    override fun onDanmakuReleaseFreeze(danmaku: Danmaku<*, *>) {}

    override fun addDanmaku(danmaku: Danmaku<*, *>) {
        val trackDanmaku = convertDanmaku(danmaku) ?: return
        val track = trackSet[trackDanmaku.number] ?: return
        super.addDanmaku(danmaku)
        trackDanmaku.track = track
        track.idleDanmakuList.add(trackDanmaku)
    }

    override fun createShareElement(): TrackShareElement {
        return TrackShareElement()
    }

    override fun supportDanmaku(danmaku: Danmaku<*, *>): Boolean {
        return danmaku.type == Danmaku.TRACK
    }

    private fun convertDanmaku(danmaku: Danmaku<*, *>) : TrackDanmaku? {
        return try {
            danmaku as TrackDanmaku
        } catch (cast : ClassCastException) {
            null
        }
    }

    override fun fetchDanmakuInit(
        currentTime: Long,
        danmakuDisplay: DanmakuDisplay,
        list: LinkedList<Danmaku<*, *>>
    ) {
        fetchDanmaku(0, currentTime, danmakuDisplay, list)
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val danmaku = iterator.next()
            if (!danmaku.isMeasured(danmakuDisplay)) {
                danmaku.measure(danmakuDisplay)
            }
            if (!danmaku.inScreen(currentTime, danmakuDisplay)) iterator.remove()
        }
    }

    override fun fetchDanmaku(
        startTime: Long,
        endTime: Long,
        danmakuDisplay: DanmakuDisplay,
        list: LinkedList<Danmaku<*, *>>
    ) {
        for (index in 0 until trackSet.size()) {
            val track = trackSet.valueAt(index)
            if (track.idleTime >= startTime) {
                spearStart.startTime = startTime
                spearEnd.startTime = -1 + if (track.idleTime >= endTime) endTime else track.idleTime
                if (spearEnd.startTime > spearStart.startTime) {
                    list.addAll(danmakuTree.subSet(spearStart, spearEnd))
                }
            }
            val record = track.danmakuRecord
            var fillTime = if (track.idleTime > startTime) track.idleTime else startTime
            while (fillTime in startTime until endTime) {
                getDanmaku(track.idleDanmakuList, danmakuDisplay)?.let {
                    it.startTime = track.minTimeInterval + if (record.lastVx >= it.getVx()) {
                        record.lastStartTime + record.lastDuration
                    } else {
                        (record.lastStartTime + record.lastShowDuration - it.getShowDuration() + it.width / it.getVx()).toLong()
                    }
                    if (it.startTime < endTime) it.startTime = endTime
                    record.record(it)
                    danmakuTree.add(it)
                    track.idleTime = record.lastStartTime + record.lastDuration + track.minTimeInterval
                } ?: run {
                    track.idleTime = endTime
                }
                fillTime = track.idleTime
            }
        }
    }

    private fun getDanmaku(list: LinkedList<TrackDanmaku>, danmakuDisplay: DanmakuDisplay) : TrackDanmaku? {
        if (list.isEmpty()) return null
        val iterator = list.iterator()
        var danmaku : TrackDanmaku? = iterator.next()
        while (danmaku != null) {
            if (danmaku.danmakuContainer != this) {
                danmaku = iterator.next()
                continue
            }
            if (!danmaku.isMeasured(danmakuDisplay)) {
                danmaku.measure(danmakuDisplay)
            }
            iterator.remove()
            break
        }
        return danmaku
    }
}
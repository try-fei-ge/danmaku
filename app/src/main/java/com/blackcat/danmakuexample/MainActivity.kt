package com.blackcat.danmakuexample

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.blackcat.danmaku.DanmakuView
import com.blackcat.danmaku.module.DanmakuContainer
import com.blackcat.danmaku.module.track.Track
import com.blackcat.danmaku.module.track.TrackDanmaku
import com.blackcat.danmaku.module.track.TrackDanmakuContainer
import java.util.*

class MainActivity : AppCompatActivity() {
    var currentTrack = 1
    var danmakuCount = 0

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val danmakuView = findViewById<DanmakuView>(R.id.danmaku)
        val maxTrack = 5
        danmakuView.config(
            danmakuContainerInit = {
                val list = LinkedList<DanmakuContainer<*>>()
                val trackDanmakuContainer = TrackDanmakuContainer()
                for (number in 0 until maxTrack) {
                    val track = Track(number, 100 * (number + 1), 100)
                    trackDanmakuContainer.putTrack(track.number, track)
                }
                list.add(trackDanmakuContainer)
                list
            }
        )
        findViewById<View>(R.id.prepare).setOnClickListener {
            danmakuView.prepare()
        }
        findViewById<View>(R.id.start).setOnClickListener {
            danmakuView.start()
        }
        findViewById<View>(R.id.stop).setOnClickListener {
            danmakuView.stop()
        }
        findViewById<View>(R.id.release).setOnClickListener {
            danmakuView.release()
            currentTrack = 1
            danmakuCount = 0
        }
        findViewById<View>(R.id.relayout).setOnClickListener {
            val layoutParam = danmakuView.layoutParams
            if (layoutParam.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                layoutParam.width = 800
            } else {
                layoutParam.width = ViewGroup.LayoutParams.MATCH_PARENT
            }
            danmakuView.requestLayout()
        }
        findViewById<View>(R.id.seek).setOnClickListener {
            danmakuView.seek(-2000)
        }
        findViewById<View>(R.id.publish).setOnClickListener {
            val time = System.nanoTime()
            val all = "[${danmakuCount}]这是一条弹幕-------$time"
            val real = all.subSequence(0, 2 + danmakuCount.toString().length + (Math.random() * 20).toInt())
            danmakuView.addDanmaku(
                TrackDanmaku(real, currentTrack, 58F, textColor = Color.BLUE)
            )
            currentTrack = (currentTrack + 1) % maxTrack
            danmakuCount ++
        }
        findViewById<View>(R.id.check_track).visibility = View.INVISIBLE
    }
}
package com.blackcat.danmakuexample

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.blackcat.danmaku.DanmakuContainerInit
import com.blackcat.danmaku.DanmakuView
import com.blackcat.danmaku.module.DanmakuContainer
import com.blackcat.danmaku.module.track.Track
import com.blackcat.danmaku.module.track.TrackDanmaku
import com.blackcat.danmaku.module.track.TrackDanmakuContainer
import java.util.*

class MainActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val danmakuView = findViewById<DanmakuView>(R.id.danmaku)
        danmakuView.config(
            danmakuContainerInit = {
                val list = LinkedList<DanmakuContainer<*>>()
                val trackDanmakuContainer = TrackDanmakuContainer()
                val track = Track(0, 300, 100)
                trackDanmakuContainer.putTrack(track.number, track)
                list.add(trackDanmakuContainer)
                list
            }
        )
        danmakuView.prepare()
        danmakuView.start()
        danmakuView.postDelayed({
//            danmakuView.addDanmaku(TrackDanmaku("哈哈哈", 0, 63F))
//            danmakuView.addDanmaku(TrackDanmaku("哈哈哈1", 0, 63F))
            danmakuView.addDanmaku(TrackDanmaku("a设计大可不必丢完毕不打卡就贬低无敌不爱的", 0, 63F))
//            danmakuView.addDanmaku(TrackDanmaku("哈哈哈2", 0, 63F))
        }, 1500)
    }
}
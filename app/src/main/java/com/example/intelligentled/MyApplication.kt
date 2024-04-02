package com.example.intelligentled

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/*  注意还需要在AndroidManifest.xml的<application>标签下加入:
    android:name=".MyApplication"
 */
class MyApplication:Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context=applicationContext
    }
}
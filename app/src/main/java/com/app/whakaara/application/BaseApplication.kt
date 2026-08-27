package com.app.whakaara.application



import android.app.Application

import com.whakaara.feature.alarm.map.OsmdroidInitializer

import dagger.hilt.android.HiltAndroidApp



@HiltAndroidApp

class BaseApplication : Application() {

    override fun onCreate() {

        super.onCreate()

        OsmdroidInitializer.init(this)

        BackgroundTasks.schedule(this)

        BackgroundTasks.refreshOnAppStart(this)

    }

}


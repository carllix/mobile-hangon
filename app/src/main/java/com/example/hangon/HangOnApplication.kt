package com.example.hangon

import android.app.Application

class HangOnApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: HangOnApplication
            private set
    }
}

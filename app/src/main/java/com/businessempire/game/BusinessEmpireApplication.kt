package com.businessempire.game

import android.app.Application

class BusinessEmpireApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

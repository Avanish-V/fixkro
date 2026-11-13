package com.iotabuild.zippyhand

import android.app.Application
import com.iotabuild.zippyhand.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZippyhandApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ZippyhandApp)
            modules(listOf(appModule, com.iotabuild.zippyhand.di.networkModule))
        }
    }
}

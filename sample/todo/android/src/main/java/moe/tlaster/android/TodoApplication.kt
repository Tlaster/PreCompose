package moe.tlaster.android

import android.app.Application
import moe.tlaster.common.di.AppModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class TodoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        stopKoin()
        startKoin {
            modules(AppModule.appModule)
        }
    }
}

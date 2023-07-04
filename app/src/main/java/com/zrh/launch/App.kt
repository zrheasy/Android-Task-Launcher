package com.zrh.launch

import android.app.Application
import android.util.Log

/**
 *
 * @author zrh
 * @date 2023/7/4
 *
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val launcher = TaskLauncher()
        launcher.setLogger { tag, msg -> Log.d(tag, msg) }


        launcher.addMainTask("SharedPreferences", emptySet()) {}

        launcher.addAsyncTask("DataBase", setOf("SharedPreferences")) {
            Thread.sleep(1000)
        }

        launcher.addAsyncTask("Network", setOf("SharedPreferences")) {
            Thread.sleep(1000)
        }

        launcher.addMainTask("Other", setOf("DataBase")) {}

        launcher.start()
    }
}
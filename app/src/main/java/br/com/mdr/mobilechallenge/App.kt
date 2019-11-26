package br.com.mdr.mobilechallenge

import android.app.Application
import android.content.Context
import android.view.View
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class App : Application() {

    companion object {
        private lateinit var mActivity: MainActivity

        lateinit var context: Context
        lateinit var view: View
        var activity: MainActivity?
            get() = mActivity
            set(mActivity) {
                App.mActivity = mActivity!!
            }
    }

    override fun onCreate() {
        super.onCreate()

        AppEventsLogger.activateApp(this)
    }
}

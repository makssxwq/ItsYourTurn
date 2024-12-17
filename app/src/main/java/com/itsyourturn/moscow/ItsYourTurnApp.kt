package com.itsyourturn.moscow

import android.app.Application
import com.google.firebase.FirebaseApp

class ItsYourTurnApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
    
    companion object {
        const val MOSCOW_LAT = 55.7558
        const val MOSCOW_LNG = 37.6173
        const val DEFAULT_ZOOM = 12f
    }
}

package com.threestrandscattle.app

import android.app.Application

class ThreeStrandsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize services
        com.threestrandscattle.app.services.NotificationService.getInstance(this)
    }
}

package me.kdv.riverlevel.presentation

import android.app.Application
import me.kdv.riverlevel.di.DaggerApplicationComponent

class RiverApp : Application() {
    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }
}
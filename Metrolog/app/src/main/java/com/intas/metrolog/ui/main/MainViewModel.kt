package com.intas.metrolog.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.intas.metrolog.database.AppDatabase
import io.reactivex.disposables.CompositeDisposable

class MainViewModel(application: Application): AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()

    init {

    }
}
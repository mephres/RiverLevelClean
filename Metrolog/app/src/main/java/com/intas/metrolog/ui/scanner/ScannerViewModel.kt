package com.intas.metrolog.ui.scanner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.equip.EquipItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()
    var onFailure: ((String) -> Unit)? = null
    var onSuccess: ((Int) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    /**
     * Установка метки для оборудования. Ведем поиск оборудования по метке, если список пуст, то считаем, что метка никому не принадлежит.
     * В этом случае, проставляем для оборудования метку. Сохраняем изменения оборудования в локальной базе данных. Выводит тост, что метка установлена.
     * Обновляем все мероприятия, в которых участвует данное оборудование.
     *
     * @param equip оборудование, экземмпляр класса [EquipItem]
     * @param rfid  метка оборудования
     */
    fun setRFIDtoEquip(equip: EquipItem, rfid: String) {
        val disposable = db.equipDao().getEquipByRFID(rfid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (!it.isNullOrEmpty()) {
                    onFailure?.invoke(rfid)
                } else {
                    equip.apply {
                        equipRFID = rfid
                        isSendRFID = 0
                    }
                    onSuccess?.invoke(db.equipDao().updateEquip(equip))
                }
            }, {
                onError?.invoke(it.localizedMessage)
            })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}

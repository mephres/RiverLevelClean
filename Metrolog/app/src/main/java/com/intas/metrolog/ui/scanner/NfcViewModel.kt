package com.intas.metrolog.ui.scanner

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.intas.metrolog.database.AppDatabase
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.util.Journal
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class NfcViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()
    var onFailure: ((String) -> Unit)? = null
    var onSuccess: ((Int) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    var onEquipItemSuccess: ((EquipItem) -> Unit)? = null

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

                    Journal.insertJournal("NfcViewModel->setRFIDtoEquip->failure",
                        "rfid: $rfid")
                } else {
                    equip.apply {
                        equipRFID = rfid
                        isSendRFID = 0
                    }
                    val update = db.equipDao().updateEquip(equip)
                    db.eventDao().updateEventByRfid(equipId = equip.equipId, rfid = rfid)
                    onSuccess?.invoke(update)

                    Journal.insertJournal("NfcViewModel->setRFIDtoEquip->success",
                        "equip: $equip, rfid: $rfid")
                }
            }, {
                onError?.invoke(it.localizedMessage)
                it.printStackTrace()
            })
        compositeDisposable.add(disposable)
    }

    /**
     * Получение одного экземпляра оборудования из базы по метке
     *
     * @param rfid метка оборудования
     */
    fun getEquipByRFID(rfid: String) {
        val disposable = db.equipDao().getEquipItemByRFID(rfid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onEquipItemSuccess?.invoke(it)

                Journal.insertJournal("NfcViewModel->getEquipByRFID->success",
                    "equip: $it, rfid: $rfid")
            }, {
                it.printStackTrace()

                if (it.toString().contains("EmptyResultSetException")) {
                    onFailure?.invoke(rfid)
                } else {
                    onError?.invoke(it.localizedMessage)
                }

                Journal.insertJournal("NfcViewModel->getEquipByRFID->error",
                    "error: $it, rfid: $rfid")
            })
        compositeDisposable.add(disposable)
    }

    fun getEventListByRfid(rfid: String): List<EventItem> {
        return db.eventDao().getEventListByRfid(rfid)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}

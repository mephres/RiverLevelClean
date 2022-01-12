package com.intas.metrolog.pojo.equip

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Оборудование
 */
@Parcelize
@Entity(tableName = "equip", indices = [Index(value = ["equipId"], unique = true)])
data class EquipItem(
    /**
     * идентификатор записи
     */
    @PrimaryKey(autoGenerate = true)
    @SerializedName("equipId")
    @Expose
    var equipId: Long,
    /**
     * Наименование оборудования
     */
    @SerializedName("equipName")
    @Expose
    var equipName: String?,
    /**
     * Место установки оборудования
     */
    @SerializedName("mestUstan")
    @Expose
    var mestUstan: String?,
    /**
     * Заводской номер
     */
    @SerializedName("equipZavNum")
    @Expose
    var equipZavNum: String?,
    /**
     * RFID-метка
     */
    @SerializedName("equipRFID")
    @Expose
    var equipRFID: String?,
    /**
     * Тэг оборудования
     */
    @SerializedName("equipTag")
    @Expose
    var equipTag: String?,
    /**
     * ГРСИ - Государственный реестр средств измерений
     */
    @SerializedName("equipGRSI")
    @Expose
    var equipGRSI: String?,
    /**
     * Наименование завода-изготовителя оборудования
     */
    @SerializedName("equipZavodIzg")
    @Expose
    var equipZavodIzg: String?,
    /**
     * Вид измерений данного одорудования
     */
    @SerializedName("equipVidIzm")
    @Expose
    var equipVidIzm: String?,
    /**
     * Дата последней калибровки оборудования
     */
    @SerializedName("lastCalibr")
    @Expose
    var lastCalibr: String?,
    /**
     * Дата последней поверки оборудования
     */
    @SerializedName("lastVerif")
    @Expose
    var lastVerif: String?,
    /**
     * Актуальность тэга оборудования (0 - неактуальный, 1 - актуальный)
     */
    @SerializedName("equipTagActual")
    @Expose
    var equipTagActual: Int = 0,
    /**
     * Признак отсылки метки на сервер ЦНО
     * 0 - не отослана
     * 1 - отослана
     */
    var isSendRFID: Int = 1,
    /**
     * Геграфическая широта оборудования
     */
    var latitude: String?,
    /**
     * Географическая долгота оборудования
     */
    var longitude: String?,
    /**
     * Признак отсылки геокоординат на сервер ЦНО
     * 0 - не отосланы
     * 1 - отосланы
     */
    var isSendGeo: Int = 1,
    /**
     * Список информации по оборудованию
     */
    @Ignore
    @SerializedName("priorityInfo")
    @Expose
    var equipInfoList: List<EquipInfo>,
) : Parcelable {
    constructor(
        equipId: Long,
        equipName: String?,
        mestUstan: String?,
        equipZavNum: String?,
        equipRFID: String?,
        equipTag: String?,
        equipGRSI: String?,
        equipZavodIzg: String?,
        equipVidIzm: String?,
        lastCalibr: String?,
        lastVerif: String?,
        equipTagActual: Int = 0,
        isSendRFID: Int = 1,
        latitude: String?,
        longitude: String?,
        isSendGeo: Int = 1,
    ) : this(equipId, equipName,mestUstan,equipZavNum,equipRFID,equipTag,equipGRSI,equipZavodIzg,
        equipVidIzm,lastCalibr,lastVerif,equipTagActual,isSendRFID,latitude,longitude,isSendGeo, listOf())
}
package me.kdv.riverlevel.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "river_level")
data class RiverLevelDbModel(
    @PrimaryKey
    val id: Int, //id
    val name: String, //наименование реки
    val location: String, //наименование пункта
    val floodplain: String, //выход на пойму
    val waterLevel: String, //уровень воды
    val levelChange: String, //Изменение уровня за сутки
    val waterTemperature: String?, //Температура воды
    val lastUpdate: Long,
    val dateTime: String
) {
}
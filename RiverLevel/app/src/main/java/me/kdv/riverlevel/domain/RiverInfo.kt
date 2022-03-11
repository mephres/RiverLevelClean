package me.kdv.riverlevel.domain

data class RiverInfo(
    val id: Int,
    val name: String, //наименование реки
    val location: String, //наименование пункта
    val floodplain: String, //выход на пойму
    val waterLevel: String, //уровень воды
    val levelChange: String, //Изменение уровня за сутки
    val waterTemperature: String?, //Температура воды
    val lastUpdate: Long,
    val dateTime: String
)

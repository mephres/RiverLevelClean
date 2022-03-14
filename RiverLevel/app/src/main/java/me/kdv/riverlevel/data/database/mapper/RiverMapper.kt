package me.kdv.riverlevel.data.database.mapper

import android.os.SystemClock
import android.util.Log
import me.kdv.riverlevel.data.database.RiverLevelDbModel
import me.kdv.riverlevel.data.network.model.RiverLevelDto
import me.kdv.riverlevel.data.network.model.RiverLevelHtmlDto
import me.kdv.riverlevel.domain.RiverInfo
import me.kdv.riverlevel.utils.DateTimeUtil
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.util.ArrayList
import javax.inject.Inject
import org.jsoup.Jsoup

class RiverMapper @Inject constructor() {
    fun mapDtoToDbModel(riverLevelDto: RiverLevelDto): RiverLevelDbModel {
        return RiverLevelDbModel(
            id = riverLevelDto.id,
            name = riverLevelDto.name,
            location = riverLevelDto.location,
            floodplain = riverLevelDto.floodplain,
            waterLevel = riverLevelDto.waterLevel,
            levelChange = riverLevelDto.levelChange,
            waterTemperature = riverLevelDto.waterTemperature,
            lastUpdate = riverLevelDto.lastUpdate,
            dateTime = riverLevelDto.dateTime
        )
    }

    fun mapHtmlContainerToListRiverLevel(html: String): List<RiverLevelDto> {

        val document = Document("")
        document.append(html)

        val riverArrayList = mutableListOf<RiverLevelDto>()
        val table: Elements = document.select("table")
        for (tableElement in table) {
            Log.i(
                "[table]",
                String.format(" * %s: <%s>", tableElement.tagName(), tableElement.attr("abs:src"))
            )
            val trElements: Elements = table.select("tr")
            var count = 1
            for (tr in trElements) {
                Log.i("[table tr]", String.format(" * %s: <%s>", tr.tagName(), tr.attr("abs:src")))
                val tdElements: Elements = tr.select("td")
                if (!tdElements.isEmpty()) {
                    val name = tdElements[0].toString().html2text()
                    val location = tdElements[1].toString().html2text()
                    val floodplain = tdElements[2].toString().html2text()
                    val waterLevel = tdElements[3].toString().html2text()
                    val levelChange = tdElements[4].toString().html2text()
                    val waterTemperature = tdElements[5].toString().html2text()
                    val dateTimeNow = DateTimeUtil.getUnixDateTimeNow()
                    val river = RiverLevelDto(
                        id = count,
                        name = name,
                        location = location,
                        floodplain = floodplain,
                        waterLevel = waterLevel,
                        levelChange = levelChange,
                        waterTemperature = waterTemperature,
                        lastUpdate = dateTimeNow,
                        dateTime = DateTimeUtil.getLongDateFromMili(dateTimeNow)
                    )
                    count++
                    riverArrayList.add(river)
                }
            }
        }
        return riverArrayList
    }

    fun mapDbModelToEntity(dbModel: RiverLevelDbModel) = RiverInfo(
        id = dbModel.id,
        name = dbModel.name,
        location = dbModel.location,
        floodplain = dbModel.floodplain,
        waterLevel = dbModel.waterLevel,
        levelChange = dbModel.levelChange,
        waterTemperature = dbModel.waterTemperature,
        lastUpdate = dbModel.lastUpdate,
        dateTime = dbModel.dateTime
    )

    private fun String.html2text() = Jsoup.parse(this).text()
}
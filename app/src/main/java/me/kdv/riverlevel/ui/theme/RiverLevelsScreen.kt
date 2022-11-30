package me.kdv.riverlevel.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.kdv.riverlevel.R
import me.kdv.riverlevel.domain.RiverInfo
import me.kdv.riverlevel.presentation.RiverViewModel
import me.kdv.riverlevel.presentation.ViewModelFactory
import me.kdv.riverlevel.utils.DateTimeUtil
import kotlin.math.abs


@Composable
fun RiversScreen(
    viewModelFactory: ViewModelFactory
) {

    val viewModel: RiverViewModel = viewModel(
        factory = viewModelFactory
    )

    val riverLevels = viewModel.riverInfoList.observeAsState(listOf())

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                start = 8.dp,
                end = 8.dp,
                bottom = 72.dp
            )
        ) {
            items(
                items = riverLevels.value,
                key = { it.id }
            ) { riverInfo ->
                RiverLevelItem(riverInfo = riverInfo)
            }
        }
    }
}

@Composable
private fun RiverLevelItem(riverInfo: RiverInfo) {

    val waterTemperaturePattern = stringResource(id = R.string.river_info_water_temperature_pattern)

    val waterTemperature = try {
        String.format(waterTemperaturePattern, (riverInfo.waterTemperature ?: "").toInt())
    } catch (nfe: Exception) {
        "Н/Д"
    }

    var arrow = Icons.Filled.KeyboardArrowDown
    var arrowTint = Color.Green
    if (riverInfo.levelChange.toInt() > 0) {
        arrow = Icons.Filled.KeyboardArrowUp
        arrowTint = Color.Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        ) {
            Text(
                text = riverInfo.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = riverInfo.location,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("Выход на пойму, см: ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(riverInfo.floodplain)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Уровень воды, см: ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(riverInfo.waterLevel)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = waterTemperature,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Изменение уровня за сутки, см:",
                )
                Icon(
                    imageVector = arrow,
                    contentDescription = null,
                    tint = arrowTint
                )
                Text(
                    fontWeight = FontWeight.Bold,
                    text = abs(riverInfo.levelChange.toInt()).toString(),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("Данные обновлены: ")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(DateTimeUtil.getLongDateFromMilli(riverInfo.lastUpdate))
                    }
                }
            )
        }
    }
}
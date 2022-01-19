package com.intas.metrolog.ui.requests.filter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RequestFilter(
    var requestDisciplineIdList: ArrayList<Int>,
    var requestStatusIdList: ArrayList<Int>,
    var dateStart: Long,
    var dateEnd: Long
) : Parcelable
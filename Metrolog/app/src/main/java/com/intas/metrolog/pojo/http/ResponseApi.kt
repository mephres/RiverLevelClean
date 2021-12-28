package com.intas.metrolog.pojo.http

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseApi<T>(

    @SerializedName("data")
    @Expose
    val list: List<T>?,

    @SerializedName("error")
    @Expose
    var requestError: RequestError?,
    )



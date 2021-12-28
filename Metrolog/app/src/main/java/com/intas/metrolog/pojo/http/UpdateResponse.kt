package com.intas.metrolog.pojo.http

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateResponse(
    @SerializedName("success")
    @Expose
    var requestSuccess: RequestSuccess? = null,

    @SerializedName("error")
    @Expose
    val requestError: RequestError? = null,

    ) {
}
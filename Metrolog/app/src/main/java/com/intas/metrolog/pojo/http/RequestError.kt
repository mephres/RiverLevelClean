package com.intas.metrolog.pojo.http

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RequestError(
    @SerializedName("code")
    @Expose
    var code: String? = null,
    @SerializedName("description")
    @Expose
    val description: String? = null,
    @SerializedName("message")
    @Expose
    val message: String? = null
) {
}
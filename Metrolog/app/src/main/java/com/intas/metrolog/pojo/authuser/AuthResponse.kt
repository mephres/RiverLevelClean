package com.intas.metrolog.pojo.authuser

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.intas.metrolog.pojo.http.RequestError

data class AuthResponse(
    @SerializedName("data")
    @Expose
    val data: AuthUser? = null,

    @SerializedName("error")
    @Expose
    val requestError: RequestError? = null
)
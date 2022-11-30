package me.kdv.riverlevel.data.network

import okhttp3.ResponseBody
import retrofit2.http.GET

interface ApiService {
    @GET("urovni-rek")
    suspend fun getRiverLevel(
    ): ResponseBody
}
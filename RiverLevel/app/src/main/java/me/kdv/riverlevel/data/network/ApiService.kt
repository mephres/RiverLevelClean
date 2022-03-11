package me.kdv.riverlevel.data.network

import me.kdv.riverlevel.data.network.model.RiverLevelHtmlDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("urovni-rek")
    suspend fun getRiverLevel(
    ): ResponseBody

    companion object {

    }
}
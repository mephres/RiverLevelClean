package me.kdv.riverlevel.data.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiFactory {

    private const val BASE_URL = "http://www.meteorb.ru/"

    val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    val apiService = retrofit.create(ApiService::class.java)
}
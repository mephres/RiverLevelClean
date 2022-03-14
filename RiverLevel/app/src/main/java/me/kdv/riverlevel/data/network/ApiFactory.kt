package me.kdv.riverlevel.data.network

import me.kdv.riverlevel.data.network.converter.ToStringConverterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiFactory {

    private const val BASE_URL = "http://www.meteorb.ru/"

    val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        //.addConverterFactory(GsonConverterFactory.create(gson))
        //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        //.addConverterFactory(ToStringConverterFactory())
        .baseUrl(BASE_URL)
        .build()

    val apiService = retrofit.create(ApiService::class.java)
}
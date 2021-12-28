package com.intas.metrolog.api

import com.google.gson.GsonBuilder
import com.intas.metrolog.util.Journal
import com.intas.metrolog.util.Util
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiFactory {

    //private const val BASE_URL = "http://92.50.140.66:8081/apiMobOp/"
    private const val TEST_BASE_URL = "http://192.168.0.23:8080/apiMobOp/"
    private var sClient: OkHttpClient? = null
    private val LOCK = Any()

    val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .baseUrl(Util.serverIpAddress)
        //.baseUrl(TEST_BASE_URL)
        .client(getClient())
        .build()

    val apiService = retrofit.create(ApiService::class.java)

    private fun getClient(): OkHttpClient {

        synchronized(LOCK) {
            sClient?.let { return it }

            val client = buildClient()
            sClient = client
            return client
        }
    }

    private fun buildClient(): OkHttpClient {

        var okHttpClient = OkHttpClient()

        val okHttpClientBuilder = OkHttpClient.Builder()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        with(okHttpClientBuilder) {

            addInterceptor(loggingInterceptor)

            addInterceptor { chain ->
                val request = chain.request().newBuilder().addHeader(
                    "Content-Type",
                    "application/x-www-form-urlencoded; charset=utf-8"
                ).build()
                chain.proceed(request)
            }
            addNetworkInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)

                Journal.insertJournal("ApiFactory->buildClient->addNetworkInterceptor->request", request.toString(), 2)

                response
            }
            connectionPool(ConnectionPool(50, 5, TimeUnit.SECONDS))

            okHttpClient = this.connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).build()
        }

        return okHttpClient
    }
}
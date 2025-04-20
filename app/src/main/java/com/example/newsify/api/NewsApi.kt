package com.example.newsify.api

import com.example.newsify.models.NewsResponse
import com.example.newsify.util.Constant.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale.IsoCountryCode

interface NewsApi {
    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("country")
        countryCode: String="us",
        @Query("page")
        pageNumber: Int=1,
        @Query("apikey")
        apikey: String=API_KEY
    ):Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery: String,
        @Query("page")
        pageNumber: Int=1,
        @Query("apikey")
        apikey: String= API_KEY

    ): Response<NewsResponse>
}
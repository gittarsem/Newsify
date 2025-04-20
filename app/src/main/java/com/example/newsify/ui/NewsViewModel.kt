package com.example.newsify.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsify.models.Article
import com.example.newsify.models.NewsResponse
import com.example.newsify.repository.NewsRepository
import com.example.newsify.util.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class NewsViewModel(app: Application, private val newsRepository: NewsRepository):AndroidViewModel(app) {
    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinesPage = 1
    var headlinesResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null

    init {
        getHeadlines("us")
    }

    fun getHeadlines(countryCode: String)=viewModelScope.launch {
        headlinesInternet(countryCode)
    }

    fun searchNews(searchQuery: String)=viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }

    private fun handleHeadlines(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                headlinesPage++
                if (headlinesResponse == null) {
                    headlinesResponse = resultResponse
                } else {
                    val oldArticle = headlinesResponse?.articles?.toMutableList() ?: mutableListOf()
                    val newArticle = resultResponse.articles
                    oldArticle.addAll(newArticle)

                    headlinesResponse = headlinesResponse?.copy(articles = oldArticle)

                }
                return Resource.Success(headlinesResponse ?: resultResponse)
            }

        }
        return Resource.Error(response.message(), null)
    }

    private fun handleSearchQuery(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery) {
                    searchNewsPage++
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = resultResponse

                } else {
                    searchNewsPage++
                    val oldArticle =
                        searchNewsResponse?.articles?.toMutableList() ?: mutableListOf()
                    val newArticle = resultResponse.articles
                    oldArticle.addAll(newArticle)

                    searchNewsResponse = searchNewsResponse?.copy(articles = oldArticle)

                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }


        }

        return Resource.Error(response.message(), null)

    }

    fun addToFavourite(article:Article)=viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getFavouriteNews()=newsRepository.getFavouriteNews()

    fun deleteArticle(article:Article)=viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    fun internetConnection(context:Context): Boolean? {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when{
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->true
                    else->false
                }
            }?:false
        }
    }

    private suspend fun headlinesInternet(countryCode:String){
        headlines.postValue(Resource.Loading())
        try {
            if(internetConnection(this.getApplication()) == true){
            val response=newsRepository.getHeadlines(countryCode,headlinesPage)
                headlines.postValue(handleHeadlines(response))
            }
            else{
                headlines.postValue(Resource.Error("No internet Connection",null))
            }
        }catch (t:Throwable){
            when(t){
                is IOException-> headlines.postValue(Resource.Error("Unable to Connect",null))
                else->headlines.postValue(Resource.Error("No Signal",null))
            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery:String){
        newSearchQuery=searchQuery
        searchNews.postValue(Resource.Loading())
        try {
            if(internetConnection(this.getApplication()) == true){
                val response=newsRepository.searchNews(searchQuery,searchNewsPage)
                searchNews.postValue(handleSearchQuery(response))
            }
            else{
                searchNews.postValue(Resource.Error("No internet Connection",null))
            }
        }catch (t:Throwable){
            when(t){
                is IOException-> searchNews.postValue(Resource.Error("Unable to Connect",null))
                else->searchNews.postValue(Resource.Error("No Signal",null))
            }
        }
    }

}
package com.example.newsify.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsify.models.Article

@Dao

interface ArticleDao {
    @Insert(onConflict =OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article)

    @Query("Select * from articles")
    fun getAllArticles(): LiveData<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)
}
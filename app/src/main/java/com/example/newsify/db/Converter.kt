package com.example.newsify.db

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.newsify.models.Source


class Converter {
    @TypeConverter
    fun fromSource(source: Source?):String?{
        return source?.name
    }

    @TypeConverter
    fun toSource(name: String?):Source{
        return Source(null,name)
    }
}
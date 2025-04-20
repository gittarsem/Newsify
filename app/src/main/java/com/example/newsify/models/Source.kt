package com.example.newsify.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Source(
    val id: String?,
    val name: String?
):Serializable, Parcelable
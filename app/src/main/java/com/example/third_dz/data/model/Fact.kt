package com.example.third_dz.data.model

import com.google.gson.annotations.SerializedName

data class Fact(
    @SerializedName("_id")
    val id: String,
    val text: String,
    @SerializedName("__v")
    val version: Int = 0,
    val updatedAt: String? = null,
    val deleted: Boolean = false,
    val source: String? = null,
    val sentCount: Int? = null
)


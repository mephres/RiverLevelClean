package me.kdv.riverlevel.data.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RiverLevelHtmlDto(
    @Expose
    val riverLevelHtml: String
)
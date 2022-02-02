package com.intas.metrolog.pojo.chat

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity(tableName = "chat_message")
@Parcelize
data class MessageItem(
    @SerializedName("id")
    @Expose
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @SerializedName("userId")
    @Expose
    val senderUserId: Int? = null,

    @SerializedName("toUserId")
    @Expose
    val companionUserId: Int? = null,

    @SerializedName("message")
    @Expose
    val message: String? = null,

    @SerializedName("dateTime")
    @Expose
    val dateTime: Long? = null,

    var isSent: Int? = 1,

    var isViewed: Int? = 0
) : Parcelable
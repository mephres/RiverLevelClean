package com.intas.metrolog.pojo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Список пользователей, исполнителей, участников переписки
 */
@Entity(tableName = "user")
data class UserItem(
    /**
     * Идентификатор записи
     */
    @PrimaryKey
    @SerializedName("id")
    @Expose
    val id: Int,
    /**
     * ФИО пользователя
     */
    @SerializedName("name")
    @Expose
    val fullName: String?,
    /**
     * Должность пользователя
     */
    @SerializedName("position")
    @Expose
    val position: String? = null,
    /**
     * Ссылка на изображение/аватар пользователя
     */
    @SerializedName("imageUrl")
    @Expose
    val avatar: String? = null,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(fullName)
        parcel.writeString(position)
        parcel.writeString(avatar)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserItem> {
        override fun createFromParcel(parcel: Parcel): UserItem {
            return UserItem(parcel)
        }

        override fun newArray(size: Int): Array<UserItem?> {
            return arrayOfNulls(size)
        }
    }
}

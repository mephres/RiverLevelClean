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
    @SerializedName("userId")
    @Expose
    val id: Int = 0,
    /**
     * ФИО пользователя
     */
    @SerializedName("fio")
    @Expose
    val fio: String? = null,
    /**
     * Имя пользователя
     */
    @SerializedName("name")
    @Expose
    val name: String? = null,
    /**
     * Фамилия пользователя
     */
    @SerializedName("surname")
    @Expose
    val surname: String? = null,
    /**
     * Отчество пользователя
     */
    @SerializedName("middleName")
    @Expose
    val middleName: String? = null,
    /**
     * Номер телефона
     */
    @SerializedName("telephone")
    @Expose
    val telephone: String? = null,
    /**
     * Электронный почтовый адрес
     */
    @SerializedName("email")
    @Expose
    val email: String? = null,
    /**
     * идентификатор должности
     */
    @SerializedName("positionId")
    @Expose
    val positionId: String? = null,
    /**
     * Должность пользователя
     */
    @SerializedName("position")
    @Expose
    val position: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(fio)
        parcel.writeString(name)
        parcel.writeString(surname)
        parcel.writeString(middleName)
        parcel.writeString(telephone)
        parcel.writeString(email)
        parcel.writeString(positionId)
        parcel.writeString(position)
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


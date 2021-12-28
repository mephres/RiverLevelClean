package com.intas.metrolog.pojo.authuser

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Авторизованный пользователь
 */
@Entity(tableName = "auth_user")
data class AuthUser(
    /**
     * Идентификатор записи
     */
    @PrimaryKey
    @SerializedName("userId")
    @Expose
    val userId: Int = 0,
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

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(userId)
        parcel.writeString(fio)
        parcel.writeString(name)
        parcel.writeString(surname)
        parcel.writeString(middleName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AuthUser> {
        override fun createFromParcel(parcel: Parcel): AuthUser {
            return AuthUser(parcel)
        }

        override fun newArray(size: Int): Array<AuthUser?> {
            return arrayOfNulls(size)
        }
    }
}
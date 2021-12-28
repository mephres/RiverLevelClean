package com.intas.metrolog.pojo.authuser

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Авторизованный поьзователь
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
    /**
     * Должность пользователя
     */
    @SerializedName("position")
    @Expose
    val position: String? = null,
    /**
     * ссылка на аватар пользователя
     */
    @SerializedName("avatarUrl")
    @Expose
    val avatarUrl: String? = null,
    /**
     * Роль пользователя в системе
     * 0 - оператор
     * 1 - инженер
     */
    @SerializedName("role")
    @Expose
    var role: Int? = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(userId)
        parcel.writeString(fio)
        parcel.writeString(name)
        parcel.writeString(surname)
        parcel.writeString(middleName)
        parcel.writeString(position)
        parcel.writeString(avatarUrl)
        parcel.writeValue(role)
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

        /**
         * Оператор
         */
        val USER_ROLE_OPERATOR = 0

        /**
         * Инженер
         */
        val USER_ROLE_INGENEER = 1
    }
}
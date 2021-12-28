package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intas.metrolog.pojo.authuser.AuthUser

@Dao
interface AuthUserDao {

    @Query("SELECT * FROM auth_user")
    fun getLoggedUser(): LiveData<AuthUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthUser(authUser: AuthUser)
}
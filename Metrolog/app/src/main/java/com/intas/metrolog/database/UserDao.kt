package com.intas.moboperator.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.intas.metrolog.pojo.UserItem
import io.reactivex.Single

@Dao
interface UserDao {
    @Query("SELECT * FROM user ORDER BY surname ASC")
    fun getUserList(): LiveData<List<UserItem>>

    @Query("SELECT * FROM user WHERE id = :id")
    fun getUserById(id: Int): UserItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserList(userList: List<UserItem>)

    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Single<Int>
}
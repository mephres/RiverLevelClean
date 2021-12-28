package com.intas.metrolog.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.intas.metrolog.pojo.JournalItem
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.authuser.AuthUser
import com.intas.metrolog.pojo.userlocation.UserLocation
import com.intas.moboperator.database.UserDao


@Database(
    entities = [AuthUser::class, UserLocation::class, JournalItem::class, UserItem::class],
    version = 1, exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    companion object {
        private var db: AppDatabase? = null
        const val DB_NAME = "database.db"
        private val LOCK = Any()

        fun getInstance(context: Context): AppDatabase {
            synchronized(LOCK) {
                db?.let { return it }

                val instance =
                    Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        DB_NAME
                    ).allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .setJournalMode(JournalMode.TRUNCATE)
                        .build()
                db = instance
                return instance
            }
        }
    }

    abstract fun authUserDao(): AuthUserDao
    abstract fun userLocationDao(): UserLocationDao
    abstract fun journalDao(): JournalDao
    abstract fun userDao(): UserDao
}
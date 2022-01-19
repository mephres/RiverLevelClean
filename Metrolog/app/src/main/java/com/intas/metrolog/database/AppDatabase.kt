package com.intas.metrolog.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.intas.metrolog.pojo.JournalItem
import com.intas.metrolog.pojo.UserItem
import com.intas.metrolog.pojo.authuser.AuthUser
import com.intas.metrolog.pojo.discipline.DisciplineItem
import com.intas.metrolog.pojo.document_type.DocumentType
import com.intas.metrolog.pojo.equip.EquipDocument
import com.intas.metrolog.pojo.equip.EquipInfo
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.pojo.equip_info_priority.EquipInfoPriority
import com.intas.metrolog.pojo.event_comment.EventComment
import com.intas.metrolog.pojo.event_priority.EventPriority
import com.intas.metrolog.pojo.event_status.EventStatus
import com.intas.metrolog.pojo.operation.EventOperationItem
import com.intas.metrolog.pojo.requestStatus.RequestStatusItem
import com.intas.metrolog.pojo.userlocation.UserLocation


@Database(
    entities = [AuthUser::class, UserLocation::class, JournalItem::class, UserItem::class,
               EquipItem::class, EquipInfo::class, RequestStatusItem::class, DisciplineItem::class,
               EventOperationItem::class, DocumentType::class, EquipInfoPriority::class,
               EventStatus::class, EventPriority::class, EventComment::class, EquipDocument::class],
    version = 3, exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {

    companion object {
        private var db: AppDatabase? = null
        private const val DB_NAME = "database.db"
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
    abstract fun equipDao(): EquipDao
    abstract fun requestStatusDao(): RequestStatusDao
    abstract fun disciplineDao(): DisciplineDao
    abstract fun eventOperationDao(): EventOperationDao
    abstract fun documentTypeDao(): DocumentTypeDao
    abstract fun equipInfoPriorityDao(): EquipInfoPriorityDao
    abstract fun eventStatusDao(): EventStatusDao
    abstract fun eventPriorityDao(): EventPriorityDao
    abstract fun eventCommentDao(): EventCommentDao
    abstract fun equipDocumentDao(): EquipDocumentDao
}
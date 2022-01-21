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
import com.intas.metrolog.pojo.event.EventItem
import com.intas.metrolog.pojo.event.event_operation.EventOperationItem
import com.intas.metrolog.pojo.event.event_operation.operation_control.OperControlItem
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.FieldItem
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.dict_data.FieldDictData
import com.intas.metrolog.pojo.event.event_operation_type.EventOperationTypeItem
import com.intas.metrolog.pojo.event_comment.EventComment
import com.intas.metrolog.pojo.request.RequestItem
import com.intas.metrolog.pojo.requestStatus.RequestStatusItem
import com.intas.metrolog.pojo.userlocation.UserLocation


@Database(
    entities = [AuthUser::class, UserLocation::class, JournalItem::class, UserItem::class,
               EquipItem::class, EquipInfo::class, RequestStatusItem::class, DisciplineItem::class,
               EventOperationTypeItem::class, DocumentType::class, EquipInfoPriority::class, EventItem::class,
               EventComment::class, EquipDocument::class, RequestItem::class, EventOperationItem::class,
               OperControlItem::class, FieldItem::class, FieldDictData::class],
    version = 17, exportSchema = false
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
    abstract fun eventOperationTypeDao(): EventOperationTypeDao
    abstract fun documentTypeDao(): DocumentTypeDao
    abstract fun equipInfoPriorityDao(): EquipInfoPriorityDao
    abstract fun eventCommentDao(): EventCommentDao
    abstract fun equipDocumentDao(): EquipDocumentDao
    abstract fun eventDao(): EventDao
    abstract fun requestDao(): RequestDao
    abstract fun eventOperationDao(): EventOperationDao
    abstract fun operControlDao(): OperControlDao
    abstract fun fieldDao(): FieldDao
    abstract fun fieldDictDataDao(): FieldDictDataDao
}
package com.intas.metrolog.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.intas.metrolog.pojo.document_type.DocumentType

@Dao
interface DocumentTypeDao {
    @Query("SELECT * FROM documentType order by name asc")
    fun getAllDocumentType(): LiveData<List<DocumentType>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDocumentType(documentType: DocumentType)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentType(documentType: DocumentType)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentTypeList(documentTypeList: List<DocumentType>)

    @Query("DELETE FROM documentType")
    suspend fun deleteAllDocumentType()
}
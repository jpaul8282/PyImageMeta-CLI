package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageMetadataDao {

    @Query("SELECT * FROM image_metadata ORDER BY timestamp DESC")
    fun getAllImages(): Flow<List<ImageMetadataEntity>>

    @Query("SELECT * FROM image_metadata ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getPagedImages(limit: Int, offset: Int): Flow<List<ImageMetadataEntity>>

    @Query("SELECT COUNT(*) FROM image_metadata")
    fun getTotalCount(): Flow<Int>

    @Query("""
        SELECT * FROM image_metadata 
        WHERE fileName LIKE '%' || :query || '%' 
           OR title LIKE '%' || :query || '%' 
           OR make LIKE '%' || :query || '%' 
           OR model LIKE '%' || :query || '%' 
           OR aiTags LIKE '%' || :query || '%' 
           OR aiCaption LIKE '%' || :query || '%'
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun searchPaged(query: String, limit: Int, offset: Int): Flow<List<ImageMetadataEntity>>

    @Query("""
        SELECT COUNT(*) FROM image_metadata 
        WHERE fileName LIKE '%' || :query || '%' 
           OR title LIKE '%' || :query || '%' 
           OR make LIKE '%' || :query || '%' 
           OR model LIKE '%' || :query || '%' 
           OR aiTags LIKE '%' || :query || '%' 
           OR aiCaption LIKE '%' || :query || '%'
    """)
    fun getSearchCount(query: String): Flow<Int>

    @Query("SELECT * FROM image_metadata WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ImageMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ImageMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ImageMetadataEntity>)

    @Update
    suspend fun update(entity: ImageMetadataEntity)

    @Query("DELETE FROM image_metadata WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM image_metadata")
    suspend fun clearAll()

    @Query("""
        UPDATE image_metadata 
        SET hasGps = 0, latitude = NULL, longitude = NULL, altitude = NULL, locationName = NULL 
        WHERE id = :id
    """)
    suspend fun stripGpsById(id: String)

    @Query("""
        UPDATE image_metadata 
        SET hasGps = 0, latitude = NULL, longitude = NULL, altitude = NULL, locationName = NULL
    """)
    suspend fun stripGpsAll()
}

package com.example.db

import kotlinx.coroutines.flow.Flow

class ImageMetadataRepository(private val dao: ImageMetadataDao) {

    val allImages: Flow<List<ImageMetadataEntity>> = dao.getAllImages()
    val totalCount: Flow<Int> = dao.getTotalCount()

    fun getPaged(page: Int, pageSize: Int): Flow<List<ImageMetadataEntity>> {
        val offset = (page.coerceAtLeast(1) - 1) * pageSize
        return dao.getPagedImages(limit = pageSize, offset = offset)
    }

    fun searchPaged(query: String, page: Int, pageSize: Int): Flow<List<ImageMetadataEntity>> {
        val offset = (page.coerceAtLeast(1) - 1) * pageSize
        return dao.searchPaged(query = query, limit = pageSize, offset = offset)
    }

    fun getSearchCount(query: String): Flow<Int> {
        return dao.getSearchCount(query)
    }

    suspend fun getById(id: String): ImageMetadataEntity? = dao.getById(id)

    suspend fun insert(entity: ImageMetadataEntity) = dao.insert(entity)

    suspend fun insertBatch(entities: List<ImageMetadataEntity>) = dao.insertAll(entities)

    suspend fun update(entity: ImageMetadataEntity) = dao.update(entity)

    suspend fun deleteById(id: String) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()

    suspend fun stripGpsById(id: String) = dao.stripGpsById(id)

    suspend fun stripGpsAll() = dao.stripGpsAll()
}

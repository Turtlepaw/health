package com.turtlepaw.shared.database.services

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.turtlepaw.shared.database.RoomSyncableDao

@Dao
abstract class ServiceDao : RoomSyncableDao<Service, ServiceType>() {
    @Query("SELECT * FROM services WHERE name = :serviceName")
    abstract suspend fun getService(serviceName: ServiceType): Service?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertService(service: Service)

    @Update
    abstract suspend fun updateService(service: Service)

    @Query("UPDATE services SET isEnabled = :isEnabled, synced = :synced WHERE name = :serviceName")
    abstract suspend fun updateService(
        serviceName: ServiceType,
        isEnabled: Boolean,
        synced: Boolean
    )

    @Query("UPDATE services SET isEnabled = :isEnabled WHERE name = :serviceName")
    abstract suspend fun updateServiceStatus(serviceName: ServiceType, isEnabled: Boolean)

    @Query("SELECT * FROM services WHERE isEnabled = 1")
    abstract suspend fun getEnabledServices(): List<Service>

    @Query("SELECT * FROM services")
    abstract suspend fun getAllServices(): List<Service>

    @Delete
    abstract suspend fun deleteService(service: Service)

    @Query("SELECT * FROM services WHERE synced = 0")
    @JvmSuppressWildcards
    abstract override suspend fun getUnsyncedInternal(): List<Service>

    @Query("UPDATE services SET synced = 1 WHERE name IN (:ids)")
    @JvmSuppressWildcards
    abstract override suspend fun markSyncedInternal(ids: List<ServiceType>)
}
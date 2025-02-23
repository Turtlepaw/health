package com.turtlepaw.shared.database.services

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.turtlepaw.shared.database.SyncableEntity

enum class ServiceType(val serviceName: String) {
    Sleep("sleep"),
    Sunlight("sunlight"),
}

@Entity(tableName = "services")
data class Service(
    @PrimaryKey val name: ServiceType,
    val isEnabled: Boolean,
    override val synced: Boolean = false
) : SyncableEntity
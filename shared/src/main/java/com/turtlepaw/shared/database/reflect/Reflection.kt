package com.turtlepaw.shared.database.reflect

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.turtlepaw.shared.database.SyncableEntity
import java.time.LocalDateTime

@Entity(tableName = "reflection")
data class Reflection(
    @PrimaryKey val date: LocalDateTime,
    val value: ReflectionType,
    override val synced: Boolean = false
) : SyncableEntity

enum class ReflectionType(val displayName: String) {
    Excited("Excited"),
    Content("Content"),
    Calm("Calm"),
    Stressed("Stressed"),
    Frustrated("Frustrated"),
    Worried("Worried"),
    Sad("Sad"),
    Happy("Happy"),
}
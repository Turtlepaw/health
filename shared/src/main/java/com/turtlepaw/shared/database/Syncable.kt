package com.turtlepaw.shared.database

interface SyncableEntity {
    val synced: Boolean
}

// Base interface with generics and JvmSuppressWildcards
interface SyncableDao<T : SyncableEntity, ID> {
    @JvmSuppressWildcards
    suspend fun getUnsynced(): List<T>

    @JvmSuppressWildcards
    suspend fun markSynced(ids: List<ID>)
}

// Intermediate abstract class with JvmSuppressWildcards
abstract class RoomSyncableDao<T : SyncableEntity, ID> : SyncableDao<T, ID> {
    @JvmSuppressWildcards
    protected abstract suspend fun getUnsyncedInternal(): List<T>

    @JvmSuppressWildcards
    protected abstract suspend fun markSyncedInternal(ids: List<ID>)

    @JvmSuppressWildcards
    final override suspend fun getUnsynced(): List<T> = getUnsyncedInternal()

    @JvmSuppressWildcards
    final override suspend fun markSynced(ids: List<ID>) = markSyncedInternal(ids)
}
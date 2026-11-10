package com.example.backup

data class BackupMetadata(
    val lastBackupTimestamp: Long = 0L,
    val totalTabsBackedUp: Int = 0,
    val totalBookmarksBackedUp: Int = 0,
    val isAutoBackupEnabled: Boolean = false
)

interface BackupService {
    fun isEnabled(): Boolean
    suspend fun performBackup(): Result<Unit>
    suspend fun restoreBackup(password: String): Result<Unit>
    suspend fun deleteBackup(): Result<Unit>
    suspend fun getMetadata(): BackupMetadata
}

class NoOpBackupService : BackupService {
    override fun isEnabled(): Boolean = false
    override suspend fun performBackup(): Result<Unit> = Result.success(Unit)
    override suspend fun restoreBackup(password: String): Result<Unit> = Result.success(Unit)
    override suspend fun deleteBackup(): Result<Unit> = Result.success(Unit)
    override suspend fun getMetadata(): BackupMetadata = BackupMetadata(
        lastBackupTimestamp = 0L,
        totalTabsBackedUp = 0,
        totalBookmarksBackedUp = 0,
        isAutoBackupEnabled = false
    )
}

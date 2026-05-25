package com.example.third_dz.data.backup

import android.content.Context
import com.example.third_dz.data.local.CollectionDao
import com.example.third_dz.data.local.CollectionFilmCrossRef
import com.example.third_dz.data.local.PinnedEntity
import com.example.third_dz.data.local.PinnedEntityDao
import com.example.third_dz.data.local.PinnedType
import com.example.third_dz.data.local.RecentViewDao
import com.example.third_dz.data.local.RecentViewEntity
import com.example.third_dz.data.local.UserFilmRecordDao
import com.example.third_dz.data.local.UserFilmRecordEntity
import com.example.third_dz.data.local.WatchStatus
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val recordDao: UserFilmRecordDao,
    private val collectionDao: CollectionDao,
    private val pinnedDao: PinnedEntityDao,
    private val recentViewDao: RecentViewDao
) {

    private val backupDir: File get() {
        val dir = File(context.filesDir, "backups")
        dir.mkdirs()
        return dir
    }

    suspend fun export(): File {
        rotateBackups()

        val records = recordDao.observeAll().first()
        val collections = collectionDao.observeCollections().first()
        val pins = pinnedDao.observeAll().first()
        val recentViews = recentViewDao.observeRecent(Int.MAX_VALUE).first()

        val backup = UserDataBackup(
            timestamp = System.currentTimeMillis(),
            records = records.map { toBackup(it) },
            collections = collections.map { c ->
                val filmIds = collectionDao.observeFilmsInCollection(c.id).first().map { it.id }
                toBackup(c, filmIds)
            },
            pins = pins.map { toBackup(it) },
            recentViews = recentViews.map { toBackup(it) }
        )

        val file = File(backupDir, "backup_${backup.timestamp}.json")
        file.writeText(gson.toJson(backup))
        return file
    }

    suspend fun import(file: File): Result<Unit> = runCatching {
        val json = file.readText()
        val backup = gson.fromJson(json, UserDataBackup::class.java)

        backup.records.forEach { r ->
            recordDao.upsert(
                UserFilmRecordEntity(
                    filmId = r.filmId,
                    status = runCatching { WatchStatus.valueOf(r.status) }.getOrDefault(WatchStatus.WATCHED),
                    rating = r.rating,
                    note = r.note,
                    watchedAt = r.watchedAt,
                    updatedAt = r.updatedAt
                )
            )
        }

        backup.collections.forEach { c ->
            val id = collectionDao.insert(
                com.example.third_dz.data.local.CollectionEntity(
                    name = c.name,
                    colorHex = c.colorHex,
                    createdAt = c.createdAt
                )
            )
            c.filmIds.forEach { filmId ->
                collectionDao.addFilm(CollectionFilmCrossRef(id, filmId))
            }
        }

        backup.pins.forEach { p ->
            pinnedDao.pin(
                PinnedEntity(
                    entityType = runCatching { PinnedType.valueOf(p.entityType) }.getOrDefault(PinnedType.PERSON),
                    entityId = p.entityId,
                    note = p.note,
                    pinnedAt = p.pinnedAt
                )
            )
        }

        backup.recentViews.forEach { rv ->
            recentViewDao.insert(RecentViewEntity(filmId = rv.filmId, openedAt = rv.openedAt))
        }
    }

    private fun rotateBackups() {
        val files = backupDir.listFiles()
            ?.filter { it.name.startsWith("backup_") && it.extension == "json" }
            ?.sortedBy { it.lastModified() }
            ?.toMutableList()
            ?: return

        while (files.size >= 4) {
            files.first().delete()
            files.removeFirst()
        }
    }

    private fun toBackup(entity: UserFilmRecordEntity) = BackupRecord(
        filmId = entity.filmId,
        status = entity.status.name,
        rating = entity.rating,
        note = entity.note,
        watchedAt = entity.watchedAt,
        updatedAt = entity.updatedAt
    )

    private fun toBackup(entity: com.example.third_dz.data.local.CollectionEntity, filmIds: List<String>) = BackupCollection(
        id = entity.id,
        name = entity.name,
        colorHex = entity.colorHex,
        createdAt = entity.createdAt,
        filmIds = filmIds
    )

    private fun toBackup(entity: PinnedEntity) = BackupPin(
        entityType = entity.entityType.name,
        entityId = entity.entityId,
        note = entity.note,
        pinnedAt = entity.pinnedAt
    )

    private fun toBackup(entity: RecentViewEntity) = BackupRecentView(
        filmId = entity.filmId,
        openedAt = entity.openedAt
    )
}

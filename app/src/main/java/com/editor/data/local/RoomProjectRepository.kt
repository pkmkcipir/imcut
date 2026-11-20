package com.editor.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val aspectRatio: String,
    val targetResolution: String,
    val targetFps: Int,
    val storageBytes: Long,
    val updatedAtEpochMs: Long,
)

@Entity(
    tableName = "clips",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("projectId"), Index("timelineStartMs")],
)
data class ClipEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val mediaAssetId: String,
    val label: String,
    val timelineStartMs: Long,
    val timelineEndMs: Long,
    val sourceInMs: Long,
    val sourceOutMs: Long,
    val speedValue: Float,
    val volume: Float,
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAtEpochMs DESC")
    fun observeAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM clips WHERE projectId = :projectId ORDER BY timelineStartMs ASC")
    suspend fun getClipsForProject(projectId: String): List<ClipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClips(clips: List<ClipEntity>)

    @Query("DELETE FROM clips WHERE projectId = :projectId")
    suspend fun deleteClipsForProject(projectId: String)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: String)

    @Transaction
    suspend fun saveProjectWithClips(project: ProjectEntity, clips: List<ClipEntity>) {
        upsertProject(project)
        deleteClipsForProject(project.id)
        upsertClips(clips)
    }
}

@Database(entities = [ProjectEntity::class, ClipEntity::class], version = 1, exportSchema = true)
abstract class StudioDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}

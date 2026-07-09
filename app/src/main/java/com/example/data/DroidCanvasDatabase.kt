package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "boards")
data class Board(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val backgroundType: String = "SLATE",
    val canvasScale: Float = 1f,
    val canvasTranslateX: Float = 0f,
    val canvasTranslateY: Float = 0f
)

@Entity(
    tableName = "canvas_items",
    foreignKeys = [
        ForeignKey(
            entity = Board::class,
            parentColumns = ["id"],
            childColumns = ["boardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["boardId"])]
)
data class CanvasItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val boardId: Int,
    val fullPath: String,     // private filesDir path to original image
    val thumbPath: String,    // private filesDir path to downsampled thumbnail
    val posX: Float,          // X coordinate in canvas space
    val posY: Float,          // Y coordinate in canvas space
    val width: Float,         // native/initial width
    val height: Float,        // native/initial height
    val scale: Float = 1f,    // visual scale factor
    val rotation: Float = 0f,  // in degrees
    val zIndex: Int = 0,      // ordering
    val isPinned: Boolean = false,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false
)

@Dao
interface DroidCanvasDao {
    @Query("SELECT * FROM boards ORDER BY createdAt ASC")
    fun getAllBoards(): Flow<List<Board>>

    @Query("SELECT * FROM boards WHERE id = :id LIMIT 1")
    suspend fun getBoardById(id: Int): Board?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: Board): Long

    @Update
    suspend fun updateBoard(board: Board)

    @Delete
    suspend fun deleteBoard(board: Board)

    @Query("SELECT * FROM canvas_items WHERE boardId = :boardId ORDER BY zIndex ASC")
    fun getItemsForBoard(boardId: Int): Flow<List<CanvasItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCanvasItem(item: CanvasItem): Long

    @Update
    suspend fun updateCanvasItem(item: CanvasItem)

    @Delete
    suspend fun deleteCanvasItem(item: CanvasItem)

    @Query("SELECT MAX(zIndex) FROM canvas_items WHERE boardId = :boardId")
    suspend fun getMaxZIndex(boardId: Int): Int?
}

@Database(entities = [Board::class, CanvasItem::class], version = 5, exportSchema = false)
abstract class DroidCanvasDatabase : RoomDatabase() {
    abstract fun droidCanvasDao(): DroidCanvasDao

    companion object {
        @Volatile
        private var INSTANCE: DroidCanvasDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE boards ADD COLUMN backgroundType TEXT NOT NULL DEFAULT 'SLATE'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE boards ADD COLUMN canvasScale REAL NOT NULL DEFAULT 1.0")
                db.execSQL("ALTER TABLE boards ADD COLUMN canvasTranslateX REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE boards ADD COLUMN canvasTranslateY REAL NOT NULL DEFAULT 0.0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE canvas_items ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE canvas_items ADD COLUMN flipHorizontal INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE canvas_items ADD COLUMN flipVertical INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): DroidCanvasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DroidCanvasDatabase::class.java,
                    "droid_canvas_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class DroidCanvasRepository(private val dao: DroidCanvasDao) {
    val allBoards: Flow<List<Board>> = dao.getAllBoards()

    suspend fun getBoardById(id: Int): Board? = dao.getBoardById(id)

    suspend fun insertBoard(board: Board): Long = dao.insertBoard(board)

    suspend fun updateBoard(board: Board) = dao.updateBoard(board)

    suspend fun deleteBoard(board: Board) = dao.deleteBoard(board)

    fun getItemsForBoard(boardId: Int): Flow<List<CanvasItem>> = dao.getItemsForBoard(boardId)

    suspend fun insertCanvasItem(item: CanvasItem): Long = dao.insertCanvasItem(item)

    suspend fun updateCanvasItem(item: CanvasItem) = dao.updateCanvasItem(item)

    suspend fun deleteCanvasItem(item: CanvasItem) = dao.deleteCanvasItem(item)

    suspend fun getMaxZIndex(boardId: Int): Int? = dao.getMaxZIndex(boardId)
}

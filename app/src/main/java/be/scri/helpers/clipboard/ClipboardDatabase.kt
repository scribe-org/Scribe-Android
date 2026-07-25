package be.scri.helpers.clipboard

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ClipboardItem::class], version = 1, exportSchema = false)
abstract class ClipboardDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao

    companion object {
        @Volatile
        private var instance: ClipboardDatabase? = null

        fun getDatabase(context: Context): ClipboardDatabase =
            instance ?: synchronized(this) {
                val newInstance =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            ClipboardDatabase::class.java,
                            "scribe_clipboard_db",
                        ).build()
                instance = newInstance
                newInstance
            }
    }
}

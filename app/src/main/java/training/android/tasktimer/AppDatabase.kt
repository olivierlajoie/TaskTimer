package training.android.tasktimer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private const val TAG = "AppDatabase"
private const val DATABASE_NAME = "TaskTimer.db"
private const val DATABASE_VERSION = 2

internal class AppDatabase private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init { Log.d(TAG, "AppDatabase: init") }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "onCreate: starts")

        val sSQLTask = """CREATE TABLE ${TasksContract.TABLE_NAME} (
            ${TasksContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
            ${TasksContract.Columns.TASK_DESC} TEXT,
            ${TasksContract.Columns.TASK_ORDER} INTEGER)""".trimIndent()

        Log.d(TAG, sSQLTask)
        db?.execSQL(sSQLTask)

        addTimingsTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade: starts")

        when(oldVersion) {
            1 -> addTimingsTable(db)
            else -> throw IllegalStateException("onUpgrade() with unknown newVersion $newVersion")
        }
    }

    private fun addTimingsTable(db: SQLiteDatabase?) {
        val sSQLTiming = """CREATE TABLE ${TimingsContract.TABLE_NAME} (
            ${TimingsContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TimingsContract.Columns.TIMING_TASK_ID} INTEGER NOT NULL,
            ${TimingsContract.Columns.TIMING_START_TIME} INTEGER,
            ${TimingsContract.Columns.TIMING_DURATION} INTEGER)""".trimIndent()

        Log.d(TAG, sSQLTiming)
        db?.execSQL(sSQLTiming)

        val sSQLCleaner = """CREATE TRIGGER Remove_Task
            AFTER DELETE ON ${TasksContract.TABLE_NAME}
            FOR EACH ROW
            BEGIN
            DELETE FROM ${TimingsContract.TABLE_NAME}
            WHERE ${TimingsContract.Columns.TIMING_TASK_ID} = OLD.${TasksContract.Columns.ID};
            END;""".trimIndent()

        Log.d(TAG, sSQLCleaner)
        db?.execSQL(sSQLCleaner)
    }

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)


}
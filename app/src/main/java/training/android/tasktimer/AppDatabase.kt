package training.android.tasktimer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private const val TAG = "AppDatabase"
private const val DATABASE_NAME = "TaskTimer.db"
private const val DATABASE_VERSION = 3

internal class AppDatabase private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init { Log.d(TAG, "AppDatabase: init") }

    override fun onCreate(db: SQLiteDatabase?) {
        //CREATE TABLE Tasks (_id INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, desc TEXT, sortOrder INTEGER);
        Log.d(TAG, "onCreate: starts")

        val sSQL = """CREATE TABLE ${TasksContract.TABLE_NAME} (
            |${TasksContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            |${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
            |${TasksContract.Columns.TASK_DESC} TEXT,
            |${TasksContract.Columns.TASK_ORDER} INTEGER
            |)""".trimMargin()

        Log.d(TAG, sSQL)

        db?.execSQL(sSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade: starts")

        when(oldVersion) {
            1 -> {/*upgrade logic*/}
            else -> throw IllegalStateException("onUpgrade() with unknown newVersion $newVersion")
        }
    }

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)
}
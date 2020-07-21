package training.android.tasktimer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private const val TAG = "AppDatabase"
private const val DATABASE_NAME = "TaskTimer.db"
private const val DATABASE_VERSION = 3

internal class AppDatabase constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init { Log.d(TAG, "AppDatabase: init") }

    override fun onCreate(db: SQLiteDatabase?) {
        //CREATE TABLE Tasks (_id INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, desc TEXT, sortOrder INTEGER);
        Log.d(TAG, "OnCreate: starts")

        val sSQL = """CREATE TABLE ${TasksContract.TABLE_NAME} (
            |${TasksContract.Columms.ID} INTEGER PRIMARY KEY NOT NULL,
            |${TasksContract.Columms.TASK_NAME} TEXT NOT NULL,
            |${TasksContract.Columms.TASK_DESC} TEXT,
            |${TasksContract.Columms.TASK_ORDER} INTEGER,
            |)""".trimMargin()

        Log.d(TAG, sSQL)

        db?.execSQL(sSQL)

        Log.d(TAG,"WTF")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}
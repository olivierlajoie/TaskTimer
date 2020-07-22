package training.android.tasktimer

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val projection = arrayOf(TasksContract.Columns.TASK_NAME, TasksContract.Columns.TASK_ORDER)
        val sortColumn = TasksContract.Columns.TASK_ORDER

        val cursor = contentResolver.query(TasksContract.CONTENT_URI, null, null, null, sortColumn)

        cursor.use {
            while (it!!.moveToNext()) {
                with(cursor) {
                    val id = it.getLong(0)
                    val name = it.getString(1)
                    val desc = it.getString(2)
                    val order = it.getString(3)
                    val result = "ID: $id NAME $name DESC $desc ORDER $order"

                    Log.d(TAG, result)
                }
            }
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    private fun testUpdateTwo() {
        val values = ContentValues().apply {
            put(TasksContract.Columns.TASK_ORDER, 99)
            put(TasksContract.Columns.TASK_DESC, "Completed")
        }

        val selection = TasksContract.Columns.TASK_ORDER + " + 2"
        val rowAffected = contentResolver.update(TasksContract.CONTENT_URI, values, selection, null)
        Log.d(TAG, "Updated row uri is $rowAffected")
    }

    private fun testUpdate() {
        val values = ContentValues().apply {
            put(TasksContract.Columns.TASK_NAME, "Content Provider")
            put(TasksContract.Columns.TASK_DESC, "Record content providers videos")
        }

        val taskUri = TasksContract.buildUriFromId(4)
        val rowAffected = contentResolver.update(taskUri, values, null, null)
        Log.d(TAG, "Updated row uri is $rowAffected")
    }

    private fun testInsert() {
        val values = ContentValues().apply {
            put(TasksContract.Columns.TASK_NAME, "New Task 1")
            put(TasksContract.Columns.TASK_DESC, "Description 1")
            put(TasksContract.Columns.TASK_ORDER, 2)
        }

        val uri = contentResolver.insert(TasksContract.CONTENT_URI, values)
        Log.d(TAG, "New row uri is $uri")
        Log.d(TAG, "id is ${uri?.let { TasksContract.getId(it) }}")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.menumain_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
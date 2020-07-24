package training.android.tasktimer

import android.content.ContentValues
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked {

    private var mTwoPane = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        var fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        if(fragment != null) showEditPane()
        else {
            task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
            main_fragment_container.view?.visibility = View.VISIBLE
        }

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
    }

    private fun showEditPane() {
        task_details_container.visibility = View.VISIBLE
        main_fragment_container.view?.visibility = if(mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane: starts")
        if(fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }

        task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
        main_fragment_container.view?.visibility = View.VISIBLE
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: starts")
        var fragment = supportFragmentManager.findFragmentById(R.id.task_details_container)
        removeEditPane(fragment)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menumain_addTask -> taskEditRequest(null)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")
        val newFragment = AddEditFragment.newInstance(task)
        supportFragmentManager.beginTransaction()
            .replace(R.id.task_details_container, newFragment)
            .commit()

        showEditPane()
    }
}
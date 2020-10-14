package training.android.tasktimer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.task_list_items.*

private const val TAG = "MainActivity"
private const val DIALOG_CANCEL_EDIT_ID = 1

class MainActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked,
    MainActivityFragment.OnTaskEdit, AppDialog.DialogEvents {

    private var mTwoPane = false
    private var aboutDialog: AlertDialog? = null
    private val viewModel by lazy { ViewModelProvider(this).get(AppViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.maint_fragment_container, MainActivityFragment.newInstance("null","null"))
                .commitAllowingStateLoss()
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val fragment = findFragmentById(R.id.task_details_container)
        if(fragment != null) showEditPane()
        else {
            task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
            maint_fragment_container.visibility = View.VISIBLE
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

        viewModel.timing.observe(this, Observer<String> { timing -> current_task.text = timing })
    }

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    private fun showEditPane() {
        task_details_container.visibility = View.VISIBLE
        maint_fragment_container.visibility = if(mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane: starts")
        if(fragment != null) {
            removeFragment(fragment)
        }

        task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
        maint_fragment_container.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: starts")
        val fragment = findFragmentById(R.id.task_details_container)
        removeEditPane(fragment)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menumain_settings -> showSettingsDialog()
            R.id.menumain_addTask -> taskEditRequest(null)
            R.id.menumain_about -> showAboutDialog()
            android.R.id.home -> {
                var fragment = findFragmentById(R.id.task_details_container)
                if((fragment is AddEditFragment) && fragment.isDirty()) {
                    showConfirmationDialog(DIALOG_CANCEL_EDIT_ID,
                    getString(R.string.cancel_dialog_message),
                    R.string.cancel_dialog_positive,
                    R.string.cancel_dialog_negative)
                }
                else
                    removeEditPane()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showAboutDialog() {
        val messageView = layoutInflater.inflate(R.layout.about, null, false)
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.app_name)
        builder.setIcon(R.mipmap.ic_launcher)

        builder.setPositiveButton(R.string.ok) { _, _ ->
            if(aboutDialog != null && aboutDialog?.isShowing == true) {
                aboutDialog?.dismiss()
            }
        }

        aboutDialog = builder.setView(messageView).create()
        aboutDialog?.setCanceledOnTouchOutside(true)

        val aboutVersion = messageView.findViewById<TextView>(R.id.about_version) as TextView
        aboutVersion.text = BuildConfig.VERSION_NAME

        aboutDialog?.show()
    }

    private fun showSettingsDialog() {
        val dialog = SettingsDialog()
        dialog.show(supportFragmentManager, null)
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")
        val newFragment = AddEditFragment.newInstance(task)
        replaceFragment(newFragment, R.id.task_details_container)

        showEditPane()
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: starts")
        val fragment = findFragmentById(R.id.task_details_container)
        if(fragment == null || mTwoPane)
            super.onBackPressed()
        else {
            if((fragment is AddEditFragment) && fragment.isDirty()) {
                showConfirmationDialog(DIALOG_CANCEL_EDIT_ID,
                    getString(R.string.cancel_dialog_message),
                    R.string.cancel_dialog_positive,
                    R.string.cancel_dialog_negative)
            }
            else
                removeEditPane()
        }
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        if(dialogId == DIALOG_CANCEL_EDIT_ID) {
            val fragment = findFragmentById(R.id.task_details_container)
            removeEditPane(fragment)
        }
    }

    override fun onStop() {
        super.onStop()
        if(aboutDialog?.isShowing() == true)
            aboutDialog?.dismiss()
    }
}

package training.android.tasktimer

import android.app.Application
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "AppViewModel"

class AppViewModel (application: Application) : AndroidViewModel(application) {
    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadTasks()
        }
    }

    private var currentTiming: Timing? = null

    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor>
        get() = databaseCursor

    init {
        Log.d(TAG, "TaskTimerViewModel: created")
        getApplication<Application>().contentResolver.registerContentObserver(TasksContract.CONTENT_URI, true, contentObserver)
        loadTasks()
    }

    private val taskTiming = MutableLiveData<String>()
    val timing: LiveData<String>
    get() = taskTiming

    private fun loadTasks() {
        val projection = arrayOf(TasksContract.Columns.ID,
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_DESC,
            TasksContract.Columns.TASK_ORDER)
        val sortOrder = "${TasksContract.Columns.TASK_ORDER}, ${TasksContract.Columns.TASK_NAME}"

        GlobalScope.launch {
            val cursor = getApplication<Application>().contentResolver.query(
                TasksContract.CONTENT_URI,
                projection, null, null,
                sortOrder)
            databaseCursor.postValue(cursor)
        }
    }

    fun saveTask(task: Task): Task {
        val values = ContentValues()
        if(task.name.isNotEmpty()) {
            values.put(TasksContract.Columns.TASK_NAME, task.name)
            values.put(TasksContract.Columns.TASK_DESC, task.desc)
            values.put(TasksContract.Columns.TASK_ORDER, task.sortOrder)

            if(task.id == 0L) {
                GlobalScope.launch {
                    Log.d(TAG, "saveTask: adding new task")
                    val uri = getApplication<Application>().contentResolver?.insert(TasksContract.CONTENT_URI, values)
                    if (uri != null) task.id = TasksContract.getId(uri)
                }
            } else {
                GlobalScope.launch {
                    Log.d(TAG, "saveTask: updating task")
                    getApplication<Application>().contentResolver?.update(TasksContract.buildUriFromId(task.id), values, null, null)
                }

            }
        }
        return task
    }

    fun deleteTasks(taskId: Long) {
        Log.d(TAG, "deleteTask: task")
        GlobalScope.launch {
            getApplication<Application>().contentResolver?.delete(TasksContract.buildUriFromId(taskId), null, null)
        }
    }

    fun timeTask(task: Task) {
        Log.d(TAG, "timeTask: task")

        val timingRecord = currentTiming

        if(timingRecord == null) {
            currentTiming = Timing(task.id)
            saveTiming(currentTiming!!)
        } else {
            timingRecord.setDuration()
            saveTiming(timingRecord)

            if(task.id == timingRecord.taskId)
                currentTiming = null
            else {
                currentTiming = Timing(task.id)
                saveTiming(currentTiming!!)
            }
        }

        taskTiming.value = if (currentTiming != null) task.name else null
    }

    private fun saveTiming(currentTiming: Timing) {
        Log.d(TAG, "saveTiming: called")

        val inserting = (currentTiming.duration == 0L)

        val values = ContentValues().apply {
            if(inserting) {
                put(TimingsContract.Columns.TIMING_TASK_ID, currentTiming.taskId)
                put(TimingsContract.Columns.TIMING_START_TIME, currentTiming.startTime)
            }
            put(TimingsContract.Columns.TIMING_DURATION, currentTiming.duration)
        }

        GlobalScope.launch {
            if(inserting) {
                val uri = getApplication<Application>().contentResolver?.insert(TimingsContract.CONTENT_URI, values)
                if (uri != null)
                    currentTiming.id = TimingsContract.getId(uri)
            } else
                getApplication<Application>().contentResolver?.update(TimingsContract.buildUriFromId(currentTiming.id), values, null, null)
        }
    }

    override fun onCleared() {
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }
}
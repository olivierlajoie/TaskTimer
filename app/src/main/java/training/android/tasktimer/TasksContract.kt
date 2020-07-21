package training.android.tasktimer

import android.provider.BaseColumns

object TasksContract {
    internal const val TABLE_NAME = "Tasks"

    object Columms {
        const val ID = BaseColumns._ID
        const val TASK_NAME = "name"
        const val TASK_DESC = "desc"
        const val TASK_ORDER = "sortOrder"
    }
}
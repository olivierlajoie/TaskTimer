package training.android.tasktimer

import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.task_list_items.view.*
import java.lang.IllegalStateException

class TaskViewHolder(override val containerView: View) :
    RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(task: Task, listener: CursorRecyclerViewAdapter.OnTaskClickListener) {
        itemView.tli_name.text = task.name
        itemView.tli_desc.text = task.desc
        itemView.tli_edit.visibility = View.VISIBLE
        itemView.tli_delete.visibility = View.VISIBLE

        if(itemView.tli_desc.text.isNullOrEmpty())
            itemView.tli_desc.visibility = View.GONE
        else
            itemView.tli_desc.visibility = View.VISIBLE

        itemView.tli_edit.setOnClickListener { listener.onEditClick(task) }
        itemView.tli_delete.setOnClickListener { listener.onDeleteClick(task) }
        containerView.setOnClickListener { listener.onLongClick(task) }
    }
}

private const val TAG = "CursorRecyclerViewAdapt"

class CursorRecyclerViewAdapter(private var cursor: Cursor?, private val listener: OnTaskClickListener) :
    RecyclerView.Adapter<TaskViewHolder>() {

    interface OnTaskClickListener {
        fun onEditClick(task: Task)
        fun onDeleteClick(task: Task)
        fun onLongClick(task: Task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        Log.d(TAG, "onCreateViewHolder: new view requested")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_items, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: starts")

        val cursor = cursor     // avoid problems with smart cast

        if (position % 2 == 1)
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.colorSecondaryRecycler))
        else
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryRecycler))

        if(cursor == null || cursor.count == 0) {
            Log.d(TAG, "onBindViewHolder: providing instructions")
            holder.itemView.current_task.setText(R.string.no_task_message)
            holder.itemView.tli_name.setText(R.string.instructions_title)
            holder.itemView.tli_desc.setText(R.string.instructions_desc)
            holder.itemView.tli_edit.visibility = View.GONE
            holder.itemView.tli_delete.visibility = View.GONE
            holder.itemView.tli_desc.visibility = View.VISIBLE
        } else {
            if(!cursor.moveToPosition(position)) {
                throw IllegalStateException("Couldn't move cursor to position $position")
            }

            // Create a Task object from the data in the cursor
            val task = Task(
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_NAME)),
                if(cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESC)) != null) cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESC)) else "No description",
                cursor.getInt(cursor.getColumnIndex(TasksContract.Columns.TASK_ORDER)))
            // Remember that the id isn't set in the constructor
            task.id = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.ID))

            if(cursor.isFirst)
                holder.itemView.current_task.visibility = View.VISIBLE
            else
                holder.itemView.current_task.visibility = View.GONE

            holder.bind(task, listener)
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: starts")
        val cursor = cursor
        val count = if (cursor == null || cursor.count == 0) {
            1   // fib, because we populate a single ViewHolder with instructions
        } else {
            cursor.count
        }
        Log.d(TAG, "returning $count")
        return count
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.
     * The returned old Cursor is *not* closed.
     *
     * @param newCursor The new cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't one.
     * If the given new Cursor is the same instance as the previously set
     * Cursor, null is also returned.
     */
    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursor) {
            return null
        }

        val numItems = itemCount

        val oldCursor = cursor
        cursor = newCursor
        if (newCursor != null) {
            // notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }
}

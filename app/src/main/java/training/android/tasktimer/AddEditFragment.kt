package training.android.tasktimer

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_add_edit.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.lang.RuntimeException

private const val TAG = "AddEditFragment"
private const val ARG_TASK = "task"

class AddEditFragment : Fragment() {
    private var task: Task? = null
    private var listener: OnSaveClicked? = null
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(AppViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASK)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: starts")
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: starts")
        if(savedInstanceState == null) {
            val task = task
            if (task != null) {
                Log.d(TAG, "onViewCreated: update record ${task.id}")
                addedit_name.setText(task.name)
                addedit_desc.setText(task.desc)
                addedit_order.setText(task.sortOrder.toString())
            } else
                Log.d(TAG, "onViewCreated: adding new record")
        }
    }

    private fun taskFromUI(): Task {
        val sortOrder = if (addedit_order.text.isNotEmpty()) Integer.parseInt(addedit_order.text.toString()) else 0
        val newTask = Task(addedit_name.text.toString(), addedit_desc.text.toString(), sortOrder)
        newTask.id = task?.id ?: 0
        return newTask
    }

    private fun saveTask() {
        Log.d(TAG, "saveTask: starts")
        val newTask = taskFromUI()
        if(newTask != task) task = viewModel.saveTask(newTask)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: starts")
        super.onActivityCreated(savedInstanceState)

        if(listener is AppCompatActivity) {
            var actionBar = (listener as AppCompatActivity?)?.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        addedit_save.setOnClickListener {
            saveTask()
            listener?.onSaveClicked()
        }
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: starts")
        super.onAttach(context)
        if(context is OnSaveClicked) listener = context
        else throw RuntimeException("${context}must implement OnSaveClicked")
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: starts")
        super.onDetach()
        listener = null
    }

    interface OnSaveClicked {
        fun onSaveClicked()
    }

    companion object {
        @JvmStatic
        fun newInstance(task: Task?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
    }
}
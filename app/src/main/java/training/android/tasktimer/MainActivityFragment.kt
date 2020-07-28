package training.android.tasktimer

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_main.*
import java.lang.RuntimeException

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MainActivityFragment : Fragment(), CursorRecyclerViewAdapter.OnTaskClickListener {
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(AppViewModel::class.java) }
    private val mAdapter = CursorRecyclerViewAdapter(null, this)
    private var param1: String? = null
    private var param2: String? = null

    interface OnTaskEdit {
        fun onTaskEdit(task: Task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        viewModel.cursor.observe(this, Observer { cursor -> mAdapter.swapCursor(cursor)?.close() })
    }

    override fun onEditClick(task: Task) {
        (activity as OnTaskEdit?)?.onTaskEdit(task)
    }

    override fun onDeleteClick(task: Task) {
        viewModel.deleteTasks(task.id)
    }

    override fun onLongClick(task: Task) {
        TODO("Not yet implemented")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task_list.layoutManager = LinearLayoutManager(context)
        task_list.adapter = mAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context !is OnTaskEdit) throw RuntimeException("${context.toString()} must implement OnTaskEdit")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainActivityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
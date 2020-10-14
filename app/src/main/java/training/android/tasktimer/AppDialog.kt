package training.android.tasktimer

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDialogFragment
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

private const val TAG = "AppDialog"

const val DIALOG_ID = "id"
const val DIALOG_MESSAGE = "message"
const val DIALOG_POSITIVE_RID = "positive_rid"
const val DIALOG_NEGATIVE_RID = "negative_rid"

class AppDialog : AppCompatDialogFragment() {

    private var dialogEvents: DialogEvents? = null

    internal interface DialogEvents {
        fun onPositiveDialogResult(dialogId: Int, args: Bundle)
//        fun onNegativeDialogResult(dialogId: Int, args: Bundle)
//        fun onDialogCancelled(dialogId: Int, args: Bundle)
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: starts with context $context")
        super.onAttach(context)

        dialogEvents = try {
            parentFragment as DialogEvents
        }
        catch (e: TypeCastException) {
            try {
                context as DialogEvents
            }
            catch (e: ClassCastException) {
                throw ClassCastException("Activity $context must implement AppDialog.DialogEvents interface")
            }
        }
        catch (e: ClassCastException) {
            throw ClassCastException("Fragment $parentFragment must implement AppDialog.DialogEvents interface")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog called")

        val builder = AlertDialog.Builder(requireContext())

        val arguments = arguments
        val dialogId: Int
        val messageString: String?
        var positiveStringId: Int
        var negativeStringId: Int

        if (arguments != null) {
            dialogId = arguments.getInt(DIALOG_ID)
            messageString = arguments.getString(DIALOG_MESSAGE)

            if (dialogId == 0 || messageString == null) throw IllegalArgumentException("DIALOG_ID & DIALOG_MESSAGE in the bundle can't be null")

            positiveStringId = arguments.getInt(DIALOG_POSITIVE_RID)
            if (positiveStringId == 0) positiveStringId = R.string.ok
            negativeStringId = arguments.getInt(DIALOG_NEGATIVE_RID)
            if (negativeStringId == 0) negativeStringId = R.string.cancel

        } else throw IllegalArgumentException("Must provide DIALOG_ID & DIALOG_MESSAGE in the bundle")

        return builder.setMessage(messageString)
            .setPositiveButton(positiveStringId) { dialogInterface, which ->
                dialogEvents?.onPositiveDialogResult(dialogId, arguments)
            }
            .setNegativeButton(negativeStringId) { dialogInterface, which ->
                //dialogEvents?.onNegativeDialogResult(dialogId, arguments)
            }
            .create()
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach called")
        super.onDetach()

        dialogEvents = null
    }

    override fun onCancel(dialog: DialogInterface) {
        Log.d(TAG, "onCancel called")
        val dialogId = requireArguments().getInt(DIALOG_ID)
//        dialogEvents?.onDialogCancelled(dialogId)
    }
}
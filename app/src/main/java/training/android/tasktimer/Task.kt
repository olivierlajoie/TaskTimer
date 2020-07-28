package training.android.tasktimer

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Task(val name: String, val desc: String, val sortOrder: Int, var id: Long = 0) : Parcelable
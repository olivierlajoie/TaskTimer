package training.android.tasktimer

import android.os.Bundle
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDialogFragment
import kotlinx.android.synthetic.main.settings.*
import java.util.*

private const val TAG = "SettingsDialog"

const val SETTINGS_FRIST_DAY_OF_WEEK = "FirstDay"
const val SETTINGS_IGNORE_LESS_THAN = "IgnoreLessThan"
const val SETTINGS_DEFAULT_IGNORE_LESS_THAN = 0

private val deltas = intArrayOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 120, 180, 240, 300, 360, 420, 540, 600, 900, 1800, 2700)

class SettingsDialog: AppCompatDialogFragment() {

    private val defaultFirstDayOfWeek = GregorianCalendar(Locale.getDefault()).firstDayOfWeek
    private var firstDay = defaultFirstDayOfWeek
    private var ignoreLessThan = SETTINGS_DEFAULT_IGNORE_LESS_THAN

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(AppCompatDialogFragment.STYLE_NORMAL, R.style.SettingsDialogStyle)

        retainInstance = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setTitle(R.string.action_settings)

        okBtn.setOnClickListener {
            saveValues()
            dismiss()
        }

        cancelBtn.setOnClickListener {
            dismiss()
        }

        ignoreSeconds.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(progress < 12)
                    ignoreSecondsTitle.text = getString(R.string.settingsIgnoreSecondsTitle, deltas[progress], resources.getQuantityString(R.plurals.settingsLittleUnits, deltas[progress]))
                else
                    ignoreSecondsTitle.text = getString(R.string.settingsIgnoreSecondsTitle, deltas[progress] / 60, resources.getQuantityString(R.plurals.settingsBigUnits, deltas[progress] / 60))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if(savedInstanceState == null) {
            readValues()

            firstDaySpinner.setSelection(firstDay - GregorianCalendar.SUNDAY)

            val seekBarValue = deltas.binarySearch(ignoreLessThan)

            if(seekBarValue < 0) throw IndexOutOfBoundsException("Value $seekBarValue not found in deltas array")

            ignoreSeconds.max = deltas.size - 1
            ignoreSeconds.progress = seekBarValue

            if(ignoreLessThan < 60)
                ignoreSecondsTitle.text = getString(R.string.settingsIgnoreSecondsTitle, ignoreLessThan, resources.getQuantityString(R.plurals.settingsLittleUnits, ignoreLessThan))
            else
                ignoreSecondsTitle.text = getString(R.string.settingsIgnoreSecondsTitle, ignoreLessThan / 60, resources.getQuantityString(R.plurals.settingsBigUnits, ignoreLessThan / 60))
        }
    }

    private fun readValues() {
        with(getDefaultSharedPreferences(context)) {
            firstDay = getInt(SETTINGS_FRIST_DAY_OF_WEEK, defaultFirstDayOfWeek)
            ignoreLessThan = getInt(SETTINGS_IGNORE_LESS_THAN, SETTINGS_DEFAULT_IGNORE_LESS_THAN)
        }
    }

    private fun saveValues() {
        val newFirstDay = firstDaySpinner.selectedItemPosition + GregorianCalendar.SUNDAY
        val newIgnoreLessThan = deltas[ignoreSeconds.progress]

        with(getDefaultSharedPreferences(context).edit()) {
            if(newFirstDay != firstDay)
                putInt(SETTINGS_FRIST_DAY_OF_WEEK, newFirstDay)

            if(newIgnoreLessThan != ignoreLessThan)
                putInt(SETTINGS_IGNORE_LESS_THAN, newIgnoreLessThan)

            apply()
        }
    }
}
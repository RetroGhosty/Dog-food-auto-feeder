package talipapa.lab.feeder_controller

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import talipapa.lab.feeder_controller.databinding.FragmentNewScheduleSheetBinding



class NewScheduleSheet : BottomSheetDialogFragment() {

    private lateinit var binding : FragmentNewScheduleSheetBinding
    private val initializeSchedHelper = FirebaseDataHelper("/feedingSchedules")
    private var newSched : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // https://youtu.be/RzjCMa4GBD4?si=ZxI7Aa1U6YTKnQ6X&t=416
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNewScheduleSheetBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val initFormattedHour = String.format("%02d", binding.timePicker.hour)
        val initFormattedMinute = String.format("%02d", binding.timePicker.minute)
        newSched = "$initFormattedHour:$initFormattedMinute"

        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            val formattedMinute = String.format("%02d", minute)
            val formattedHour = String.format("%02d", hourOfDay)
            newSched = "$formattedHour:$formattedMinute"
        }

        binding.addScheduleCompactBtn.setOnClickListener{
            if (newSched != null) {
                initializeSchedHelper.addSched(newSched!!)
                dismiss()
            }
        }
    }
}
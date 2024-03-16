package talipapa.lab.feeder_controller

import Models.FeedSignal
import Models.Schedule
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import talipapa.lab.feeder_controller.databinding.ActivityMainBinding
import java.security.KeyStore.TrustedCertificateEntry
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private val TAG = "FirebaseSnippets"

    private lateinit var binding : ActivityMainBinding
    private lateinit var servoActivationStatus : TextView
    private lateinit var newRecyclerView : RecyclerView

    private lateinit var feedNow : Button
    private lateinit var addSchedButton : Button


    private lateinit var newSchedArrayList: ArrayList<Schedule>
    private var schedules : Map<String, String>? = null

    private val initializeSchedHelper = FirebaseDataHelper("/feedingSchedules")
    private val initializeServoSignalHelper = FirebaseDataHelper("/feedSignal")


    @SuppressLint("SetTextI18n")
    private val ref = FirebaseDatabase.getInstance().getReference("/")
    private val menuListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val post = snapshot.getValue<FeedSignal>()
//            Log.d("HELP", post?.feedingSchedules.toString())
            servoActivationStatus =  findViewById<TextView>(R.id.servoStatusData)

            servoActivationStatus.text = "${post?.feedSignal}"

            schedules = post?.feedingSchedules


            newSchedArrayList.clear()
            if (schedules != null){
                for (i in schedules!!){
                    val tempSchedObj = Schedule(i.key, i.value)
                    newSchedArrayList.add(tempSchedObj)
                }
            }

            newRecyclerView.adapter = RecycleViewAdapter(newSchedArrayList)

        }
        @SuppressLint("SetTextI18n")
        override fun onCancelled(error: DatabaseError) {
//            val scheduleArr : TextView = findViewById<TextView>(R.id.scheduleArrData)
            servoActivationStatus = findViewById<TextView>(R.id.servoStatusData)
            servoActivationStatus.text = "Cancelled or something went wrong."
//            scheduleArr.text = "Cancelled or something went wrong."

        }
    }






    

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        feedNow = findViewById(R.id.button)

        feedNow.setOnClickListener {
            Log.d("BUTTON", "Button is clicked")
            initializeServoSignalHelper.changeServoSignal(true)
        }

        addSchedButton = findViewById(R.id.button3)
        newRecyclerView = findViewById(R.id.recyclerView)
        newRecyclerView.layoutManager = LinearLayoutManager(this)

        newSchedArrayList = arrayListOf<Schedule>()

        addSchedButton.setOnClickListener {
//            val firebaseHelper = FirebaseDataHelper("feedingSchedules")
//            var dialogView = layoutInflater.inflate(R.layout.fragment_new_schedule_sheet, null)
//            firebaseHelper.addSched()
            NewScheduleSheet().show(supportFragmentManager, "newScheduleTag")

        }


        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Main) {
            ref.addValueEventListener(menuListener)
            Log.d(TAG, "Coroutine thread dispatched!")
            withContext(Dispatchers.IO){
                Log.d(TAG, "Coroutine thread dispatched! ${Thread.currentThread().name}")
            }
        }

    }

    fun showBottomSheet(view: View){

    }

    override fun onDestroy() {
        super.onDestroy()
        ref.removeEventListener(menuListener)
    }
}
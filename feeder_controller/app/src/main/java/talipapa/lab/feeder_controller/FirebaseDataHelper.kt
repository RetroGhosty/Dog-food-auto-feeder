package talipapa.lab.feeder_controller

import Models.Schedule
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
// this should be the format that this class needs "/baseLink${whatever the function assign will be here}"
class FirebaseDataHelper(private var dbUrl : String?) : ViewModel(){

    private val TAG : String = "FirebaseDataHelper_Class"

    public suspend fun changeSchedArrVar(index: Number, newValue: String) : Boolean {
        val database: DatabaseReference = Firebase.database.reference
        val updateMap = hashMapOf<String, Any>(
            "${dbUrl}/${index.toString()}" to newValue
        )
        database.updateChildren(updateMap).addOnSuccessListener {
            Log.d(TAG, "Successfully changed")
        }.addOnFailureListener{ e ->
            Log.d(TAG, "ERROR: $e")

        }
        return true
    }
    public fun addSched(value: String) : Boolean {
        val databaseRef : DatabaseReference = FirebaseDatabase.getInstance().getReference().child("feedingSchedules")
        val newData = databaseRef.push()
        newData.setValue(value)

        return true
    }

    public fun deleteSchedArrVar(index: Schedule) : Boolean {
        val databaseRef : DatabaseReference = FirebaseDatabase.getInstance().getReference().child("feedingSchedules").child(index.key.toString())
        val firebaseDelResponse = databaseRef.removeValue();
        return true
    }


    public fun changeServoSignal(booleanSignal : Boolean) : Boolean {

        val database: DatabaseReference = Firebase.database.reference
        val updateMap = hashMapOf<String, Any>(
            dbUrl!! to booleanSignal
        )
        database.updateChildren(updateMap)
        return true
    }
}
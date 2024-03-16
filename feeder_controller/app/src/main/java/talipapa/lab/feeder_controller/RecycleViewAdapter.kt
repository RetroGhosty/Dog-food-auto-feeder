package talipapa.lab.feeder_controller

import Models.Schedule
import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.DelicateCoroutinesApi

class RecycleViewAdapter (private val schedList : ArrayList<Schedule>) : RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecycleViewAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(itemView)
    }

    private val firebaseSchedHelper = FirebaseDataHelper("/feedingSchedules")


    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: RecycleViewAdapter.MyViewHolder, position: Int) {
        val currentTime = schedList[position]
        holder.schedDate.text = currentTime.dateStr

        holder.schedButton.setOnClickListener(){
            firebaseSchedHelper.deleteSchedArrVar(schedList[position])
            deleteItem(position)
        }
    }

    override fun getItemCount(): Int {
        return schedList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val schedDate : TextView = itemView.findViewById(R.id.textView)
        val schedButton : Button = itemView.findViewById(R.id.button2)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteItem(index: Int){
        schedList.removeAt(index)
        notifyDataSetChanged()
    }

    public fun getSchedList() : List<Schedule>{
        return schedList
    }
}
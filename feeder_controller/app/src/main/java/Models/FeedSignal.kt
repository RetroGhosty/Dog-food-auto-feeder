package Models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

// [START post_class]

data class FeedSignal(
    var feedSignal: Boolean? = null,
    var feedingSchedules: Map<String, String>? = null
) {



    fun toMap(): Map<String, Any?>{
        return mapOf(
            "feedSignal" to feedSignal,
            "feedingSchedules" to feedingSchedules
        )
    }


}
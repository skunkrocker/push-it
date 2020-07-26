package machinehead.model

import com.google.gson.annotations.SerializedName
import machinehead.servers.Stage

class Payload(
    var stage: Stage,
    var tokens: List<String>,
    var headers: Map<String, Any>,
    var notification: Notification?,
    var custom: HashMap<String, Any>
)

data class Notification(var aps: Aps? = null)

data class Aps(
    var alert: Alert? = null,
    var sound: String? = null,
    var badge: Int? = null,
    var category: String? = null,
    @SerializedName("thread-id") var thread_id: String? = null,
    @SerializedName("mutable-content") var mutable_content: Int? = null,
    @SerializedName("content-available") var content_available: Int? = null,
    @SerializedName("target-content-id") var target_content_id: String? = null
)

data class Alert(
    var body: String? = null,
    var title: String? = null,
    var subtitle: String? = null,
    @SerializedName("loc-key") var loc_key: String? = null,
    @SerializedName("launch-image") var launch_image: String? = null,
    @SerializedName("title-loc-key") var title_loc_key: String? = null,
    @SerializedName("subtitle-loc-key") var subtitle_loc_key: String? = null
)

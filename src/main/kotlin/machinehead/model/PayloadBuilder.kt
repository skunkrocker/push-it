package machinehead.model

import machinehead.servers.Stage

fun payload(block: PayloadBuilder.() -> Unit): Payload = PayloadBuilder().apply(block).build()

class PayloadBuilder {
    var notification: Notification? = null
    var custom = hashMapOf<String, Any>()
    var tokens = listOf<String>()
    var stage = Stage.DEVELOPMENT
    var headers = hashMapOf<String, Any>()

    fun notification(block: NotificationBuilder.() -> Unit) {
        notification = NotificationBuilder().apply(block).build()
    }

    fun build(): Payload = Payload(stage, tokens, headers, notification, custom)
}

class NotificationBuilder {
    var aps: Aps? = null

    fun aps(block: ApsBuilder.() -> Unit) {
        aps = ApsBuilder().apply(block).build()
    }

    fun build(): Notification = Notification(aps)
}

class ApsBuilder {

    var alert: Alert? = null
    var sound: String? = null
    var badge: Int? = null
    var category: String? = null
    var thread_id: String? = null
    var mutable_content: Int? = null
    var content_available: Int? = null
    var target_content_id: String? = null

    fun alert(block: AlertBuilder.() -> Unit) {
        alert = AlertBuilder().apply(block).build()
    }

    fun build(): Aps =
        Aps(alert, sound, badge, category, thread_id, mutable_content, content_available, target_content_id)
}

class AlertBuilder {
    var body: String? = null
    var title: String? = null
    var subtitle: String? = null
    var loc_key: String? = null
    var launch_image: String? = null
    var title_loc_key: String? = null
    var subtitle_loc_key: String? = null

    fun build(): Alert = Alert(body, title, subtitle, loc_key, launch_image, title_loc_key, subtitle_loc_key)
}


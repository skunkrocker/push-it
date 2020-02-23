package machinehead.model

import com.google.gson.GsonBuilder

data class Notification(var aps: Aps? = null)

fun Notification.toJson(): String {
    var gson = GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(this)
}

data class Aps(var alert: Alert? = null, var sound: String? = null)

data class Alert(var body: String? = null, var title: String? = null)

fun notification(block: Notification.() -> Unit): Notification = Notification().apply(block)

fun Notification.aps(block: Aps.() -> Unit) {
    aps = Aps().apply(block)
}

fun Aps.alert(block: Alert.() -> Unit) {
    alert = Alert().apply(block)
}


fun main() {
    val notification = notification {
        aps {
            alert {
                title = "Is it going to work"
                body = "Hello world"
            }
            sound = "damn.wav"
        }
    }

    print(notification.toJson())
}
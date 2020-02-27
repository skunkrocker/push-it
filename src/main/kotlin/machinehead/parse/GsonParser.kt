package machinehead.parse

import com.google.gson.GsonBuilder
import machinehead.model.Payload

data class ParseErrors(val noTokens: String, val noAlert: String)

infix fun Payload.notificationAsString(onParsed: (notification: String) -> Unit) {
    onParsed(notificationAsString())
}

fun Payload.notificationAsString(): String {
    val gson = gson()
    if (this.custom.isNotEmpty()) {
        val notification = gson?.toJsonTree(this.notification)
        for (custom in this.custom) {
            val toJsonTree = gson.toJsonTree(custom.value)
            notification?.asJsonObject?.add(custom.key, toJsonTree)
        }
        return gson?.toJson(notification).toString()
    }
    return gson?.toJson(this.notification).toString()
}

private fun gson() =
    GsonBuilder()
        .setPrettyPrinting()
        .create()

package machinehead.extensions

import machinehead.PushIt
import machinehead.model.Payload
import machinehead.model.ResponsesAndErrors
import machinehead.parse.gson
import mu.KotlinLogging

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

infix fun Payload.push(report: (ResponsesAndErrors) -> Unit) {
    val logger = KotlinLogging.logger {}

    val pushIt = PushIt()
    logger.info { "will begin to prepare push" }

    pushIt.with(this)

    val responsesAndErrors = ResponsesAndErrors(
        pushIt.clientErrorListener.clientErrors,
        pushIt.requestErrorListener.requestErrors,
        pushIt.platformResponseListener.platformResponses
    )
    logger.debug { "report responses and errors $responsesAndErrors" }
    report(responsesAndErrors)
}

package machinehead

import machinehead.model.Payload
import machinehead.model.payload
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile
import machinehead.parse.ParseErrors

class InvalidNotification(message: String) : Exception(message)
class TokensMissingException(message: String) : Exception(message)

data class Response(val status: String, val message: String)
data class PushResult(val responses: Map<String, Response>?, val internalError: Exception?)

val Payload.errors: ParseErrors
    get() = From the YAMLFile("payload-errors.yml", ParseErrors::class)

infix fun Payload.push(result: (PushResult) -> Unit) {
    result(
        validate {
            val messageForTokens = mutableMapOf<String, Response>()
            this.tokens.forEach {
                messageForTokens[it] = Response("200", "Message")
            }
            return@validate PushResult(messageForTokens, null)
        }
    )
}

private fun Payload.validate(isValid: () -> PushResult): PushResult {
    if (this.tokens.isEmpty()) {
        return PushResult(null, TokensMissingException(errors.noTokens))
    }
    if (this.notification?.aps?.alert == null) {
        return PushResult(null, InvalidNotification(errors.noAlert))
    }
    return isValid()
}

fun main() {
    payload {
        notification {
            aps {
                alert {
                    body = "Hello"
                    subtitle = "Subtitle"
                }
            }
        }
        custom = hashMapOf(
            "custom-property" to "hello custom",
            "blow-up" to true
        )
        tokens = arrayListOf("asdfsd", "sadfsdf")
    } push {
        println(it)
    }
}
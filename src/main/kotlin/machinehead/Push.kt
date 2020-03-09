package machinehead

import machinehead.model.Payload
import machinehead.model.payload
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile
import machinehead.parse.ParseErrors
import machinehead.result.InvalidNotification
import machinehead.result.PushResult
import machinehead.result.Response
import machinehead.result.TokensMissingException

infix fun Payload.push(result: (PushResult) -> Unit) {
    PushIt.with(this) {
        result(it)
    }
}

sealed class PushIt {
    companion object {
        private val errors: ParseErrors
            get() = From the YAMLFile("payload-errors.yml", ParseErrors::class)

        fun with(payload: Payload, result: (PushResult) -> Unit) {
            result(
                validate(payload) {
                    val messageForTokens = mutableMapOf<String, Response>()
                    payload.tokens.forEach {
                        messageForTokens[it] = Response("200", "Message")
                    }
                    return@validate PushResult(messageForTokens, null)
                }
            )
        }

        private fun validate(payload: Payload, isValid: () -> PushResult): PushResult {
            if (payload.tokens.isEmpty()) {
                return PushResult(
                    null,
                    TokensMissingException(errors.noTokens)
                )
            }
            if (payload.notification?.aps?.alert == null) {
                return PushResult(
                    null,
                    InvalidNotification(errors.noAlert)
                )
            }
            return isValid()
        }
    }
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
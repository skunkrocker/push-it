package machinehead

import machinehead.credentials.CredentialsManager
import machinehead.credentials.P12CredentialsFromEnv
import machinehead.model.Payload
import machinehead.model.payload
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile
import machinehead.parse.ParseErrors
import machinehead.result.*
import machinehead.servers.Platform
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

infix fun Payload.push(result: (PushResult) -> Unit) {
    PushIt.with(this) {
        result(it)
    }
}

sealed class PushIt {
    companion object {

        private var credentials: CredentialsManager = P12CredentialsFromEnv()

        private val errors: ParseErrors
            get() = From the YAMLFile("payload-errors.yml", ParseErrors::class)

        var credentialsManager: CredentialsManager
            get() = this.credentials
            set(value) {
                this.credentials = value
            }

        fun with(payload: Payload, result: (PushResult) -> Unit) {
            result(
                validate(payload) {
                    return@validate credentials.credentials({ _: SSLSocketFactory, _: X509TrustManager ->
                        val messageForTokens = mutableMapOf<String, Response>()
                        payload.tokens.forEach {
                            messageForTokens[it] = Response("200", "Message")
                        }
                        return@credentials PushResult(messageForTokens, null)
                    }, {
                        return@credentials PushResult(null, errors.credentialsError?.let { CredentialsException(it) })
                    })
                }
            )
        }

        private fun validate(payload: Payload, isValid: () -> PushResult): PushResult {
            if (payload.tokens.isEmpty()) {
                return PushResult(
                    null,
                    errors.noTokens?.let { TokensMissingException(it) }
                )
            }
            if (payload.notification?.aps?.alert == null) {
                return PushResult(
                    null,
                    errors.noAlert?.let { InvalidNotification(it) }
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
        platform = Platform.IOS
        tokens = arrayListOf("asdfsd", "sadfsdf")
    } push {
        println(it)
    }
}
package machinehead

import machinehead.credentials.CredentialsManager
import machinehead.credentials.P12CredentialsFromEnv
import machinehead.model.Payload
import machinehead.model.payload
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile
import machinehead.parse.ParseErrors
import machinehead.result.PushResult
import machinehead.result.Response
import machinehead.servers.Stage
import machinehead.servers.Stage.DEVELOPMENT
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
                    return@validate credentials.credentials(
                        pushWithCredentials(payload),
                        noCredentialsFound()
                    )
                }
            )
        }

        private fun pushWithCredentials(payload: Payload): (SSLSocketFactory, X509TrustManager) -> PushResult {
            return credentials@{ _: SSLSocketFactory, _: X509TrustManager ->
                val messageForTokens = mutableMapOf<String, Response?>()
                payload.tokens.forEach {
                    messageForTokens[it] = Response("200", "Message")
                }
                return@credentials PushResult(messageForTokens as HashMap<String, Response?>)
            }
        }

        private fun noCredentialsFound(): () -> PushResult {
            return credentials@{
                return@credentials PushResult(
                    hashMapOf("error" to errors.credentialsError?.let { Response("500", it) })
                )
            }
        }

        private fun validate(payload: Payload, isValid: () -> PushResult): PushResult {
            if (payload.tokens.isEmpty()) {
                return PushResult(
                    hashMapOf("error" to errors.noTokens?.let { Response("500", it) })
                )
            }
            if (payload.notification?.aps?.alert == null) {
                return PushResult(
                    hashMapOf("error" to errors.noAlert?.let { Response("500", it) })
                )
            }
            if (payload.headers.isEmpty()) {
                return PushResult(
                    hashMapOf("error" to errors.noTopic?.let { Response("500", it) })
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
        headers = hashMapOf(
            "apns-topic" to "ch.sbb.ios.pushnext"
        )
        custom = hashMapOf(
            "custom-property" to "hello custom",
            "blow-up" to true
        )
        stage = DEVELOPMENT
        tokens = arrayListOf("3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55")
    } push {
        println(it)
    }
}
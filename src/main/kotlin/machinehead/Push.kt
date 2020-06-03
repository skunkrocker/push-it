package machinehead

import arrow.core.*
import machinehead.credentials.CredentialsManager
import machinehead.credentials.P12CredentialsFromEnv
import machinehead.model.Payload
import machinehead.model.payload
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile
import machinehead.okclient.OkClientWithCredentials.Companion.createOkClient
import machinehead.parse.ParseErrors
import machinehead.servers.Stage.DEVELOPMENT
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class ClientError(val message: String)
class APNSResponse(val status: String, val message: String)

class ErrorListener {
    private val errors = ArrayList<ClientError>()
    infix fun report(error: ClientError) {
        errors.add(error)
    }

    val hasErrors: Boolean get() = errors.isNotEmpty()
    val occurredErrors: List<ClientError> get() = errors
}

infix fun Payload.push(errorsAndResults: (Either<ClientError, APNSResponse>) -> Unit) {
    val pushIt = PushIt()
    pushIt.with(this)

    if (pushIt.errorListener.hasErrors) {
        errorsAndResults(Either.Left(pushIt.errorListener.occurredErrors.first()))
    }
}

class PushIt {

    private var credentials: CredentialsManager = P12CredentialsFromEnv()
    private var theErrorListener = ErrorListener()


    private val errorMessages: ParseErrors
        get() = From the YAMLFile("payload-errors.yml", ParseErrors::class)

    var credentialsManager: CredentialsManager?
        get() = this.credentials
        set(value) {
            if (value != null) {
                this.credentials = value
            }
        }

    var errorListener: ErrorListener
        get() = this.theErrorListener
        set(value) {
            this.theErrorListener = value
        }

    infix fun with(payload: Payload) {
        validate(payload)
            .fold(
                push(payload),
                reportError()
            )
    }

    private fun validate(payload: Payload): Option<ClientError> {
        if (payload.tokens.isEmpty()) {
            return Some(ClientError(errorMessages.noTokens.orEmpty()))
        }
        if (payload.notification?.aps?.alert == null) {
            return Some(ClientError(errorMessages.noAlert.orEmpty()))
        }

        if (payload.headers.isEmpty()) {
            return Some(ClientError(errorMessages.noTopic.orEmpty()))
        }
        return None
    }

    private fun push(payload: Payload): () -> Unit {
        return {
            credentialsManager
                .toOption()
                .fold(
                    reportCredentialsManagerError(),
                    createCredentialsAndPush(payload)
                )
        }
    }

    private fun createCredentialsAndPush(payload: Payload): (CredentialsManager) -> Unit {
        return {
            it.credentials()
                .fold(
                    reportError(),
                    createOkClientAndPush(payload)
                )
        }
    }

    private fun createOkClientAndPush(payload: Payload): (Pair<SSLSocketFactory, X509TrustManager>) -> Unit {
        return { socketFactoryAndTrustManager ->
            createOkClient(payload, socketFactoryAndTrustManager)
                .fold(
                    reportError(),
                    { okClient ->

                    })
        }
    }

    private fun reportError(): (ClientError) -> Unit {
        return {
            this.theErrorListener report it
        }
    }

    private fun reportCredentialsManagerError(): () -> Unit {
        return {
            errorListener report ClientError("no credential manager instantiated")
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
    } push { resultEither ->
        resultEither
            .fold({
                println(it.message)
            }, {
                println(it.status)
                println(it.message)
            })
    }
}

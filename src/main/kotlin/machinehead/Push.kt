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

/*
infix fun Payload.push(result: (PushResult) -> Unit) {
    PushIt.with(this) {
        result(it)
    }
}
*/

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
    val errorListener = ErrorListener()
    pushIt.with(this, errorListener)

    if (errorListener.hasErrors) {
        errorsAndResults(Either.Left(errorListener.occurredErrors.first()))
    }
}

class PushIt {

    private var credentials: CredentialsManager = P12CredentialsFromEnv()

    private val errors: ParseErrors
        get() = From the YAMLFile("payload-errors.yml", ParseErrors::class)

    var credentialsManager: CredentialsManager
        get() = this.credentials
        set(value) {
            this.credentials = value
        }

    fun with(payload: Payload, errorListener: ErrorListener) {
        validate(payload)
            .fold({
                forValidPayload(payload, errorListener)
            }, {
                errorListener report it
            })
    }

    private fun forValidPayload(payload: Payload, errorListener: ErrorListener) {
        credentials
            .credentials()
            .fold(
                {
                    errorListener report it
                }, { factoryManager ->
                    createOkClient(factoryManager)
                        .fold({
                            errorListener report it
                        }, { okClient ->

                        })
                })
    }

    private fun validate(payload: Payload): Option<ClientError> {
        if (payload.tokens.isEmpty()) {
            return Some(ClientError(errors.noTokens.orEmpty()))
        }
        if (payload.notification?.aps?.alert == null) {
            return Some(ClientError(errors.noAlert.orEmpty()))
        }

        if (payload.headers.isEmpty()) {
            return Some(ClientError(errors.noTopic.orEmpty()))
        }
        return None
    }
    /*
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
 */
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
    } push { either ->
        if (either.isLeft()) {
            either.mapLeft { clientError -> println(clientError.message) }
            return@push
        }
        if (either.isRight()) {
            either.rightIfNotNull {
                either.map {
                    println(it.status)
                    println(it.message)
                }
            }
        }
    }
}

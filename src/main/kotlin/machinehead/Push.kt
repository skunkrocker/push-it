package machinehead

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption
import kotlinx.coroutines.*
import machinehead.credentials.CredentialsManager
import machinehead.credentials.P12CredentialsFromEnv
import machinehead.model.Payload
import machinehead.model.payload
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile
import machinehead.okclient.OkClientAPNSRequest.Companion.createAPNSRequest
import machinehead.okclient.OkClientWithCredentials.Companion.createOkClient
import machinehead.okclient.RequestData
import machinehead.parse.ParseErrors
import machinehead.parse.notificationAsString
import machinehead.result.PlatformResponse
import machinehead.servers.Stage.DEVELOPMENT
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.io.IOException
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class ClientError(val message: String)
class CreateRequestError(val token: String, val message: String)

class ErrorListener {
    private val clientErrors = mutableListOf<ClientError>()
    infix fun report(error: ClientError) {
        clientErrors.add(error)
    }

    val hasErrors: Boolean get() = clientErrors.isNotEmpty()
    val clientErrorsOccurred: List<ClientError> get() = clientErrors
}

class ResponseListener {
    private val responsesList = mutableListOf<PlatformResponse>()

    fun report(result: PlatformResponse) {
        this.responsesList.add(result)
    }

    val responses get() = responsesList
}

infix fun Payload.push(errorsAndResults: (Pair<List<ClientError>, List<PlatformResponse>>) -> Unit) {
    val pushIt = PushIt()
    pushIt.with(this)
    errorsAndResults(Pair(pushIt.errorListener.clientErrorsOccurred, pushIt.responseListener.responses))
}

class PushIt {
    private var credentialsManager: CredentialsManager = P12CredentialsFromEnv()
    var errorListener = ErrorListener()
    var responseListener = ResponseListener()


    private val errorMessages: ParseErrors
        get() = From the YAMLFile("payload-errors.yml", ParseErrors::class)

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
                    pushBlockingWithOkClient(payload)
                )
        }
    }

    private fun pushBlockingWithOkClient(payload: Payload): (OkHttpClient) -> Unit {
        return { okClient ->
            val stage = payload.stage
            val applePayload = payload.notificationAsString()

            runBlocking {
                withContext(Dispatchers.IO) {
                    payload.tokens.forEach { token ->
                        createAPNSRequest(RequestData(applePayload, token, stage))
                            .fold(
                                reportRequestCreationError(),
                                pushAndEvalResponse(okClient)
                            )
                    }
                }
            }

        }
    }

    private fun reportRequestCreationError(): (CreateRequestError) -> Unit {
        return {
            //TODO(Implement create request error)
        }
    }

    private fun CoroutineScope.pushAndEvalResponse(okClient: OkHttpClient): (Request) -> Unit {
        return { request ->
            launch {
                var response: Response? = null
                try {
                    response = okClient.newCall(request).execute()
                    println(response)
                    response
                        .toOption()
                        .map {
                            val theResponse = when (it.isSuccessful) {
                                true -> PlatformResponse(it.code.toString(), it.message)
                                false -> PlatformResponse(
                                    it.code.toString(),
                                    it.body?.string().orEmpty()
                                )
                            }
                            responseListener.report(theResponse)
                        }
                } catch (e: IOException) {
                    println("could not execute request for request: $request")
                    errorListener.report(ClientError("error occurred when executing request: ${e.message}"))
                } finally {
                    response.let {
                        it?.closeQuietly()
                        it?.body?.closeQuietly()
                    }
                }
            }
        }
    }

    private fun reportError(): (ClientError) -> Unit {
        return {
            this.errorListener report it
        }
    }

    private fun reportCredentialsManagerError(): () -> Unit {
        return {
            errorListener report ClientError(errorMessages.noCredentialsManager.orEmpty())
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
        tokens = arrayListOf(
            "3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55",
            "3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a54"
        )
    } push { errorsAndResults ->
        println("the errors: ${errorsAndResults.first}")
        println("the results: ${errorsAndResults.second}")
    }

    println("#################################################")
    println("#                                               #")
    println("#        next comes the second call             #")
    println("#                                               #")
    println("#################################################")


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
        tokens = arrayListOf(
            "3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a54",
            "3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55"
        )
    } push { errorsAndResults ->
        println("the second errors: ${errorsAndResults.first}")
        println("the second results: ${errorsAndResults.second}")
    }
}

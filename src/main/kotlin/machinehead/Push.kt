package machinehead

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption
import machinehead.credentials.CredentialsManager
import machinehead.credentials.P12CredentialsFromEnv
import machinehead.model.*
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile
import machinehead.okclient.OkClientAPNSRequest.Companion.createAPNSRequest
import machinehead.okclient.OkClientWithCredentials.Companion.createOkClient
import machinehead.okclient.PlatformCallback
import machinehead.okclient.RequestData
import machinehead.parse.ParseErrors
import machinehead.parse.notificationAsString
import machinehead.servers.Stage.DEVELOPMENT
import mu.KotlinLogging
import okhttp3.OkHttpClient
import java.util.concurrent.CountDownLatch
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

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

class PushIt {

    private val logger = KotlinLogging.logger {}

    private var credentialsManager = P12CredentialsFromEnv()

    var clientErrorListener = ClientErrorListener()
    var requestErrorListener = RequestErrorListener()
    var platformResponseListener = PlatformResponseListener()


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
            logger.debug { "will create ok client with socket factory and trust manager" }
            createOkClient(payload, socketFactoryAndTrustManager)
                .fold(
                    reportError(),
                    pushAsyncAndWaitToFinish(payload)
                )
        }
    }

    private fun pushAsyncAndWaitToFinish(payload: Payload): (OkHttpClient) -> Unit {
        return { okClient ->
            val stage = payload.stage
            val applePayload = payload.notificationAsString()

            logger.debug { "the string representation of the payload will be push: $applePayload" }

            val countDownLatch = CountDownLatch(payload.tokens.size)
            logger.debug { "the payload will be pushed to total of ${payload.tokens.size} clients" }
            val callBacks = mutableListOf<PlatformCallback>()

            payload.tokens.forEach { token ->
                createAPNSRequest(RequestData(applePayload, token, stage))
                    .fold({
                        logger.error { "could not create request for the token: $token with the error: ${it.message}" }
                        requestErrorListener.report(Some(it))
                        countDownLatch.countDown()
                    },
                        { request ->
                            logger.info { "will push the payload: $applePayload to device with token: $token" }
                            val responseCallback = PlatformCallback(token, countDownLatch)
                            okClient.newCall(request).enqueue(responseCallback)
                            callBacks.add(responseCallback)
                        })
            }
            logger.debug { "will wait for all platform callbacks to report being done" }
            countDownLatch.await()
            logger.debug { "waiting for all platform callbacks is done" }

            callBacks.forEach { callBack ->
                platformResponseListener.report(callBack.response)
                requestErrorListener.report(callBack.requestError)
            }
        }
    }

    private fun reportError(): (ClientError) -> Unit {
        return {
            logger.error { it.message }
            this.clientErrorListener report it
        }
    }

    private fun reportCredentialsManagerError(): () -> Unit {
        return {
            logger.error { "the default credential manager was replaced with null" }
            clientErrorListener report ClientError(errorMessages.noCredentialsManager.orEmpty())
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
    } push { errorAndResponses ->
        println("the errors: ${errorAndResponses.clientErrors}")
        println("the request errors: ${errorAndResponses.requestErrors}")
        println("the platform responses: ${errorAndResponses.responses}")
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
    } push { errorAndResponses ->
        println("the second errors: ${errorAndResponses.clientErrors}")
        println("the second request errors: ${errorAndResponses.requestErrors}")
        println("the second platform responses: ${errorAndResponses.responses}")
    }
}

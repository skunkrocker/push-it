package machinehead

import arrow.core.Some
import arrow.core.toOption
import machinehead.credentials.CredentialsManager
import machinehead.credentials.P12CredentialsFromEnv
import machinehead.extensions.notificationAsString
import machinehead.extensions.pushk
import machinehead.extensions.reportCredentialsManagerError
import machinehead.extensions.reportError
import machinehead.model.*
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile
import machinehead.okclient.OkClientAPNSRequest.Companion.createAPNSRequest
import machinehead.okclient.OkClientAPNSRequest.Companion.createURLAndRequestBody
import machinehead.okclient.OkClientWithCredentials.Companion.createOkClient
import machinehead.okclient.OkClientWithCredentials.Companion.releaseResources
import machinehead.okclient.PayloadValidator.Companion.validate
import machinehead.okclient.PlatformCallback
import machinehead.parse.ParseErrors
import machinehead.servers.Stage
import mu.KotlinLogging
import okhttp3.OkHttpClient
import java.util.concurrent.CountDownLatch
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

class PushIt {

    val logger = KotlinLogging.logger {}

    var credentialsManager = P12CredentialsFromEnv()

    var clientErrorListener = ClientErrorListener()
    var requestErrorListener = RequestErrorListener()
    var platformResponseListener = PlatformResponseListener()

    val errorMessages: ParseErrors
        get() = From the YAMLFile("payload-errors.yml", ParseErrors::class)

    infix fun with(payload: Payload) {
        validate(payload)
            .fold(
                push(payload),
                reportError()
            )
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
            val applePayload = payload.notificationAsString()

            logger.debug { "the string representation of the payload will be push: $applePayload" }

            val countDownLatch = CountDownLatch(payload.tokens.size)
            logger.debug { "the payload will be pushed to total of ${payload.tokens.size} clients" }
            val callBacks = mutableListOf<PlatformCallback>()

            createURLAndRequestBody(payload) { baseUrl, body ->
                payload.tokens.forEach { token ->
                    createAPNSRequest(baseUrl, body, token)
                        .fold(
                            {
                                logger.error { "could not create request for the token: $token with the error: ${it.message}" }
                                requestErrorListener.report(Some(it))
                                countDownLatch.countDown()
                            },
                            { request ->
                                logger.info { "will push the payload: $applePayload to device with token: $token" }
                                val responseCallback = PlatformCallback(token, countDownLatch)
                                okClient.newCall(request).enqueue(responseCallback)
                                callBacks.add(responseCallback)
                            }
                        )
                }
            }

            logger.debug { "will wait for all platform callbacks to report being done" }
            countDownLatch.await()
            logger.debug { "waiting for all platform callbacks is done" }

            releaseResources(okClient)

            callBacks.forEach { callBack ->
                platformResponseListener.report(callBack.response)
                requestErrorListener.report(callBack.requestError)
            }
        }
    }
}
fun main() {
    val logger = KotlinLogging.logger { }
    /*
    val theTokens = mutableListOf<String>()
    repeat(1000) {
        if (it % 2 == 0)
            theTokens.add("3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55")
        else
            theTokens.add("3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a54")
    }
    */
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
            "apns-topic" to "org.your.app.bundle.id"
        )
        custom = hashMapOf(
            "custom-property" to "hello custom",
            "blow-up" to true
        )
        stage = Stage.DEVELOPMENT
        tokens = listOf("3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a54")

    }.pushk { errorAndResponses ->
        println("what ever")
    }
}

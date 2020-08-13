package machinehead.app

import arrow.core.Either
import arrow.core.Some
import machinehead.extensions.notificationAsString
import machinehead.model.*
import machinehead.okclient.OkClientService
import machinehead.okclient.PlatformCallback
import machinehead.okclient.RequestService
import machinehead.validation.ValidatePayloadService
import mu.KotlinLogging
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent
import org.koin.java.KoinJavaComponent.get
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CountDownLatch

class PushApp {

    private val okClientService by KoinJavaComponent.inject(OkClientService::class.java)
    private val validatePayloadService by KoinJavaComponent.inject(ValidatePayloadService::class.java)

    private val logger = KotlinLogging.logger { }

    fun with(payload: Payload, report: (Either<ClientError, RequestErrorsAndResults>) -> Unit) {
        validatePayloadService
            .isValid(payload)
            .fold(
                {
                    pushNotification(payload) { errorAndResults ->
                        errorAndResults.fold({
                            report(Either.left(it))
                        }, {
                            report(Either.right(it))
                        })
                    }
                }, {
                    report(Either.left(it))
                }
            )
    }

    private fun pushNotification(payload: Payload, report: (Either<ClientError, RequestErrorsAndResults>) -> Unit) {
        try {
            val okClient = okClientService.getHttpClient()

            val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
            val body: RequestBody = payload
                .notificationAsString()
                .toRequestBody(mediaType)
            logger.info { "body created" }

            val countDownLatch = CountDownLatch(payload.tokens.size)
            val callBacks = mutableListOf<PlatformCallback>()

            val start = Instant.now()

            payload.tokens.forEach { token ->
                get(RequestService::class.java) {
                    parametersOf(
                        token,
                        body,
                        payload.stage
                    )
                }
                    .get(
                        {
                            val responseCallback = PlatformCallback(token, countDownLatch)
                            responseCallback.requestError = Some(it)
                            callBacks.add(responseCallback)
                        }, { request ->
                            val responseCallback = PlatformCallback(token, countDownLatch)
                            okClient.newCall(request).enqueue(responseCallback)
                            callBacks.add(responseCallback)
                        }
                    )
            }

            logger.debug { "will wait for all platform callbacks to report being done" }
            countDownLatch.await()

            val end = Instant.now()
            logger.info { "it took  ${Duration.between(start, end).toSeconds()} s to push all the messages " }

            okClientService.releaseResources()

            report(Either.right(getRequestErrorsAndResults(callBacks)))

        } catch (e: Exception) {
            logger.error { "could not push messages $e" }
            report(Either.left(ClientError("exception happened: $e")))
        }
    }

    private fun getRequestErrorsAndResults(callBacks: MutableList<PlatformCallback>): RequestErrorsAndResults {
        val pushResults = mutableListOf<PushResult>()
        val requestErrors = mutableListOf<RequestError>()

        callBacks.map { callBack ->
            callBack.requestError.map { error ->
                requestErrors.add(error)
            }
            callBack.response.map { result ->
                pushResults.add(result)
            }
        }
        return RequestErrorsAndResults(requestErrors, pushResults)
    }
}

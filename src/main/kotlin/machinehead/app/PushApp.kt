package machinehead.app

import arrow.core.Either
import kotlinx.coroutines.*
import machinehead.exceptions.ClientCreationException
import machinehead.extensions.notificationAsString
import machinehead.model.*
import machinehead.okclient.PushNotification
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

class PushApp {
    private val validatePayloadService by KoinJavaComponent.inject(ValidatePayloadService::class.java)

    private val logger = KotlinLogging.logger { }

    fun with(payload: Payload, report: (Either<ClientError, RequestErrorsAndResults>) -> Unit) {
        validatePayloadService
            .isValid(payload)
            .fold(
                {
                    performPush(payload) { errorAndResults ->
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

    private fun performPush(payload: Payload, report: (Either<ClientError, RequestErrorsAndResults>) -> Unit) =
        runBlocking {
            try {
                val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
                val body: RequestBody = payload
                    .notificationAsString()
                    .toRequestBody(mediaType)
                logger.info { "body created" }
                val notifications = payload.tokens
                    .map { token ->
                        get(PushNotification::class.java) {
                            parametersOf(
                                token,
                                payload.stage,
                                body
                            )
                        }
                    }
                logger.info { "the notification instances were created: ${notifications.size}" }
                coroutineScope {
                    logger.info { "now pushing and waiting for results" }

                    val start = Instant.now()
                    val results = notifications.map { notification ->
                        async {
                            notification.push(payload)
                        }
                    }.awaitAll()

                    val end = Instant.now()

                    logger.info { "waited for all in ${Duration.between(start, end).toSeconds()} s " }

                    //val results = differedNotifications.awaitAll()//deferredList.awaitAll()
                    logger.info { "all pushes are finished, results number: ${results.size}" }
                    report(Either.right(requestErrorsAndResponses(results)))
                }
            } catch (e: Throwable) {
                logger.error { "aborting push notification. exception was: $e" }
                reportError(e, report)
            }
        }

    private fun requestErrorsAndResponses(results: List<Either<RequestError, PushResult>>): RequestErrorsAndResults {
        val pushResults = mutableListOf<PushResult>()
        val requestErrors = mutableListOf<RequestError>()

        results
            .parallelStream()
            .forEach {
                it.fold({ error ->
                    requestErrors.add(error)
                }, { result ->
                    pushResults.add(result)
                })
            }
        return RequestErrorsAndResults(requestErrors, pushResults)
    }

    private fun reportError(error: Throwable, report: (Either<ClientError, RequestErrorsAndResults>) -> Unit) =
        when (error) {
            is ClientCreationException -> {
                report(Either.left(error.clientError))
            }
            else -> {
                report(Either.left(ClientError("unknown error happened. see the logs")))
            }
        }
}

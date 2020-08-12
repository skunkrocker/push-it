package machinehead.app

import arrow.core.Either
import kotlinx.coroutines.*
import machinehead.exceptions.ClientCreationException
import machinehead.extensions.notificationAsString
import machinehead.model.*
import machinehead.okclient.PushNotification
import machinehead.validation.ValidatePayloadService
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent

class PushApp {
    private val validatePayloadService by KoinJavaComponent.inject(ValidatePayloadService::class.java)

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
                val deferredList = mutableListOf<Deferred<Either<RequestError, PushResult>>>()

                val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
                val body: RequestBody = payload
                    .notificationAsString()
                    .toRequestBody(mediaType)

                coroutineScope {
                    payload
                        .tokens
                        .parallelStream()
                        .forEach { token ->
                            deferredList.add(
                                async {
                                    val notification = KoinJavaComponent.get(PushNotification::class.java) {
                                        parametersOf(
                                            token,
                                            payload.stage,
                                            body
                                        )
                                    }
                                    notification.push(payload)
                                }
                            )
                        }

                    val results = deferredList.awaitAll()
                    report(Either.right(requestErrorsAndResponses(results)))
                }
            } catch (e: Throwable) {
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

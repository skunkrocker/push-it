package machinehead.okclient

import arrow.core.Either
import machinehead.extensions.isNotNullOrEmpty
import machinehead.model.*
import machinehead.servers.NotificationServers
import machinehead.servers.Stage
import mu.KotlinLogging
import okhttp3.Request
import okhttp3.RequestBody
import org.koin.java.KoinJavaComponent.inject

interface PushNotification {
    fun push(payload: Payload): Either<RequestError, PushResult>
}

class PushNotificationImpl(val token: String, val stage: Stage, val body: RequestBody) : PushNotification {

    private val okClientService by inject(OkClientService::class.java)

    val logger = KotlinLogging.logger() {}

    override fun push(payload: Payload): Either<RequestError, PushResult> {
        return okClientService.getHttpClient()
            .map { okHttpClient ->
                createRequest()
                    .fold({
                        return@map Either.left(it)
                    }, {

                        //okHttpClient.newCall(it).execute()

                        val pushResult = PushResult(token, PlatformResponse(200, APNSResponse("Success")))
                        return@map Either.right(pushResult)
                    })
            }.orNull()!!
    }

    private fun createRequest(): Either<RequestError, Request> {
        try {
            val url = getUrl() + "/$token"
            logger.debug { "final push url is: $url" }

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            return Either.right(request)
        } catch (e: Exception) {
            logger.error { "failed to create request for the token: $token" }
        }
        return Either.left(RequestError(token, "failed to create request for the token"))
    }

    private fun getUrl(): String {
        var url = NotificationServers.urlForStage(stage)

        if (System.getProperty(OkClientAPNSRequest.TEST_URL_PROPERTY).isNotNullOrEmpty()) {
            url = NotificationServers.urlForStage(Stage.TEST)
            logger.warn { "you overwrite the APNS  url to: $url " }
            logger.warn { "if you didn't do this for test purposes, please remove the property 'localhost.url' from your ENV" }
        }
        return url
    }
}
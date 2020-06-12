package machinehead.okclient

import arrow.core.Either
import machinehead.model.RequestError
import machinehead.servers.NotificationServers
import machinehead.servers.Stage
import mu.KotlinLogging
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class RequestData(val stringPayload: String, val token: String, val stage: Stage)

class OkClientAPNSRequest {
    companion object {
        private val logger = KotlinLogging.logger { }
        fun createAPNSRequest(requestData: RequestData): Either<RequestError, Request> {
            val result = Either.left(
                RequestError(
                    requestData.token,
                    "could not create request for device token: ${requestData.token}"
                )
            )
            var url = NotificationServers.forUrl(requestData.stage, requestData.token)

            System.getProperty("localhost.url").let {
                if (it != null) {
                    url = NotificationServers.forUrl(Stage.TEST, requestData.token)
                    logger.warn { "you overwrite the APNS  url to: $url " }
                    logger.warn { "if you didn't do this for test purposes, please remove the property 'localhost.url' from your ENV" }
                }
            }
            logger.debug { "the final request end point url: $url" }

            try {
                val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
                val body: RequestBody = requestData.stringPayload.toRequestBody(mediaType)

                logger.debug { "the request body was created for device token: ${requestData.token}" }

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                return Either.right(request)

            } catch (e: Exception) {
                logger.error { "could not create request for token: ${requestData.token}. error was: $e" }
            }
            return result
        }
    }
}
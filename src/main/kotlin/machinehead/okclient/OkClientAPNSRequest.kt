package machinehead.okclient

import arrow.core.Either
import machinehead.model.Payload
import machinehead.model.RequestError
import machinehead.parse.notificationAsString
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
        const val TEST_URL_PROPERTY = "localhost.url"

        private val logger = KotlinLogging.logger { }

        fun createURLAndRequestBody(payload: Payload, onCreated: (url: String, body: RequestBody) -> Unit) {
            var url = NotificationServers.baseUrlForStage(payload.stage)

            if (!System.getProperty(TEST_URL_PROPERTY).isNullOrEmpty()) {
                url = NotificationServers.baseUrlForStage(Stage.TEST)
                logger.warn { "you overwrite the APNS  url to: $url " }
                logger.warn { "if you didn't do this for test purposes, please remove the property 'localhost.url' from your ENV" }
            }

            logger.debug { "the final request end point url: $url" }

            val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
            val body: RequestBody = payload
                .notificationAsString()
                .toRequestBody(mediaType)

            onCreated(url, body)
        }

        fun createAPNSRequest(url: String, body: RequestBody, token: String): Either<RequestError, Request> {
            val finalUrl = url + token
            try {
                val request = Request.Builder()
                    .url(finalUrl)
                    .post(body)
                    .build()

                return Either.right(request)
            } catch (e: Exception) {
                logger.error { "could not request for token: $token" }
            }
            return Either.left(
                RequestError(
                    token,
                    "could not create request for device token: $token}"
                )
            )
        }
    }
}
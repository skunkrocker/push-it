package machinehead.okclient

import arrow.core.Either
import machinehead.CreateRequestError
import machinehead.servers.NotificationServers
import machinehead.servers.Stage
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class RequestData(val stringPayload: String, val token: String, val stage: Stage)

class OkClientAPNSRequest {
    companion object {
        fun createAPNSRequest(requestData: RequestData): Either<CreateRequestError, Request> {
            val result = Either.left(
                CreateRequestError(
                    requestData.token,
                    "could not create request for device token"
                )
            )

            val url = NotificationServers.forUrl(requestData.stage, requestData.token)

            try {
                val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
                val body: RequestBody = requestData.stringPayload.toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                return Either.right(request)

            } catch (e: Exception) {
                println(e)
            }
            return result
        }
    }
}
package machinehead.okclient

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import machinehead.model.APNSResponse
import machinehead.model.PlatformResponse
import machinehead.model.PushResult
import machinehead.model.RequestError
import machinehead.parse.gson
import mu.KotlinLogging
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch

class PlatformCallback(
    private val token: String,
    private val countDownLatch: CountDownLatch
) : Callback {
    private val logger = KotlinLogging.logger {}

    var response: Option<PushResult> = None
    var requestError: Option<RequestError> = None


    override fun onFailure(call: Call, e: IOException) {
        logger.error { e }
        requestError =
            Some(RequestError(token, message = "failed to execute request with exception ${e.message}"))
        countDownLatch.countDown()
        logger.debug { "count down latch called" }
    }

    override fun onResponse(call: Call, response: Response) {
        logger.debug { "response received for $token" }
        try {
            val pushResponse = getPlatformResponse(response)

            logger.debug { "the push response: $pushResponse for token: $token received" }
            this.response = Some(PushResult(token, pushResponse))

        } catch (e: IOException) {
            logger.error { "could not execute request for token $token . error was: $e" }
            this.response = None
        } finally {
            response.close()
        }
        countDownLatch.countDown()
        logger.debug { "count down latch called" }
    }

    private fun getPlatformResponse(response: Response) =
        PlatformResponse(response.code, getAPNSResponse(response))

    private fun getAPNSResponse(response: Response): APNSResponse {
        val body = response.body?.string().orEmpty()

        if (body.isNotEmpty()) {
            return gson().fromJson(body, APNSResponse::class.java)
        }
        return gson().fromJson("{\"reason\":\"Success\"}", APNSResponse::class.java)
    }
}

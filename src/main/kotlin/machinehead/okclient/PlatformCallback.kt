package machinehead.okclient

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import machinehead.model.PlatformResponse
import machinehead.model.RequestError
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch

class PlatformCallback(private val token: String, private val countDownLatch: CountDownLatch) : Callback {

    lateinit var platformResponse: Option<PlatformResponse>
    var requestError: Option<RequestError> = None

    override fun onFailure(call: Call, e: IOException) {
        println(e)

        requestError =
            Some(RequestError(token, message = "failed to execute request with exception ${e.message}"))

        countDownLatch.countDown()
    }

    override fun onResponse(call: Call, response: Response) {
        println(response)
        try {
            val theResponse = when (response.isSuccessful) {
                true -> PlatformResponse(response.code.toString(), response.message)
                false -> PlatformResponse(
                    response.code.toString(),
                    response.body?.string().orEmpty()
                )
            }
            println(theResponse)
            platformResponse = Some(theResponse)

        } catch (e: IOException) {
            println("could not execute request for request: $response")
            platformResponse = None
        } finally {
            response.close()
        }
        countDownLatch.countDown()
    }
}

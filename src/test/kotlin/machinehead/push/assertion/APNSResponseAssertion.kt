package machinehead.push.assertion

import machinehead.model.APNSResponse
import machinehead.model.PushResult
import machinehead.model.ResponsesLoader
import machinehead.parse.gson
import org.junit.jupiter.api.Assertions.assertEquals

open class APNSResponseAssertion {
    fun assertResponse(platformResponses: List<PushResult>, expectedResponse: String) {
        val size = platformResponses.size
        assertEquals(1, size)

        platformResponses.forEach {
            assertEquals(400, it.response.status)
            val apnsReason = it.response.apns.reason

            val expected = gson()
                .fromJson(
                    ResponsesLoader.getJson(expectedResponse), APNSResponse::class.java
                )

            assertEquals(expected.reason, apnsReason)
        }
    }
}
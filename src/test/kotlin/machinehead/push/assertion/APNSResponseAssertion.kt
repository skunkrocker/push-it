package machinehead.push.assertion

import com.google.gson.JsonParser
import machinehead.model.PushResult
import machinehead.model.ResponsesLoader
import org.junit.jupiter.api.Assertions

open class APNSResponseAssertion {
    fun assertResponse(platformResponses: List<PushResult>, expectedResponse: String) {
        val jsonParser = JsonParser()

        val size = platformResponses.size
        Assertions.assertEquals(1, size)

        platformResponses.forEach {
            Assertions.assertEquals(400, it.response.status.toInt())
            val apnsReason = jsonParser
                .parse(it.response.message)
                .asJsonObject.get("reason")
                .asString

            val expectedReason = jsonParser
                .parse(ResponsesLoader.getJson(expectedResponse))
                .asJsonObject.get("reason")
                .asString

            Assertions.assertEquals(expectedReason, apnsReason)
        }
    }
}
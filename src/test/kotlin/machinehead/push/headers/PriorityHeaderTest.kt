package machinehead.push.headers

import com.google.gson.JsonParser
import com.squareup.okhttp.mockwebserver.MockWebServer
import machinehead.PushIt
import machinehead.model.ResponsesLoader.Companion.getJson
import machinehead.okclient.OkClientAPNSRequest
import machinehead.push.APNSHeaders.Companion.APNS_PRIORITY
import machinehead.push.APNSResponseAssertion
import machinehead.push.MockAPNSResponses
import machinehead.push.TestData
import machinehead.push.TestData.Companion.APNS_TOPIC_KEY
import machinehead.push.TestData.Companion.APNS_TOPIC_VALUE
import machinehead.push.TestData.Companion.TOKEN
import machinehead.push.TestData.Companion.`get test payload`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.Assertions.assertEquals

@TestInstance(Lifecycle.PER_CLASS)
class PriorityHeaderTest : APNSResponseAssertion() {

    private lateinit var mockWebServer: MockWebServer

    @BeforeAll
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(MockAPNSResponses())
        mockWebServer.play()

        val url = mockWebServer.getUrl("")
        System.setProperty(OkClientAPNSRequest.TEST_URL_PROPERTY, url.toString())
    }

    @Test
    @ExperimentalStdlibApi
    fun `priority header has invalid value - report apns message`() {
        //given
        val payload = `get test payload`(
            TOKEN, hashMapOf(
                APNS_TOPIC_KEY to APNS_TOPIC_VALUE,
                APNS_PRIORITY to "11"
            )
        )
        //when
        val pushIt = PushIt()
        pushIt.with(payload)

        //then
        val jsonParser = JsonParser()
        mockWebServer.takeRequest()

        val platformResponses = pushIt.platformResponseListener.platformResponses

        assertResponse(platformResponses, "bad_priority.json")
    }

    @AfterAll
    fun tearDown() {
        mockWebServer.shutdown()
    }
}
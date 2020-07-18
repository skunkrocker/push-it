package machinehead.push.token

import com.squareup.okhttp.mockwebserver.MockWebServer
import machinehead.PushIt
import machinehead.okclient.OkClientAPNSRequest
import machinehead.push.APNSHeaders
import machinehead.push.APNSResponseAssertion
import machinehead.push.MockAPNSResponses
import machinehead.push.TestData
import machinehead.push.TestData.Companion.APNS_TOPIC_KEY
import machinehead.push.TestData.Companion.APNS_TOPIC_VALUE
import machinehead.push.TestData.Companion.BAD_DEVICE_TOKEN
import machinehead.push.TestData.Companion.`get test payload`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
class BadTokenTest : APNSResponseAssertion() {

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
    fun `the device token is bad - report apns message`() {
        //given
        val payload = `get test payload`(
            BAD_DEVICE_TOKEN, hashMapOf(APNS_TOPIC_KEY to APNS_TOPIC_VALUE)
        )
        //when
        val pushIt = PushIt()
        pushIt.with(payload)

        //then
        val platformResponses = pushIt.platformResponseListener.platformResponses

        assertResponse(platformResponses, "bad_device_token.json")
    }

    @AfterAll
    fun tearDown() {
        mockWebServer.shutdown()
    }
}
package machinehead.push.token

import com.squareup.okhttp.mockwebserver.MockWebServer
import machinehead.PushIt
import machinehead.okclient.OkClientAPNSRequest
import machinehead.push.APNSResponseAssertion
import machinehead.push.MockAPNSResponses
import machinehead.push.TestData
import machinehead.push.TestData.Companion.APNS_TOPIC_KEY
import machinehead.push.TestData.Companion.APNS_TOPIC_VALUE
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
class MissingTokenTest : APNSResponseAssertion() {

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
    fun `the device token is missing - report apns message`() {
        //given
        val payload = TestData.`get test payload`(
            "", hashMapOf(APNS_TOPIC_KEY to APNS_TOPIC_VALUE)
        )
        //when
        val pushIt = PushIt()
        pushIt.with(payload)

        //then
        val platformResponses = pushIt.platformResponseListener.platformResponses
        println(platformResponses)
        assertResponse(platformResponses, "missing_token.json")
    }

    @AfterAll
    fun tearDown() {
        mockWebServer.shutdown()
    }
}
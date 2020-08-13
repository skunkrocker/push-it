package machinehead.push.token

import com.squareup.okhttp.mockwebserver.MockWebServer
import machinehead.extensions.push
import machinehead.push.TestData
import machinehead.push.assertion.APNSResponseAssertion
import machinehead.push.TestData.Companion.APNS_TOPIC_KEY
import machinehead.push.TestData.Companion.APNS_TOPIC_VALUE
import machinehead.push.TestData.Companion.BAD_DEVICE_TOKEN
import machinehead.push.TestData.Companion.`get test payload`
import machinehead.push.responses.MockAPNSResponses
import org.junit.jupiter.api.*
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
        System.setProperty(TestData.TEST_URL_PROPERTY, url.toString())
    }

    @Test
    @ExperimentalStdlibApi
    fun `the device token is bad - report apns message`() {
        `get test payload`(
            BAD_DEVICE_TOKEN, hashMapOf(APNS_TOPIC_KEY to APNS_TOPIC_VALUE)
        )
            .push { either ->
                either
                    .fold(
                        {
                            Assertions.assertFalse(true)
                        },
                        {
                            assertResponse(it.results, "bad_device_token.json")
                        }
                    )
            }
    }

    @AfterAll
    fun tearDown() {
        mockWebServer.shutdown()
    }
}
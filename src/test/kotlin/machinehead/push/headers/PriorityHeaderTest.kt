package machinehead.push.headers

import com.squareup.okhttp.mockwebserver.MockWebServer
import machinehead.extensions.pushIt
import machinehead.push.TestData
import machinehead.push.responses.APNSHeaders.Companion.APNS_PRIORITY
import machinehead.push.assertion.APNSResponseAssertion
import machinehead.push.TestData.Companion.APNS_TOPIC_KEY
import machinehead.push.TestData.Companion.APNS_TOPIC_VALUE
import machinehead.push.TestData.Companion.TOKEN
import machinehead.push.TestData.Companion.`get test payload`
import machinehead.push.responses.APNSHeaderResponses
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
class PriorityHeaderTest : APNSResponseAssertion() {

    private lateinit var mockWebServer: MockWebServer

    @BeforeAll
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(APNSHeaderResponses())
        mockWebServer.play()

        val url = mockWebServer.getUrl("")
        System.setProperty(TestData.TEST_URL_PROPERTY, url.toString())
    }

    @Test
    @ExperimentalStdlibApi
    fun `priority header has invalid value - report apns message`() {
        `get test payload`(
            TOKEN, hashMapOf(
                APNS_TOPIC_KEY to APNS_TOPIC_VALUE,
                APNS_PRIORITY to "11"
            )
        )
            .pushIt { either ->
                mockWebServer.takeRequest()
                either.fold(
                    {
                        Assertions.assertFalse(true)
                    },
                    { errorsAndResults ->

                        val platformResponses = errorsAndResults.results
                        assertResponse(platformResponses, "bad_priority.json")
                    }
                )
            }
    }

    @AfterAll
    fun tearDown() {
        mockWebServer.shutdown()
    }
}
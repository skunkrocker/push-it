package machinehead.push

import com.google.gson.JsonParser
import com.squareup.okhttp.mockwebserver.MockWebServer
import machinehead.extensions.push
import machinehead.push.TestData.Companion.`get test payload`
import machinehead.push.responses.MockAPNSResponses
import machinehead.servers.NotificationServers
import mu.KotlinLogging
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
class PushPayloadWithCustomFieldsTest {

    private lateinit var mockWebServer: MockWebServer

    private val logger = KotlinLogging.logger { }

    @BeforeAll
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(MockAPNSResponses())
        mockWebServer.play()

        val url = mockWebServer.getUrl("")
        logger.warn { "during the test, the url will be used for MocWebServer to run properly: $url" }
        System.setProperty(TestData.TEST_URL_PROPERTY, url.toString())
    }

    @Test
    @ExperimentalStdlibApi
    fun `push notification for develop platform`() {
        `get test payload`(TestData.TOKEN, hashMapOf(TestData.APNS_TOPIC_KEY to TestData.APNS_TOPIC_VALUE))
            .push { either ->
                either.fold({
                    Assertions.assertFalse(true)
                }, {

                    val jsonParser = JsonParser()

                    val theRequest = mockWebServer.takeRequest()

                    val theResponseString = String(theRequest.body, Charsets.UTF_8)
                    val apnsTopicHeader = theRequest.getHeader(TestData.APNS_TOPIC_KEY)
                    val theBodySent = jsonParser.parse(theResponseString).asJsonObject
                    val alertDictionary = theBodySent.get("aps").asJsonObject.get("alert").asJsonObject

                    logger.info {
                        theResponseString
                    }

                    assertEquals("${NotificationServers.DEVICE_PATH}${TestData.TOKEN}", theRequest.path)

                    assertEquals(TestData.BODY_VALUE, alertDictionary.get("body").asString)
                    assertEquals(TestData.SUBTITLE_VALUE, alertDictionary.get("subtitle").asString)

                    assertEquals(
                        TestData.CUSTOM_PROPERTY_VALUE,
                        theBodySent.get(TestData.CUSTOM_PROPERTY_KEY).asString
                    )
                    assertEquals(
                        TestData.CUSTOM_PROPERTY_VALUE2,
                        theBodySent.get(TestData.CUSTOM_PROPERTY_KEY2).asBoolean
                    )

                    assertEquals(TestData.APNS_TOPIC_VALUE, apnsTopicHeader)

                    val platformResponses = it.results

                    val size = platformResponses.size
                    assertEquals(1, size)

                    platformResponses.forEach {
                        assertEquals(TestData.TOKEN, it.token)
                        assertEquals(200, it.response.status)
                        val reasonMessage = it.response.apns.reason
                        assertEquals("Success", reasonMessage)
                    }
                })
            }
    }

    @AfterAll
    fun tearDown() {
        mockWebServer.shutdown()
    }
}

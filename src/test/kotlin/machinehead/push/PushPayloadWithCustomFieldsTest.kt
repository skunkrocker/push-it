package machinehead.push

import com.google.gson.JsonParser
import com.squareup.okhttp.mockwebserver.MockWebServer
import machinehead.PushIt
import machinehead.okclient.OkClientAPNSRequest.Companion.TEST_URL_PROPERTY
import machinehead.push.TestData.Companion.`get development payload`
import machinehead.servers.NotificationServers
import mu.KotlinLogging
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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
        System.setProperty(TEST_URL_PROPERTY, url.toString())
    }

    @ExperimentalStdlibApi
    @Test
    fun `push notification for develop platform`() {
        //given
        val payload =
            `get development payload`(TestData.TOKEN, hashMapOf(TestData.APNS_TOPIC_KEY to TestData.APNS_TOPIC_VALUE))

        //when
        val pushIt = PushIt()
        pushIt.with(payload)

        //then
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

        assertEquals(TestData.CUSTOM_PROPERTY_VALUE, theBodySent.get(TestData.CUSTOM_PROPERTY_KEY).asString)
        assertEquals(TestData.CUSTOM_PROPERTY_VALUE2, theBodySent.get(TestData.CUSTOM_PROPERTY_KEY2).asBoolean)

        assertEquals(TestData.APNS_TOPIC_VALUE, apnsTopicHeader)

        val platformResponses = pushIt.platformResponseListener.platformResponses

        val size = platformResponses.size
        assertEquals(1, size)

        platformResponses.forEach {
            assertEquals(TestData.TOKEN, it.token)
            assertEquals(200.toString(), it.response.status)
            val reasonMessage = jsonParser.parse(it.response.message).asJsonObject.get("reason").asString
            assertEquals("Success", reasonMessage)
        }
    }

    @AfterAll
    fun tearDown() {
        mockWebServer.shutdown()
    }
}

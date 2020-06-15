package machinehead.push

import com.google.gson.JsonParser
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import machinehead.PushIt
import machinehead.model.Payload
import machinehead.model.payload
import machinehead.okclient.OkClientAPNSRequest.Companion.TEST_URL_PROPERTY
import machinehead.push.TestData.Companion.`get development payload`
import machinehead.servers.NotificationServers
import machinehead.servers.Stage
import mu.KotlinLogging
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.File


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
        val payload = `get development payload`()

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

class TestData {
    companion object {

        const val BODY_VALUE = "Hello World"
        const val SUBTITLE_VALUE = "Cool Subtitle"

        const val APNS_TOPIC_KEY = "apns-topic"
        const val APNS_TOPIC_VALUE = "ch.sbb.ios.pushnext"

        const val CUSTOM_PROPERTY_KEY = "custom-property"
        const val CUSTOM_PROPERTY_VALUE = "hello custom"

        const val CUSTOM_PROPERTY_KEY2 = "blow-up"
        const val CUSTOM_PROPERTY_VALUE2 = true

        const val TOKEN: String = "3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55"

        fun `get development payload`(): Payload {
            return payload {
                notification {
                    aps {
                        alert {
                            body = BODY_VALUE
                            subtitle = SUBTITLE_VALUE
                        }
                    }
                }
                headers = hashMapOf(
                    APNS_TOPIC_KEY to APNS_TOPIC_VALUE
                )
                custom = hashMapOf(
                    CUSTOM_PROPERTY_KEY to CUSTOM_PROPERTY_VALUE,
                    CUSTOM_PROPERTY_KEY2 to CUSTOM_PROPERTY_VALUE2
                )
                stage = Stage.DEVELOPMENT
                tokens = mutableListOf(TOKEN)
            }
        }
    }
}


fun MockResponse.getJson(path: String): String {
    val classLoader = this.javaClass.classLoader
    val uri = classLoader.getResource(path)
    val file = File(uri!!.path)
    return String(file.readBytes(), Charsets.UTF_8)
}
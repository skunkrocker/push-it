package machinehead.push

import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import machinehead.PushIt
import machinehead.model.Payload
import machinehead.model.payload
import machinehead.okclient.OkClientAPNSRequest.Companion.TEST_URL_PROPERTY
import machinehead.push.TestData.Companion.TOKEN
import machinehead.servers.Stage
import mu.KotlinLogging
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.File


@TestInstance(Lifecycle.PER_CLASS)
class PushNetworkingTest {

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

    @Test
    fun `push notification for develop platform`() {
        val payload = `get development payload`()

        val pushIt = PushIt()
        pushIt.with(payload)
        logger.info { "SERVICE RESPONSES: ${pushIt.platformResponseListener.platformResponses}" }

        val takeRequest = mockWebServer.takeRequest()
        logger.info { takeRequest.path }
        logger.info { "SERVER HAD THIS PAYLOAD PUSHED: ${String(takeRequest.body)}" }

    }

    @AfterAll
    fun tearDown() {
        mockWebServer.shutdown()
    }
}


fun `get development payload`(): Payload {
    return payload {
        notification {
            aps {
                alert {
                    body = "Hello"
                    subtitle = "Subtitle"
                }
            }
        }
        headers = hashMapOf(
            "apns-topic" to "ch.sbb.ios.pushnext"
        )
        custom = hashMapOf(
            "custom-property" to "hello custom",
            "blow-up" to true
        )
        stage = Stage.DEVELOPMENT
        tokens = mutableListOf(TOKEN)
    }
}

class TestData {
    companion object {
        const val TOKEN: String = "3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55"
    }
}


fun MockResponse.getJson(path: String): String {
    val classLoader = this.javaClass.classLoader
    val uri = classLoader.getResource(path)
    val file = File(uri!!.path)
    return String(file.readBytes(), Charsets.UTF_8)
}
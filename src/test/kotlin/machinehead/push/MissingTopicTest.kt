package machinehead.push

import machinehead.PushIt
import machinehead.push.TestData.Companion.TOKEN
import machinehead.push.TestData.Companion.`get test payload`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull

@TestInstance(Lifecycle.PER_CLASS)
class MissingTopicTest {

    @Test
    @ExperimentalStdlibApi
    fun `the payload is missing the topic header - return validation error`() {
        //given
        val payloadWithoutTopic = `get test payload`(TOKEN, hashMapOf())
        //when
        val pushIt = PushIt()
        pushIt.with(payloadWithoutTopic)
        //then
        val noTopic = pushIt.errorMessages.noTopic
        assertNotNull(noTopic)
        assertNotEquals(noTopic, "")
        assertEquals(
            noTopic,
            "you have to provide the \"apns-topic\" in the headers of the payload. It is usually the bundle id of your App"
        )
    }
}
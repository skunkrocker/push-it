package machinehead.push.headers

import machinehead.extensions.push
import machinehead.push.TestData.Companion.TOKEN
import machinehead.push.TestData.Companion.`get test payload`
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull

class MissingTopicHeaderTest {

    @Test
    @ExperimentalStdlibApi
    fun `the payload is missing the topic header - return validation error`() {

        `get test payload`(TOKEN, hashMapOf())
            .push { either ->
                either
                    .fold(
                        { clientError ->
                            val noTopic = clientError.message
                            assertNotNull(noTopic)
                            assertNotEquals(noTopic, "")
                            assertEquals(
                                noTopic,
                                "you have to provide the \"apns-topic\" in the headers of the payload. It is usually the bundle id of your App"
                            )
                        }, {
                            Assertions.assertFalse(true)
                        })
            }
    }
}
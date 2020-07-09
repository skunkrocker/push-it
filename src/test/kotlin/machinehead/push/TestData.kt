package machinehead.push

import machinehead.model.Payload
import machinehead.model.payload
import machinehead.servers.Stage

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
        const val BAD_DEVICE_TOKEN: String = "3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a54"

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

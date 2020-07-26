package machinehead.validation

import machinehead.model.payload
import machinehead.okclient.PayloadValidator.Companion.validate
import machinehead.servers.Stage
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be null`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.api.Assertions.assertTrue

class PayloadValidatorSpec : Spek({
    describe("invalid payloads are reported as such with client errors") {
        given("the payload doesn't include the device tokens") {
            val thePayload = payload {
                notification {
                    aps {
                        alert {
                            body = "hello world"
                        }
                    }
                }

                headers = hashMapOf(
                    "apns-topic" to "org.your.app.bundle.id"
                )
                custom = hashMapOf(
                    "custom-array" to arrayListOf(1, 2, 3),
                    "custom-boolean" to true
                )
            }

            it("should be invalid payload and reported with the following client error") {
                validate(thePayload)
                    .fold({
                        assertTrue(false)
                    }, {
                        it.message.`should not be null`()
                        it.message `should be equal to` "payload must include the device tokens"
                    })
            }
        }
        given("a payload without valid notification is passed") {

            val thePayload = payload {
                notification {
                    //invalid notification
                }
                tokens =
                    listOf("3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55")

                headers = hashMapOf(
                    "apns-topic" to "org.your.app.bundle.id"
                )
                custom = hashMapOf(
                    "custom-array" to arrayListOf(1, 2, 3),
                    "custom-boolean" to true
                )
            }

            it("should be reported as invalid with the right client error message") {
                validate(thePayload)
                    .fold({
                        assertTrue(false)
                    }, {
                        it.message.`should not be null`()
                        it.message `should be equal to` "valid notification must include at least the alert dictionary with body set"
                    })
            }
        }
        given("the payload is passed without apns-topic in the headers") {
            val thePayload =
                payload {
                    notification {
                        aps {
                            alert {
                                body = "Hello"
                                subtitle = "Subtitle"
                            }
                        }
                    }

                    stage = Stage.DEVELOPMENT
                    tokens = listOf("3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55")
                }
            it("should report the missing apns-topic with the appropriate client error message") {

                validate(thePayload)
                    .fold({
                        assertTrue(false)
                    }, {
                        it.message.`should not be null`()
                        it.message `should be equal to` "you have to provide the \"apns-topic\" in the headers of the payload. It is usually the bundle id of your App"
                    })
            }
        }
    }
    describe("valid payloads are not reporting any client errors") {
        given("the passed payload is valid") {
            val thePayload =
                payload {
                    notification {
                        aps {
                            alert {
                                body = "Hello"
                                subtitle = "Subtitle"
                            }
                        }
                    }
                    headers = hashMapOf(
                        "apns-topic" to "org.your.app.bundle.id"
                    )
                    custom = hashMapOf(
                        "custom-property" to "hello custom",
                        "blow-up" to true
                    )
                    tokens =
                        listOf("3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55")
                }
            it("should pass validation and report no client errors") {
                validate(thePayload)
                    .fold({
                        assertTrue(true)
                    }, {
                        assertTrue(false)
                    })
            }
        }
    }
})
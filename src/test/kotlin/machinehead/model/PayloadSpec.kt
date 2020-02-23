package machinehead.model

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotBeNull
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

class PayloadTestData {
    companion object {
        const val badge: Int = 1
        const val body: String = "Hello world"
        const val title: String = "Awesome payload"

        const val token1 = "123456789"
        const val token2 = "987654321"

        const val custom_property = "custom-property"
    }
}


class PayloadSpec : Spek({
    given("we use the dsl builder to construct our payload") {

        val thePayload = payload {
            notification {
                aps {
                    alert {
                        body = PayloadTestData.body
                        title = PayloadTestData.title
                    }
                    badge = PayloadTestData.badge
                }
            }
        }

        it("should contain the correct body, title and badge") {

            thePayload.custom.shouldNotBeNull()
            thePayload.notification.shouldNotBeNull()
            thePayload.notification?.aps.shouldNotBeNull()
            thePayload.notification?.aps?.alert.shouldNotBeNull()


            val badge = thePayload.notification?.aps?.badge
            val body = thePayload.notification?.aps?.alert?.body
            val title = thePayload.notification?.aps?.alert?.title

            body shouldBeEqualTo PayloadTestData.body
            badge shouldBeEqualTo PayloadTestData.badge
            title shouldBeEqualTo PayloadTestData.title
        }
    }

    given("device tokes were provided with the payload builder") {
        val thePayload = payload {
            tokens = listOf(PayloadTestData.token1, PayloadTestData.token2)
        }

        it("should contain the list of device tokens") {
            thePayload.tokens shouldContain PayloadTestData.token1
            thePayload.tokens shouldContain PayloadTestData.token2
        }
    }

    given("custom properties are provided with payload builder") {
        val thePayload = payload {
            custom = hashMapOf(PayloadTestData.custom_property to 1)
        }
        it("should contain the custom property") {
            thePayload.custom.keys shouldContain PayloadTestData.custom_property
        }
    }
})
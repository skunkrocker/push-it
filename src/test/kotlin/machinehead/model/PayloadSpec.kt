package machinehead.model

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

class PayloadTestData {
    companion object {
        const val body: String = "Hello world"
        const val title: String = "Awesome payload"
        const val badge: Int = 1
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

            thePayload.custom shouldNotBe null
            thePayload.notification shouldNotBe null
            thePayload.notification?.aps shouldNotBe null
            thePayload.notification?.aps?.alert shouldNotBe null


            val badge = thePayload.notification?.aps?.badge
            val body = thePayload.notification?.aps?.alert?.body
            val title = thePayload.notification?.aps?.alert?.title

            body shouldBeEqualTo PayloadTestData.body
            badge shouldBeEqualTo PayloadTestData.badge
            title shouldBeEqualTo PayloadTestData.title
        }
    }
})
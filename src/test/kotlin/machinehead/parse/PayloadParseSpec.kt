package machinehead.parse

import com.google.gson.JsonParser
import machinehead.extensions.notificationAsString
import machinehead.model.payload
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be null`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class PayloadParseSpecSpec : Spek({
    describe("the notification from the payload can be parsed to a string") {
        val stringNotification = payload {
            notification {
                aps {
                    alert {
                        body = "hello world"
                    }
                }
            }
            custom = hashMapOf(
                "custom-array" to arrayListOf(1, 2, 3),
                "custom-boolean" to true
            )
        }.notificationAsString()

        it("should contain the body and aps and alert dictionary ") {
            val notification = JsonParser().parse(stringNotification)

            val aps = notification.asJsonObject.getAsJsonObject("aps")
            aps.`should not be null`()

            val alert = aps.asJsonObject.getAsJsonObject("alert")
            alert.`should not be null`()

            val body = alert.get("body").asString
            body.`should not be null`()
            body `should be equal to` "hello world"
        }
    }
})
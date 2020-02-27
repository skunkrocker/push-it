package machinehead

import machinehead.model.Payload
import machinehead.model.payload
import machinehead.model.yaml.From
import machinehead.model.yaml.YAMLFile
import machinehead.parse.ParseErrors
import machinehead.parse.notificationAsString

data class PayloadValidation(val wasValid: Boolean, val error: String)
data class P12CertEnvKeys(var passKey: String, var encodedP12Key: String)

object Push {
    private val errors = From the YAMLFile("payload-errors.yml", ParseErrors::class)

    infix fun the(payload: Payload) {
        validate(payload, {
            it notificationAsString {

            }
        }, {

        })
    }

    private fun validate(payload: Payload, ifValid: (Payload) -> Unit, ifNotValid: (PayloadValidation) -> Unit) {
        if (payload.notification?.aps?.alert == null) {
            ifNotValid(PayloadValidation(false, this.errors.noAlert))
            return
        }
        if (payload.tokens.isEmpty()) {
            ifNotValid(PayloadValidation(false, this.errors.noTokens))
            return
        }
        ifValid(payload)
    }
}

fun main() {

    val payload = payload {
        notification {
            aps {
                alert {
                    body = "Hello"
                    subtitle = "Subtitle"
                }
            }
        }
        custom = hashMapOf(
            "custom-property" to "hello custom",
            "blow-up" to true
        )
        tokens = arrayListOf("", "")
    }
    Push the payload
}
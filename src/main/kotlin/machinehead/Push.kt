package machinehead

import machinehead.model.Payload
import machinehead.model.payload

data class P12CertEnvKeys(var passKey: String, var encodedP12Key: String)

class Push {
    companion object {
        infix fun the(payload: Payload) {

        }
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
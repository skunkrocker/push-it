package machinehead.model

import com.google.gson.GsonBuilder

fun main() {

    val payload = payload {
        notification {
            aps {
                alert {
                    title = "Hello world"
                    subtitle = "wow"
                    body = "Hello"
                }
                badge = 1
                content_available = 1
            }
        }
        custom = hashMapOf("hello-custom" to 2)
    }

    var gson = GsonBuilder().setPrettyPrinting().create();
    print(gson.toJson(payload.notification))
}
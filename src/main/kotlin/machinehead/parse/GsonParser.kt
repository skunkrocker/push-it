package machinehead.parse

import com.google.gson.GsonBuilder
import machinehead.model.Payload

data class ParseErrors(
    var noTopic: String?,
    var noAlert: String?,
    var noTokens: String?,
    var credentialsError: String?,
    var noCredentialsManager: String?
)

fun gson() =
    GsonBuilder()
        .setPrettyPrinting()
        .create()

package machinehead.parse

import com.google.gson.GsonBuilder

data class ParseErrors(
    var noTopic: String?,
    var noAlert: String?,
    var noTokens: String?,
    var credentialsError: String?,
    var noCredentialsManager: String?
)

val gson = GsonBuilder()
    .setPrettyPrinting()
    .create()

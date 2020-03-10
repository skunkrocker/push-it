package machinehead.result

class InvalidNotification(message: String) : Exception(message)
class TokensMissingException(message: String) : Exception(message)
class CredentialsException(message: String) : Exception(message)

data class Response(val status: String, val message: String)
data class PushResult(val responses: Map<String, Response>?, val internalError: Exception?)

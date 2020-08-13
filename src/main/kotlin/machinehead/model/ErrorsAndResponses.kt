package machinehead.model

data class ClientError(val message: String)
data class APNSResponse(val reason: String)
data class RequestError(val token: String, val message: String)
data class PlatformResponse(val status: Int, val apns: APNSResponse)
data class PushResult(val token: String, val response: PlatformResponse)

class RequestErrorsAndResults(
    val errors: List<RequestError>,
    var results: List<PushResult>
)

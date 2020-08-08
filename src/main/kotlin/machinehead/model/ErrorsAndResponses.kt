package machinehead.model

import arrow.core.Option

data class ClientError(val message: String)
data class APNSResponse(val reason: String)
data class RequestError(val token: String, val message: String)
data class PlatformResponse(val status: Int, val apns: APNSResponse)
data class PushResult(val token: String, val response: PlatformResponse)

class RequestErrorsAndResults(
    val errors: List<RequestError>,
    var results: List<PushResult>
)

class ClientErrorListener {
    private val clientErrorList = mutableListOf<ClientError>()
    infix fun report(error: ClientError) {
        clientErrorList.add(error)
    }

    val clientErrors: List<ClientError> get() = clientErrorList
}

class PlatformResponseListener {
    private val platformResponseList = mutableListOf<PushResult>()

    fun report(response: Option<PushResult>) {
        response.fold({}, {
            platformResponseList.add(it)
        })
    }

    val platformResponses get() = platformResponseList
}

class RequestErrorListener {
    private val requestErrorList = mutableListOf<RequestError>()

    fun report(error: Option<RequestError>) {
        error.fold({}, {
            requestErrorList.add(it)
        })
    }

    val requestErrors get() = requestErrorList
}

class ResponsesAndErrors(
    val clientErrors: List<ClientError> = mutableListOf(),
    val requestErrors: List<RequestError> = mutableListOf(),
    var responses: List<PushResult> = mutableListOf()
)

class ErrorsAndResponses(
    val clientErrors: List<ClientError>,
    val requestErrors: List<RequestError>,
    var responses: List<PushResult>
)

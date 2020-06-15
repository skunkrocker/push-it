package machinehead.model

import arrow.core.Option

data class ClientError(val message: String)
data class RequestError(val token: String, val message: String)
data class PlatformResponse(val status: String, val message: String)
data class PushResult(val token: String, val response: PlatformResponse)


class ClientErrorListener {
    private val clientErrorList = mutableListOf<ClientError>()
    infix fun report(error: ClientError) {
        clientErrorList.add(error)
    }

    val hasErrors: Boolean get() = clientErrorList.isNotEmpty()
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
    val clientErrors: List<ClientError> = ArrayList(),
    val requestErrors: List<RequestError> = ArrayList(),
    var responses: List<PushResult> = ArrayList()
)


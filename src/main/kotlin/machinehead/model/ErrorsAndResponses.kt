package machinehead.model

import arrow.core.Option

class ClientError(val message: String)
class RequestError(val token: String, val message: String)
data class PlatformResponse(val status: String, val message: String)

class ClientErrorListener {
    private val clientErrorList = mutableListOf<ClientError>()
    infix fun report(error: ClientError) {
        clientErrorList.add(error)
    }

    val hasErrors: Boolean get() = clientErrorList.isNotEmpty()
    val clientErrors: List<ClientError> get() = clientErrorList
}

class PlatformResponseListener {
    private val platformResponseList = mutableListOf<Option<PlatformResponse>>()

    fun report(response: Option<PlatformResponse>) {
        this.platformResponseList.add(response)
    }

    val platformResponses get() = platformResponseList
}

class RequestErrorListener {
    private val requestErrorList = mutableListOf<Option<RequestError>>()

    fun report(error: Option<RequestError>) {
        requestErrorList.add(error)
    }

    val requestErrors get() = requestErrorList
}

class ResponsesAndErrors {
    val clientErrors: List<ClientError> = ArrayList()
    val requestErrors: List<Option<RequestError>> = ArrayList()
    var platformResponses: List<Option<PlatformResponse>> = ArrayList()
}


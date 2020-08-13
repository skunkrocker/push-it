package machinehead.okclient

import machinehead.extensions.isNotNullOrEmpty
import machinehead.model.RequestError
import machinehead.servers.NotificationServers
import machinehead.servers.Stage
import mu.KotlinLogging
import okhttp3.Request
import okhttp3.RequestBody

interface RequestService {
    fun get(onError: (RequestError) -> Unit, get: (Request) -> Unit)
}

class RequestServiceImpl(val token: String, val body: RequestBody, val stage: Stage) : RequestService {

    private val logger = KotlinLogging.logger { }

    private var request: Request? = null
    private var error: RequestError? = null

    init {
        createRequest({
            error = it
        }, {
            request = it
        })
    }

    override fun get(onError: (RequestError) -> Unit, get: (Request) -> Unit) {
        request?.let(get)
        error?.let(onError)
    }

    private fun createRequest(onError: (RequestError) -> Unit, onCreate: (Request) -> Unit) {
        try {
            val url = getUrl() + token
            logger.debug { "final push url is: $url" }

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            onCreate(request)
        } catch (e: Exception) {
            logger.error { "failed to create request for the token: $token" }
            onError(RequestError(token, "failed to create request for the token"))
        }
    }

    private fun getUrl(): String {
        var url = NotificationServers.urlForStage(stage)

        if (System.getProperty(OkClientAPNSRequest.TEST_URL_PROPERTY).isNotNullOrEmpty()) {
            url = NotificationServers.urlForStage(Stage.TEST)
            logger.warn { "you overwrite the APNS  url to: $url " }
            logger.warn { "if you didn't do this for test purposes, please remove the property 'localhost.url' from your ENV" }
        }
        return url
    }
}
package machinehead.okclient

import arrow.core.Either
import machinehead.exceptions.ClientCreationException
import machinehead.model.*
import org.koin.java.KoinJavaComponent.inject

interface PushNotification {
    fun push(payload: Payload): Either<RequestError, PushResult>
}

class PushNotificationImpl(val token: String) : PushNotification {

    private val okClientService by inject(OkClientService::class.java)

    override fun push(payload: Payload): Either<RequestError, PushResult> {
        okClientService.getHttpClient()
        val pushResult = PushResult(token, PlatformResponse(200, APNSResponse("Success")))

        return Either.right(pushResult)
    }
}
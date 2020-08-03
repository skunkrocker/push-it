package machinehead.okclient

import machinehead.model.APNSResponse
import machinehead.model.Payload
import machinehead.model.PlatformResponse
import machinehead.model.PushResult
import org.koin.java.KoinJavaComponent.inject

interface PushNotification {
    fun push(payload: Payload): PushResult
    fun getResult(): PushResult
    fun setToken(token: String)
}

class PushNotificationImpl : PushNotification {
    private lateinit var token: String

    private val okClientService by inject(OkClientService::class.java)

    override fun push(payload: Payload): PushResult {
        okClientService.getHttpClient()
        return PushResult(token, PlatformResponse(200, APNSResponse("Success")));
    }

    override fun getResult(): PushResult {
        return PushResult(token, PlatformResponse(200, APNSResponse("Success")));
    }

    override fun setToken(token: String) {
        this.token = token
    }
}
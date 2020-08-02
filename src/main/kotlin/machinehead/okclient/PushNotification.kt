package machinehead.okclient

import machinehead.credentials.CredentialsService
import machinehead.extensions.notificationAsString
import machinehead.model.APNSResponse
import machinehead.model.Payload
import machinehead.model.PlatformResponse
import machinehead.model.PushResult
import okhttp3.ResponseBody
import org.koin.java.KoinJavaComponent.inject

interface PushNotification {
    fun push(payload: Payload)
    fun getResult(): PushResult
}

class PushNotificationImpl : PushNotification {
    private val credentials by inject(CredentialsService::class.java)
    override fun push(payload: Payload) {
        credentials.createCredentials()
        println(payload.notificationAsString())
    }

    override fun getResult(): PushResult {
        return PushResult("", PlatformResponse(200, APNSResponse("Success")));
    }
}
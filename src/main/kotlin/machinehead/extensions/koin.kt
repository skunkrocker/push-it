package machinehead.extensions

import arrow.core.None
import arrow.core.Option
import machinehead.credentials.CredentialsService
import machinehead.credentials.CredentialsServiceImpl
import machinehead.model.ClientError
import machinehead.model.Payload
import machinehead.model.ResponsesAndErrors
import machinehead.okclient.OkClientService
import machinehead.okclient.OkClientServiceImpl
import machinehead.okclient.PushNotification
import machinehead.okclient.PushNotificationImpl
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.get
import org.koin.java.KoinJavaComponent.inject

interface ValidatePayloadService {
    fun isValid(payload: Payload): Option<ClientError>
}

class ValidatePayloadServiceImpl : ValidatePayloadService {
    override fun isValid(payload: Payload): Option<ClientError> {
        return None
    }
}

interface PushingService {
    fun with(payload: Payload)
}

class PushingServiceImpl : PushingService {
    private val validatePayloadService by inject(ValidatePayloadService::class.java)

    override fun with(payload: Payload) {
        validatePayloadService
            .isValid(payload)
            .fold(
                {
                    val list = mutableListOf<PushNotification>()
                    for (i in 2 downTo 1) {
                        val notification = get(PushNotification::class.java)
                        list.add(notification)
                    }
                    list.forEach {
                        it.push(payload)
                        println(it.getResult().response.apns.reason)
                    }
                }, {
                    println(it)
                }
            )
    }
}

infix fun Payload.pushk(report: (ResponsesAndErrors) -> Unit) {
    val modules = module {
        single { CredentialsServiceImpl() as CredentialsService }
        single { ValidatePayloadServiceImpl() as ValidatePayloadService }
        single { OkClientServiceImpl() as OkClientService }
        factory { PushNotificationImpl() as PushNotification }
    }
    val app = startKoin {
        modules(modules)
    }
    PushingServiceImpl().with(this)
    app.close()
}

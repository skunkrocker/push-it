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

class PushApp {
    private val validatePayloadService by inject(ValidatePayloadService::class.java)

    fun with(payload: Payload) {
        validatePayloadService
            .isValid(payload)
            .fold(
                {
                    val list = mutableListOf<PushNotification>()
                    payload
                        .tokens
                        .forEach {
                            val notification = get(PushNotification::class.java)
                            notification.setToken(it)
                            notification.push(payload)
                            list.add(notification)
                        }

                    list.forEach {
                        println(it.getResult().response.apns.reason)
                    }
                }, {
                    println(it)
                }
            )
    }
}

infix fun Payload.pushIt(report: (ResponsesAndErrors) -> Unit) {
    val services = module {
        single { OkClientServiceImpl() as OkClientService }
        factory { PushNotificationImpl() as PushNotification }
        single { CredentialsServiceImpl() as CredentialsService }
        single { ValidatePayloadServiceImpl() as ValidatePayloadService }
    }
    val appContext = startKoin {
        modules(services)
    }
    PushApp().with(this)
    appContext.close()
}

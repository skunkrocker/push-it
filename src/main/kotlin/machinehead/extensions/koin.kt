package machinehead.extensions

import arrow.core.None
import arrow.core.Option
import kotlinx.coroutines.*
import machinehead.credentials.CredentialsService
import machinehead.credentials.CredentialsServiceImpl
import machinehead.model.ClientError
import machinehead.model.Payload
import machinehead.model.ResponsesAndErrors
import machinehead.okclient.*
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.get
import org.koin.java.KoinJavaComponent.inject
import machinehead.model.PushResult

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
                    performPush(payload) {
                        println(it)
                    }
                }, {
                    println(it)
                }
            )
    }

    private fun performPush(payload: Payload, onCompleted: (List<PushResult>) -> Unit) = runBlocking {
        val deferredList = mutableListOf<Deferred<PushResult>>()

        coroutineScope {
            payload.tokens.forEach {
                deferredList.add(
                    async {
                        val notification = get(PushNotification::class.java)
                        notification.setToken(it)
                        notification.push(payload)
                    }
                )
            }
            val results = deferredList.awaitAll()
            onCompleted(results)
        }
    }
}

infix fun Payload.pushIt(report: (ResponsesAndErrors) -> Unit) {
    val headers = this.headers
    val services = module {
        single { OkClientServiceImpl() as OkClientService }
        factory { PushNotificationImpl() as PushNotification }
        single { CredentialsServiceImpl() as CredentialsService }
        single { ValidatePayloadServiceImpl() as ValidatePayloadService }
        single { InterceptorChainServiceImpl(headers) as InterceptorChainService }
    }
    val appContext = startKoin {
        modules(services)
    }
    PushApp().with(this)
    appContext.close()
}

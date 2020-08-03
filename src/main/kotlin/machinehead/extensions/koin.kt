package machinehead.extensions

import kotlinx.coroutines.*
import machinehead.credentials.CredentialsService
import machinehead.credentials.CredentialsServiceImpl
import machinehead.model.Payload
import machinehead.model.ResponsesAndErrors
import machinehead.okclient.*
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.get
import org.koin.java.KoinJavaComponent.inject
import machinehead.model.PushResult
import machinehead.validation.ValidatePayloadService
import machinehead.validation.ValidatePayloadServiceImpl
import org.koin.core.parameter.parametersOf

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
                        val notification = get(PushNotification::class.java) { parametersOf(it) }
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
        single { CredentialsServiceImpl() as CredentialsService }
        single { ValidatePayloadServiceImpl() as ValidatePayloadService }
        single { InterceptorChainServiceImpl(headers) as InterceptorChainService }
        factory { (token: String) -> PushNotificationImpl(token) as PushNotification }
    }
    val appContext = startKoin {
        modules(services)
    }
    PushApp().with(this)
    appContext.close()
}

package machinehead.extensions

import arrow.core.Either
import kotlinx.coroutines.*
import machinehead.credentials.CredentialsService
import machinehead.credentials.CredentialsServiceImpl
import machinehead.model.*
import machinehead.okclient.*
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.get
import org.koin.java.KoinJavaComponent.inject
import machinehead.validation.ValidatePayloadService
import machinehead.validation.ValidatePayloadServiceImpl
import org.koin.core.parameter.parametersOf

class PushApp {
    private val validatePayloadService by inject(ValidatePayloadService::class.java)

    fun with(payload: Payload, report: (ErrorsAndResponses) -> Unit) {
        validatePayloadService
            .isValid(payload)
            .fold(
                {
                    val pushResults = mutableListOf<PushResult>()
                    val requestErrors = mutableListOf<RequestError>()

                    performPush(payload) { pushErrorOrResult ->
                        pushErrorOrResult.forEach {
                            it.fold({ error ->
                                requestErrors.add(error)
                            }, { result ->
                                pushResults.add(result)
                            })
                        }
                    }

                    report(ErrorsAndResponses(emptyList(), requestErrors, pushResults))

                }, {
                    report(ErrorsAndResponses(listOf(it), emptyList(), emptyList()))
                }
            )
    }

    private fun performPush(payload: Payload, report: (List<Either<RequestError, PushResult>>) -> Unit) = runBlocking {
        val deferredList = mutableListOf<Deferred<Either<RequestError, PushResult>>>()

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
            report(results)
        }
    }
}

infix fun Payload.pushIt(report: (ErrorsAndResponses) -> Unit) {
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
    PushApp().with(this) {
        report(it)
    }
    appContext.close()
}
